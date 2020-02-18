package it.unive.dais.staticanalyzer.cfg.expression;

/**
 * A Boolean constant value (true or false)
 * @author Pietro Ferrara
 *
 */
public class BooleanConstant extends Constant {
	private boolean value;
	/**
	 * 
	 * @param value the Boolean value
	 * @param line the line where the Boolean constant begins
	 * @param column the column where the Boolean constant begins
	 */
	public BooleanConstant(boolean value, int line, int column) {
		super(line, column);
		this.value = value;
	}
	@Override
	public String toString() {
		return Boolean.toString(value);
	}
}
