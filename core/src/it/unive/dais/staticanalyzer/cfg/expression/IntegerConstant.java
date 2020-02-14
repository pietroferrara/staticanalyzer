package it.unive.dais.staticanalyzer.cfg.expression;

public class IntegerConstant extends Constant {
	private long value;
	public IntegerConstant(long value, int line, int column) {
		super(line, column);
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
