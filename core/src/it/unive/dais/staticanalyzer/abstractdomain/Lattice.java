package it.unive.dais.staticanalyzer.abstractdomain;

public interface Lattice<T extends Lattice<T>> {

	public T lub(T other);
	public boolean lessOrEqual(T other);
	public T widening(T succ);
	public T bottom();
	
}
