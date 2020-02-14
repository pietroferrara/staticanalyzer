package it.unive.dais.staticanalyzer.cfg.expression;

public class BinaryArithmeticExpression extends Expression {
	private Expression left, right;
	private String operator;

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
	public String getOperator() {
		return operator;
	}
	public Expression getLeft() {
		return left;
	}
	public Expression getRight() {
		return right;
	}
}
