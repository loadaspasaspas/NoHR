/**
 *
 */
package pt.unl.fct.di.centria.nohr.deductivedb;

import pt.unl.fct.di.centria.nohr.model.Answer;
import pt.unl.fct.di.centria.nohr.model.Atom;
import pt.unl.fct.di.centria.nohr.model.Constant;
import pt.unl.fct.di.centria.nohr.model.FormatVisitor;
import pt.unl.fct.di.centria.nohr.model.IndividualConstant;
import pt.unl.fct.di.centria.nohr.model.LiteralConstant;
import pt.unl.fct.di.centria.nohr.model.Model;
import pt.unl.fct.di.centria.nohr.model.NegativeLiteral;
import pt.unl.fct.di.centria.nohr.model.NumericConstant;
import pt.unl.fct.di.centria.nohr.model.Query;
import pt.unl.fct.di.centria.nohr.model.Rule;
import pt.unl.fct.di.centria.nohr.model.RuleConstant;
import pt.unl.fct.di.centria.nohr.model.Variable;
import pt.unl.fct.di.centria.nohr.model.predicates.MetaPredicate;
import pt.unl.fct.di.centria.nohr.model.predicates.Predicate;

/**
 * An {@link FormatVisitor} to format the {@link Rules rules} and {@link TableDirective table directives} that are sent to a XSB Prolog engine,
 * according to the XSB syntax.
 *
 * @author Nuno Costa
 */
public class XSBFormatVisitor implements FormatVisitor {

	private String quoted(String str) {
		return "'" + str.replaceAll("'", "''") + "'";
	}

	@Override
	public String visit(Answer answer) {
		return Model.concat(answer.apply(), this, ",");
	}

	@Override
	public String visit(Atom atom) {
		final String pred = atom.getFunctor().accept(this);
		final String args = Model.concat(atom.getArguments(), this, ",");
		if (atom.getArity() == 0)
			return pred;
		return pred + "(" + args + ")";
	}

	@Override
	public String visit(Constant constant) {
		return quoted(constant.getSymbol());
	}

	@Override
	public String visit(IndividualConstant constant) {
		return quoted(constant.getOWLIndividual().toStringID());
	}

	@Override
	public String visit(LiteralConstant constant) {
		return quoted(constant.getOWLLiteral().getLiteral()
				+ (constant.getOWLLiteral().getLang() != null ? constant.getOWLLiteral().getLang() : ""));
	}

	@Override
	public String visit(MetaPredicate metaPredicate) {
		return quoted(metaPredicate.getSymbol());
	}

	@Override
	public String visit(NegativeLiteral literal) {
		final String format = literal.isExistentiallyNegative() ? "not_exists(%s)" : "tnot(%s)";
		return String.format(format, literal.getAtom().accept(this));
	}

	@Override
	public String visit(NumericConstant constant) {
		return constant.getNumber().toString();
	}

	@Override
	public String visit(Predicate predicate) {
		if (predicate.getSymbol().equals("fail"))
			return predicate.getSymbol();
		return quoted(predicate.getSymbol());
	}

	@Override
	public String visit(Query query) {
		return Model.concat(query.getLiterals(), this, ",");
	}

	@Override
	public String visit(Rule rule) {
		final String head = rule.getHead().accept(this);
		final String body = Model.concat(rule.getBody(), this, ",");
		if (rule.isFact())
			return head + ".";
		else
			return head + ":-" + body + ".";
	}

	@Override
	public String visit(RuleConstant constant) {
		return quoted(constant.getSymbol());
	}

	@Override
	public String visit(Variable variable) {
		return variable.getSymbol();
	}

}
