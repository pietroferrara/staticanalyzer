package it.unive.dais.staticanalyzer.cfg.expression;

/**
 * An integer constant value (true or false)
 * @author Pietro Ferrara
 *
 */
public class IntegerConstant extends Constant {
	private long value;
	
	/**
	 * 
	 * @param value the integer value
	 * @param line the line where the integer constant begins
	 * @param column the column where the integer constant begins
	 */
	public IntegerConstant(long value, int line, int column) {
		super(line, column);
		this.value = value;
	}
	@Override
	public String toString() {
		return String.valueOf(getValue());
	}
	/**
	 * 
	 * @return the integer value of the constant
	 */
	public long getValue() {
		return value;
	}
}
