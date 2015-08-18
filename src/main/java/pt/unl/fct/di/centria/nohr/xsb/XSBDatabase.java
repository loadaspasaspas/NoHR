package pt.unl.fct.di.centria.nohr.xsb;

import static pt.unl.fct.di.centria.nohr.model.Model.ans;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.declarativa.interprolog.AbstractPrologEngine;
import com.declarativa.interprolog.SolutionIterator;
import com.declarativa.interprolog.TermModel;
import com.declarativa.interprolog.XSBSubprocessEngine;
import com.declarativa.interprolog.util.IPException;

import pt.unl.fct.di.centria.nohr.model.Answer;
import pt.unl.fct.di.centria.nohr.model.FormatVisitor;
import pt.unl.fct.di.centria.nohr.model.Model;
import pt.unl.fct.di.centria.nohr.model.Query;
import pt.unl.fct.di.centria.nohr.model.Term;
import pt.unl.fct.di.centria.nohr.model.TruthValue;
import pt.unl.fct.di.centria.nohr.model.Variable;

public class XSBDatabase {

	protected final FormatVisitor formatVisitor;

	private SolutionIterator lastSolutionsIterator;

	protected final File xsbBinDirectory;

	protected AbstractPrologEngine xsbEngine;

	public XSBDatabase(File xsbBinDirectory) throws IPException, XSBDatabaseCreationException {
		Objects.requireNonNull(xsbBinDirectory);
		this.xsbBinDirectory = xsbBinDirectory;
		formatVisitor = new XSBFormatVisitor();
		startXsbEngine();
	}

	public void abolishTables() {
		xsbEngine.deterministicGoal("abolish_all_tables");
	}

	// TODO remove
	public void add(String rule) {
		xsbEngine.deterministicGoal("assert((" + rule + "))");
	}

	private void addAnswer(TermModel valuesList, Map<List<Term>, TruthValue> answers) {
		final TermModel[] termsList = valuesList.flatList();
		final List<Term> vals = new ArrayList<Term>(termsList.length);
		for (int i = 1; i < termsList.length; i++)
			vals.add(TermModelAdapter.getTerm(termsList[i]));
		final TruthValue truth = TermModelAdapter.getTruthValue(termsList[0]);
		answers.put(vals, truth);

	}

	private Answer answer(Query query, Map<Variable, Integer> varsIdx, TermModel valuesList) {
		final TermModel[] termsList = valuesList.flatList();
		final TruthValue truth = TermModelAdapter.getTruthValue(termsList[0]);
		final List<Term> vals = new ArrayList<Term>(termsList.length);
		for (int i = 1; i <= varsIdx.size(); i++)
			vals.add(TermModelAdapter.getTerm(termsList[i]));
		return ans(query, truth, vals);
	}

	public void cancelLastIterator() {
		if (lastSolutionsIterator != null)
			lastSolutionsIterator.cancel();
		lastSolutionsIterator = null;
	}

	public void clear() {
		try {
			startXsbEngine();
		} catch (IPException | XSBDatabaseCreationException e) {
			throw new RuntimeException(e);
		}
	}

	private XSBSubprocessEngine createXSBSubprocessEngine() throws IPException, XSBDatabaseCreationException {
		XSBSubprocessEngine result = null;
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final Future<XSBSubprocessEngine> future = executor.submit(new Callable<XSBSubprocessEngine>() {

			@Override
			public XSBSubprocessEngine call() throws Exception {
				return new XSBSubprocessEngine(xsbBinDirectory.toPath().toAbsolutePath().toString());
			}
		});

		try {
			result = future.get(3, TimeUnit.SECONDS);
		} catch (final TimeoutException e) {
			// Without the below cancel the thread will continue to live
			// even though the timeout exception thrown.
			future.cancel(false);
			throw new XSBDatabaseCreationException();
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		} catch (final ExecutionException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof IPException)
				throw (IPException) cause;
			else
				throw new RuntimeException(e);
		}

		executor.shutdownNow();
		return result;
	}

	public void dispose() {
		xsbEngine.shutdown();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		dispose();
	}

	public boolean hasAnswers(Query query) {
		return hasAnswers(query, null);
	}

	public boolean hasAnswers(Query query, Boolean trueAnswers) {
		String goal;
		if (trueAnswers == null)
			goal = query.accept(formatVisitor);
		else {
			final String truth = trueAnswers ? "true" : "undefined";
			goal = String.format("call_tv((%s),%s)", query.accept(formatVisitor), truth);
		}
		return xsbEngine.deterministicGoal(goal);
	}

	public Iterable<Answer> lazilyQuery(Query query) {
		return lazilyQuery(query, null);
	}

	public Iterable<Answer> lazilyQuery(final Query query, Boolean trueAnswers) {
		if (lastSolutionsIterator != null) {
			lastSolutionsIterator.cancel();
			lastSolutionsIterator = null;
		}
		final String vars = Model.concat(query.getVariables(), formatVisitor, ",");
		String goal;
		if (trueAnswers == null)
			goal = String.format("detGoal([%s],(%s),TM)", vars, query.accept(formatVisitor));
		else {
			final String truth = trueAnswers ? "true" : "undefined";
			goal = String.format("detGoal([%s],(%s),%s,TM)", vars, query.accept(formatVisitor), truth);
		}
		final Map<Variable, Integer> varsIdx = variablesIndex(query.getVariables());
		final SolutionIterator solutions = xsbEngine.goal(goal, "[TM]");
		lastSolutionsIterator = solutions;
		final XSBDatabase xsbDatabase = this;
		return new Iterable<Answer>() {

			@Override
			public Iterator<Answer> iterator() {
				return new Iterator<Answer>() {

					private boolean canceled;

					@Override
					public boolean hasNext() {
						if (canceled)
							return false;
						return solutions.hasNext();
					}

					@Override
					public Answer next() {
						final Object[] bindings = solutions.next();
						if (!solutions.hasNext()) {
							solutions.cancel();
							canceled = true;
							xsbDatabase.lastSolutionsIterator = null;
						}
						final TermModel valuesList = (TermModel) bindings[0];
						return answer(query, varsIdx, valuesList);
					}

					@Override
					public void remove() {
						solutions.remove();
					}
				};
			}

		};
	}

	public void load(File file) {
		final boolean loaded = xsbEngine.load_dynAbsolute(file);
		if (!loaded)
			throw new IPException("file not loaded");
	}

	public Answer query(Query query) {
		return query(query, null);
	}

	public Answer query(Query query, Boolean trueAnswers) {
		final String vars = Model.concat(query.getVariables(), formatVisitor, ",");
		String goal;
		if (trueAnswers == null)
			goal = String.format("detGoal([%s],(%s),TM)", vars, query.accept(formatVisitor));
		else {
			final String truth = trueAnswers ? "true" : "undefined";
			goal = String.format("detGoal([%s],(%s),%s,TM)", vars, query.accept(formatVisitor), truth);
		}
		final Object[] bindings = xsbEngine.deterministicGoal(goal, "[TM]");
		if (bindings == null)
			return null;
		return answer(query, variablesIndex(query.getVariables()), (TermModel) bindings[0]);
	}

	public Map<List<Term>, TruthValue> queryAll(Query query) {
		return queryAll(query, null);
	}

	public Map<List<Term>, TruthValue> queryAll(Query query, Boolean trueAnswers) {
		final Map<List<Term>, TruthValue> answers = new HashMap<List<Term>, TruthValue>();
		final String vars = Model.concat(query.getVariables(), formatVisitor, ",");
		String goal;
		if (trueAnswers == null)
			goal = String.format("nonDetGoal([%s],(%s),TM)", vars, query.accept(formatVisitor));
		else {
			final String truth = trueAnswers ? "true" : "undefined";
			goal = String.format("nonDetGoal([%s],(%s),%s,TM)", vars, query.accept(formatVisitor), truth);
		}
		final Object[] bindings = xsbEngine.deterministicGoal(goal, "[TM]");
		if (bindings == null)
			return answers;
		final TermModel ansList = (TermModel) bindings[0];
		for (final TermModel ans : ansList.flatList())
			addAnswer(ans, answers);
		return answers;
	}

	protected void startXsbEngine() throws IPException, XSBDatabaseCreationException {
		if (xsbEngine != null) {
			xsbEngine.shutdown();
			xsbEngine = null;
		}
		xsbEngine = createXSBSubprocessEngine();
		final XSBDatabase self = this;
		xsbEngine.consultFromPackage("startup", self);
		xsbEngine.deterministicGoal("set_prolog_flag(unknown, fail)");
	}

	// TODO remove
	public void table(String predicate) {
		xsbEngine.deterministicGoal("table " + predicate);
	}

	private SortedMap<Variable, Integer> variablesIndex(List<Variable> variables) {
		final SortedMap<Variable, Integer> result = new TreeMap<Variable, Integer>();
		int i = 0;
		for (final Variable var : variables)
			result.put(var, i++);
		return result;
	}
}