package it.unive.dais.staticanalyzer.cfg.expression;

public class BooleanConstant extends Constant {
	private boolean value;
	public BooleanConstant(boolean value, int line, int column) {
		super(line, column);
		this.value = value;
	}
	@Override
	public String toString() {
		return Boolean.toString(value);
	}
}
