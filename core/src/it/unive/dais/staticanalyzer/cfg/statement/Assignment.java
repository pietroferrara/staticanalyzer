package it.unive.dais.staticanalyzer.cfg.statement;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;

public class Assignment implements Statement {
	private VariableIdentifier assigned;
	private Expression expression;

	public Assignment(VariableIdentifier left, Expression right) {
		this.assigned = left;
		this.expression = right;
	}
	@Override
	public String toString() {
		return assigned.toString()+"="+getExpression().toString();
	}
	public Expression getExpression() {
		return expression;
	}
	public VariableIdentifier getAssignedVariable() {
		return assigned;
	}
	@Override
	public boolean isTerminatingStatement() {
		return false;
	}
}
