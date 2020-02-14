package it.unive.dais.staticanalyzer.cfg.statement;

import it.unive.dais.staticanalyzer.cfg.ParsedBlock;

public abstract class Statement extends ParsedBlock {

	public Statement(int line, int column) {
		super(line, column);
	}

	public abstract boolean isTerminatingStatement();

}
