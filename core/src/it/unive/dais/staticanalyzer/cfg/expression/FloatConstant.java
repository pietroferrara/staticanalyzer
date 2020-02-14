package it.unive.dais.staticanalyzer.cfg.expression;

public class FloatConstant extends Constant {
	private double value;
	public FloatConstant(double value, int line, int column) {
		super(line, column);
		this.value = value;
	}
	@Override
	public String toString() {
		return String.valueOf(getValue());
	}
	public double getValue() {
		return value;
	}
}
