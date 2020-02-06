package it.unive.dais.staticanalyzer.cfg.statement;

import it.unive.dais.staticanalyzer.cfg.ParsedBlock;

public interface Statement extends ParsedBlock {

	boolean isTerminatingStatement();

}
