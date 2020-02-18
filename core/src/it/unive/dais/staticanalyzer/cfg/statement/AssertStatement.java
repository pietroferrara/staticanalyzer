package it.unive.dais.staticanalyzer.cfg.statement;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;

/**
 * An assert statement (assert <exp>)
 * @author Pietro Ferrara
 *
 */
public class AssertStatement extends SkipStatement {
	//Assert statements are ignored during the execution, they are interesting only to check if something holds through the static analyzer after the fixpoint computation

	private Expression expression;
	
	/**
	 * 
	 * @param expression the asserted expression
	 * @param line the line where the assert statement begins
	 * @param column the column where the assert statement begins
	 */
	public AssertStatement(Expression expression, int line, int column) {
		super(line, column);
		this.expression = expression;
	}
	
	@Override
	public String toString() {
		return "assert "+getExpression().toString();
	}

	/**
	 * 
	 * @return the asserted expression
	 */
	public Expression getExpression() {
		return expression;
	}

}
