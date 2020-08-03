package it.unive.dais.staticanalyzer.cfg.expression;

/**
 * A class representing a numerical comparison <left> <op> <right>
 * @author Pietro Ferrara
 *
 */
public class NumericalComparisonExpression extends Expression {
	private Expression left, right;
	private String operator;

	/**
	 * 
	 * @param left the left part of the numerical comparison
	 * @param right the right part of the numerical comparison
	 * @param operator the operator of the numerical comparison
	 * @param line the line where the numerical comparison begins
	 * @param column the column where the numerical comparison begins
	 */
	public NumericalComparisonExpression(Expression left, Expression right, String operator, int line, int column) {
		super(line, column);
		this.left = left;
		this.right = right;
		this.operator = operator;
	}
	
	@Override
	public String toString() {
		return getLeft().toString()+" "+getOperator()+" "+getRight().toString();
	}

	/**
	 * 
	 * @return the left part of the numerical comparison
	 */
	public Expression getLeft() {
		return left;
	}

	/**
	 * 
	 * @return the right part of the numerical comparison
	 */
	public Expression getRight() {
		return right;
	}

	/**
	 * 
	 * @return the operator of the numerical comparison
	 */
	public String getOperator() {
		return operator;
	}
	/**
	 * 
	 * @return the negation of the numerical comparison
	 */
	public Expression negate() {
		return new NumericalComparisonExpression(left, right, NumericalComparisonExpression.negateOperator(operator), this.getLine(), this.getColumn());
	}

	private static String negateOperator(String operator) {
		switch(operator) {
			case "<=": return ">";
			case ">=": return "<";
			case "<": return ">=";
			case ">": return "<=";
			case "==": return "!=";
			case "!=": return "==";
			default: throw new UnsupportedOperationException("Comparison operator "+operator+" not yet supported");
		}
	}

}
