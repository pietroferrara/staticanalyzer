package it.unive.dais.staticanalyzer.cfg.expression;
/**
 * A float constant value
 * @author Pietro Ferrara
 *
 */
public class FloatConstant extends Constant {
	private double value;
	/**
	 * 
	 * @param value the float value
	 * @param line the line where the float constant begins
	 * @param column the column where the float constant begins
	 */
	public FloatConstant(double value, int line, int column) {
		super(line, column);
		this.value = value;
	}
	@Override
	public String toString() {
		return String.valueOf(getValue());
	}
	/**
	 * 
	 * @return the float constant value
	 */
	public double getValue() {
		return value;
	}
}
