package it.unive.dais.staticanalyzer.cfg.expression;

import it.unive.dais.staticanalyzer.cfg.Type;

/**
 * An assignable expression like a variable identifier or an array access
 * @author Pietro Ferrara
 *
 */
public abstract class AssignableExpression extends Expression {
	/**
	 * 
	 * @param line the line where the negated expression begins
	 * @param column the column where the negated expression begins
	 */
	public AssignableExpression(int line, int column) {
		super(line, column);
	}
}