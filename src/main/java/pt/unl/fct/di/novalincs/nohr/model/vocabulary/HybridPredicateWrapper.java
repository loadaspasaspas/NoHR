package pt.unl.fct.di.novalincs.nohr.model.vocabulary;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLProperty;

import pt.unl.fct.di.novalincs.nohr.model.FormatVisitor;
import pt.unl.fct.di.novalincs.nohr.model.Predicate;

public class HybridPredicateWrapper implements HybridPredicate {

	private HybridPredicate wrappee;

	HybridPredicateWrapper(HybridPredicate wrappee) {
		if (wrappee instanceof HybridPredicateWrapper)
			throw new IllegalArgumentException("wrappe: shouldn't be an HybridPredicateWrapper.");
		this.wrappee = wrappee;
	}

	@Override
	public String accept(FormatVisitor visitor) {
		return wrappee.accept(visitor);
	}

	@Override
	public Predicate accept(ModelVisitor visitor) {
		return wrappee.accept(visitor);
	}

	@Override
	public OWLClass asConcept() {
		return wrappee.asConcept();
	}

	@Override
	public OWLProperty<?, ?> asRole() {
		return wrappee.asRole();
	}

	boolean changeWrapee(HybridPredicate wrappee) {
		if (wrappee instanceof HybridPredicateWrapper)
			throw new IllegalArgumentException("wrappe: shouldn't be an HybridPredicateWrapper.");
		final boolean changed = !wrappee.equals(this.wrappee);
		this.wrappee = wrappee;
		return changed;
	}

	@Override
	public int getArity() {
		return wrappee.getArity();
	}

	@Override
	public String getSignature() {
		return wrappee.getSignature();
	}

	@Override
	public String getSymbol() {
		return wrappee.getSymbol();
	}

	HybridPredicate getWrapee() {
		return wrappee;
	}

	@Override
	public boolean isConcept() {
		return wrappee.isConcept();
	}

	@Override
	public boolean isRole() {
		return wrappee.isRole();
	}

	@Override
	public String toString() {
		return wrappee.toString();
	}

}