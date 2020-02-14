package it.unive.dais.staticanalyzer.cfg.expression;

public class BooleanExpression extends Expression {
	private Expression left, right;
	private String operator;

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
	public BooleanExpression negate() {
		switch(getOperator()) {
			case "&&": return new BooleanExpression(new NegatedBooleanExpression(this.getLeft(), this.getLeft().getLine(), this.getLeft().getColumn()), new NegatedBooleanExpression(this.getRight(), this.getRight().getLine(), this.getRight().getColumn()), "||", this.getLine(), this.getColumn());
			case "||": return new BooleanExpression(new NegatedBooleanExpression(this.getLeft(), this.getLeft().getLine(), this.getLeft().getColumn()), new NegatedBooleanExpression(this.getRight(), this.getRight().getLine(), this.getRight().getColumn()), "&&", this.getLine(), this.getColumn());
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
