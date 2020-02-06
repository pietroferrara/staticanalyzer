package it.unive.dais.staticanalyzer.cfg.statement;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;

public class Assignment implements Expression {
	private Expression left, right;

	public Assignment(Expression left, Expression right) {
		this.left = left;
		this.right = right;
		
	}
	@Override
	public String toString() {
		return left.toString()+"="+right.toString();
	}
}
