package it.unive.dais.staticanalyzer.cfg.statement;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;

/**
 * An assignment statement (<var> = <exp>)
 * @author Pietro Ferrara
 *
 */
public class Assignment extends Statement {
	private VariableIdentifier assigned;
	private Expression expression;

	/**
	 * 
	 * @param left the assigned variable
	 * @param right the expression assigned to the variable
	 * @param line the line where the assignment begins
	 * @param column the column where the assignment begins
	 */
	public Assignment(VariableIdentifier left, Expression right, int line, int column) {
		super(line, column);
		this.assigned = left;
		this.expression = right;
	}
	@Override
	public String toString() {
		return assigned.toString()+"="+getExpression().toString();
	}
	/**
	 * 
	 * @return the assigned expression
	 */
	public Expression getExpression() {
		return expression;
	}
	/**
	 * 
	 * @return the assigned variable
	 */
	public VariableIdentifier getAssignedVariable() {
		return assigned;
	}
	@Override
	public boolean isTerminatingStatement() {
		return false;
	}
}
