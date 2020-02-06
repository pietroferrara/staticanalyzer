package it.unive.dais.staticanalyzer.cfg.statement;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;

public class ReturnStatement implements Statement {
	private Expression returnedExpression;

	public ReturnStatement(Expression returnedExpression) {
		this.returnedExpression = returnedExpression;
	}
	@Override
	public String toString() {
		return "return "+ (returnedExpression!=null?returnedExpression.toString() : "")+";";
	}
	
	@Override
	public boolean isTerminatingStatement() {
		return true;
	}
}
