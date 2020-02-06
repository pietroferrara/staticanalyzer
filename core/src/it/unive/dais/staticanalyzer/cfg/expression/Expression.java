package it.unive.dais.staticanalyzer.cfg.expression;

import it.unive.dais.staticanalyzer.cfg.statement.Statement;

public interface Expression extends Statement {
	
	@Override
	default boolean isTerminatingStatement() {
		return false;
	}
}
