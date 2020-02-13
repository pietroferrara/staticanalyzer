package it.unive.dais.staticanalyzer.cfg.expression;

public class FloatConstant implements Constant {
	private double value;
	public FloatConstant(double value) {
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
