package it.unive.dais.staticanalyzer.cfg.expression;

import it.unive.dais.staticanalyzer.cfg.Type;

/**
 * The array access <variableId>[<index>]
 * @author Pietro Ferrara
 *
 */
public class ArrayAccessExpression extends AssignableExpression {
	private Expression index;
	private VariableIdentifier variableId;
	/**
	 * 
	 * @param variableId the variable identifier
	 * @param index the index
	 * @param line the line where the negated expression begins
	 * @param column the column where the negated expression begins
	 */
	public ArrayAccessExpression(VariableIdentifier variableId, Expression index, int line, int column) {
		super(line, column);
		this.variableId= variableId;
		this.index = index;
	}
	@Override
	public String toString() {
		return variableId+"["+index.toString()+"]";
	}
	public Expression getIndex() {
		return index;
	}
	public VariableIdentifier getVariableId() {
		return variableId;
	}
}