package it.unive.dais.staticanalyzer.cfg.expression;

public class NumericalComparisonExpression implements Expression {
	private Expression left, right;
	private String operator;

	public NumericalComparisonExpression(Expression left, Expression right, String operator) {
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
		return new NumericalComparisonExpression(left, right, NumericalComparisonExpression.negateOperator(operator));
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
