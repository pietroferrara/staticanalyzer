package it.unive.dais.staticanalyzer.cfg.expression;

/**
 * The null constant value
 * @author Pietro Ferrara
 *
 */
public class NullConstant extends Constant {
	/**
	 * 
	 * @param line the line where the null constant begins
	 * @param column the column where the null constant begins
	 */
	public NullConstant(int line, int column) {	
		super(line, column);
	}
	@Override
	public String toString() {
		return "null";
	}
}
