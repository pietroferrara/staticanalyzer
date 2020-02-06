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
		return left.toString()+" "+operator+right.toString();
	}
}
