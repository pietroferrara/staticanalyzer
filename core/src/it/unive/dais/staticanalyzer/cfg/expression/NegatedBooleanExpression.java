package it.unive.dais.staticanalyzer.cfg.expression;

public class NegatedBooleanExpression implements Expression {
	private Expression expression;

	public NegatedBooleanExpression(Expression exp) {
		this.expression = exp;
	}
	@Override
	public String toString() {
		return "! "+getExpression();
	}
	public Expression getExpression() {
		return expression;
	}
}
