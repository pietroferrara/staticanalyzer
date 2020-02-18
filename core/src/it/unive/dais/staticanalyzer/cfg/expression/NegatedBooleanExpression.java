package it.unive.dais.staticanalyzer.cfg.expression;

/**
 * The negation of an expression ! <exp>
 * @author Pietro Ferrara
 *
 */
public class NegatedBooleanExpression extends Expression {
	private Expression expression;
	/**
	 * 
	 * @param exp the negated expression
	 * @param line the line where the negated expression begins
	 * @param column the column where the negated expression begins
	 */
	public NegatedBooleanExpression(Expression exp, int line, int column) {
		super(line, column);
		this.expression = exp;
	}
	@Override
	public String toString() {
		return "! "+getExpression();
	}
	/**
	 * 
	 * @return the expression that is negated
	 */
	public Expression getExpression() {
		return expression;
	}
}
