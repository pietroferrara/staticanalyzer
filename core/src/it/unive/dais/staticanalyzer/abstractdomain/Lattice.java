package it.unive.dais.staticanalyzer.abstractdomain;

/**
 * The interface representing a lattice structure.
 * @author Pietro Ferrara
 *
 * @param <T> The concrete type of the lattice
 */
public interface Lattice<T extends Lattice<T>> {

	/**
	 * Least upper bound operator
	 * @param other another element of the domain
	 * @return the least upper bound of this and other
	 */
	public T lub(T other);
	
	/**
	 * Less or equal comparison
	 * @param other another element of the domain
	 * @return true iff this is less than or equal to other
	 */
	public boolean lessOrEqual(T other);
	
	/**
	 * Widening operator
	 * @param succ the value of the domain at the next iteration of the fixpoint algorithm
	 * @return the widening of this with succ
	 */
	public T widening(T succ);
	
	/**
	 * 
	 * @return the bottom value of the domain
	 */
	public T bottom();
	
}
