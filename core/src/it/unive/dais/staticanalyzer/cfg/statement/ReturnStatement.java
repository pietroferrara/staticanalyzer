package it.unive.dais.staticanalyzer.cfg.statement;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;

public class ReturnStatement extends Assignment {

	public ReturnStatement(Expression returnedExpression, int line, int column) {
		super(new VariableIdentifier("<ret>", line, column), returnedExpression, line, column);
	}
	
	@Override
	public String toString() {
		return "return "+ (super.getExpression()!=null?super.getExpression().toString() : "")+";";
	}
	
	@Override
	public boolean isTerminatingStatement() {
		return true;
	}
}
