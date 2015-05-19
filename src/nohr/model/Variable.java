package nohr.model;

public interface Variable extends Term {
	
	@Override
	public boolean equals(Object obj);
	
	public String getSymbol();
	
	@Override
	public int hashCode();
	
	@Override
	public String toString();

}
