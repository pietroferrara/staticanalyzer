package it.unive.dais.staticanalyzer.cfg.statement;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;

public class AssertStatement extends SkipStatement {
	//Assert statements are ignore during the execution, they are interesting only to check if something holds through the static analyzer after the fixpoint computation

	private Expression expression;
	
	public AssertStatement(Expression expression, int line, int column) {
		super(line, column);
		this.expression = expression;
	}
	
	@Override
	public String toString() {
		return "assert "+getExpression().toString();
	}

	public Expression getExpression() {
		return expression;
	}

}
