package it.unive.dais.staticanalyzer.cfg.expression;

import it.unive.dais.staticanalyzer.cfg.statement.Statement;

public abstract class Expression extends Statement {
	
	public Expression(int line, int column) {
		super(line, column);
	}

	@Override
	public boolean isTerminatingStatement() {
		return false;
	}
}
