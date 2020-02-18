package it.unive.dais.staticanalyzer.cfg.expression;

/**
 * The class representing Boolean expressions <left> <op> <right>
 * @author Pietro Ferrara
 *
 */
public class BooleanExpression extends Expression {
	private Expression left, right;
	private String operator;

	/**
	 * 
	 * @param left the left part of the Boolean expression
	 * @param right the right part of the Boolean expression
	 * @param operator the operator of the Boolean expression
	 * @param line the line where the Boolean expression begins
	 * @param column the column where the Boolean expression begins
	 */
	public BooleanExpression(Expression left, Expression right, String operator, int line, int column) {
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
	 * @return the negation of the Boolean expression
	 */
	public BooleanExpression negate() {
		switch(getOperator()) {
			case "&&": return new BooleanExpression(new NegatedBooleanExpression(this.getLeft(), this.getLeft().getLine(), this.getLeft().getColumn()), new NegatedBooleanExpression(this.getRight(), this.getRight().getLine(), this.getRight().getColumn()), "||", this.getLine(), this.getColumn());
			case "||": return new BooleanExpression(new NegatedBooleanExpression(this.getLeft(), this.getLeft().getLine(), this.getLeft().getColumn()), new NegatedBooleanExpression(this.getRight(), this.getRight().getLine(), this.getRight().getColumn()), "&&", this.getLine(), this.getColumn());
			default: throw new UnsupportedOperationException("Boolean operator "+getOperator()+" not yet supported");
		}
	}
	/**
	 * 
	 * @return the left part of the Boolean expression
	 */
	public Expression getLeft() {
		return left;
	}
	/**
	 * 
	 * @return the right part of the Boolean expression
	 */
	public Expression getRight() {
		return right;
	}
	/**
	 * 
	 * @return the operator of the Boolean expression
	 */
	public String getOperator() {
		return operator;
	}
}
