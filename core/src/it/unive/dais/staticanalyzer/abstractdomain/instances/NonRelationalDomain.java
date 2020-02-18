package it.unive.dais.staticanalyzer.abstractdomain.instances;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;

/**
 * The interface representing a non relational abstract domain
 * @author Pietro Ferrara
 *
 * @param <T> the exact type of the non relational abstract domain
 */
public interface NonRelationalDomain<T extends NonRelationalDomain<T>> extends Lattice<T> {

	/**
	 * Evaluate an expression
	 * @param expr the expression to be evaluated
	 * @param env the abstract state on which the expression is evaluated
	 * @return the abstract value resulting from the evaluation of the expression
	 */
	public T eval(Expression expr, Environment<T> env);
}
