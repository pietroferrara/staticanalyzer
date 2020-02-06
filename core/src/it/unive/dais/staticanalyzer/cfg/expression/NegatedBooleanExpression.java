package it.unive.dais.staticanalyzer.cfg.expression;

public class NegatedBooleanExpression implements Expression {
	private Expression exp;

	public NegatedBooleanExpression(Expression exp) {
		this.exp = exp;
	}
	@Override
	public String toString() {
		return "! "+exp;
	}
}
