package it.unive.dais.staticanalyzer.cfg.expression;

/**
 * A class representing binary arithmetic expressions <left> <op> <right>
 * @author Pietro Ferrara
 *
 */
public class BinaryArithmeticExpression extends Expression {
	private Expression left, right;
	private String operator;

	/**
	 * 
	 * @param left the left part of the arithmetic expression
	 * @param right the right part of the arithmetic expression
	 * @param operator the operator of the arithmetic expression
	 * @param line the line where the arithmetic expression begins
	 * @param column the column where the arithmetic expression begins
	 */
	public BinaryArithmeticExpression(Expression left, Expression right, String operator, int line, int column) {
		super(line, column);
		this.left = left;
		this.right = right;
		this.operator = operator;
	}
	@Override
	public String toString() {
		return getLeft().toString()+" "+getOperator()+getRight().toString();
	}
	/**
	 * 
	 * @return the operator of the arithmetic expression
	 */
	public String getOperator() {
		return operator;
	}
	
	/**
	 * 
	 * @return the left part of the arithmetic expression
	 */
	public Expression getLeft() {
		return left;
	}
	
	/**
	 * 
	 * @return the right part of the arithmetic expression
	 */
	public Expression getRight() {
		return right;
	}
}
