package it.unive.dais.staticanalyzer.cfg.expression;

public class NumericalComparisonExpression extends Expression {
	private Expression left, right;
	private String operator;

	public NumericalComparisonExpression(Expression left, Expression right, String operator, int line, int column) {
		super(line, column);
		this.left = left;
		this.right = right;
		this.operator = operator;
	}
	
	@Override
	public String toString() {
		return getLeft().toString()+" "+getOperator()+getRight().toString();
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
