package it.unive.dais.staticanalyzer.cfg.expression;

public class IntegerConstant implements Constant {
	private long value;
	public IntegerConstant(long value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return String.valueOf(getValue());
	}
	public long getValue() {
		return value;
	}
}
