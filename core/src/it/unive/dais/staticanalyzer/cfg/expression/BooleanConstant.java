package it.unive.dais.staticanalyzer.cfg.expression;

public class BooleanConstant implements Constant {
	private boolean value;
	public BooleanConstant(boolean value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return Boolean.toString(value);
	}
}
