package it.unive.dais.staticanalyzer.cfg.expression;

public class BooleanExpression implements Expression {
	private Expression left, right;
	private String operator;

	public BooleanExpression(Expression left, Expression right, String operator) {
		this.left = left;
		this.right = right;
		this.operator = operator;
	}
	@Override
	public String toString() {
		return getLeft().toString()+" "+getOperator()+getRight().toString();
	}
	public BooleanExpression negate() {
		switch(getOperator()) {
			case "&&": return new BooleanExpression(new NegatedBooleanExpression(this.getLeft()), new NegatedBooleanExpression(this.getRight()), "||");
			case "||": return new BooleanExpression(new NegatedBooleanExpression(this.getLeft()), new NegatedBooleanExpression(this.getRight()), "&&");
			default: throw new UnsupportedOperationException("Boolean operator "+getOperator()+" not yet supported");
		}
	}
	public Expression getLeft() {
		return left;
	}
	public Expression getRight() {
		return right;
	}
	public String getOperator() {
		return operator;
	}
}
