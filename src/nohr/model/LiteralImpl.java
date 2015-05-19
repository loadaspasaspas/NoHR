package nohr.model;

public abstract class LiteralImpl implements Literal {
	
	protected Atom atom;
	
	public LiteralImpl(Atom atom) {
		this.atom = atom;
	}

	@Override
	public abstract NegativeLiteral asNegativeLiteral() throws ModelException;

	@Override
	public abstract PositiveLiteral asPositiveLiteral() throws ModelException;
	
	@Override
	public Atom getAtom() {
		return atom;
	}

	@Override
	public boolean isGrounded() {
		return atom.isGrounded();
	}

	@Override
	public abstract boolean isNegative();

	@Override
	public abstract boolean isPositive();

}
