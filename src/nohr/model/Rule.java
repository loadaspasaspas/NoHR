package nohr.model;

import java.util.List;

public interface Rule {

	@Override
	public boolean equals(Object obj);
	
	public List<Literal> getBody();
	
	public PositiveLiteral getHead();
	
	public boolean isFact();
	
	@Override
	public String toString();
}
