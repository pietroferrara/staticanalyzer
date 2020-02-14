package it.unive.dais.staticanalyzer.cfg.expression;

public class NegatedBooleanExpression extends Expression {
	private Expression expression;

	public NegatedBooleanExpression(Expression exp, int line, int column) {
		super(line, column);
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
