package it.unive.dais.staticanalyzer.cfg.statement;

import it.unive.dais.staticanalyzer.cfg.ParsedBlock;

/**
 * A statement of the program
 * @author Pietro Ferrara
 *
 */
public abstract class Statement extends ParsedBlock {

	/**
	 * 
	 * @param line the line where the statement begins
	 * @param column the column where the statement begins
	 */
	public Statement(int line, int column) {
		super(line, column);
	}

	/**
	 * 
	 * @return true iff this is a terminating statement (e.g., return or throw)
	 */
	public abstract boolean isTerminatingStatement();

}
