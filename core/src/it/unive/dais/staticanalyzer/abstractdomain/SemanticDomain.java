package it.unive.dais.staticanalyzer.abstractdomain;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

/**
 * The interface representing the semantic operators of an abstract domain.
 * @author Pietro Ferrara
 *
 * @param <T> the concrete type of the semantic domain
 */
public interface SemanticDomain<T extends SemanticDomain<T>> {
	
	/**
	 * The abstract semantics of a statement
	 * @param st the statement under analysis
	 * @return the abstract state after computing the semantics of st on this
	 */
	public T smallStepSemantics(Statement st);

	/**
	 * The abstract assumption that an expression holds
	 * @param currentExpression the expression to be assumed
	 * @return the abstract state after assuming that expr holds on this
	 */
	public T assume(Expression currentExpression);
	
	/**
	 * Check if an expression holds on this abstract state
	 * @param currentExpression the expression to be checked
	 * @return true iff expr is always true in this abstract state 
	 */
	public boolean satisfy(Expression currentExpression);
}
