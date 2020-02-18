package it.unive.dais.staticanalyzer.cfg.expression;
/**
 * A generic constant value
 * @author Pietro Ferrara
 *
 */
public abstract class Constant extends Expression {
	/**
	 * 
	 * @param line the line where the constant begins
	 * @param column the column where the constant begins
	 */
	public Constant(int line, int column) {
		super(line, column);
	}

}
