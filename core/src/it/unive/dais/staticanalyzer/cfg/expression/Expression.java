package it.unive.dais.staticanalyzer.cfg.expression;

import it.unive.dais.staticanalyzer.cfg.statement.Statement;

/**
 * The class representing a generic expression
 * @author Pietro Ferrara
 *
 */
public abstract class Expression extends Statement {
	/**
	 * 
	 * @param line the line where the expression begins
	 * @param column the column where the expression begins
	 */
	public Expression(int line, int column) {
		super(line, column);
	}

	@Override
	public boolean isTerminatingStatement() {
		return false;
	}
}
