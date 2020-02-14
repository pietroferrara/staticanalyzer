package it.unive.dais.staticanalyzer.cfg.expression;

public class StringConstant extends Constant {
	private String value;
	public StringConstant(String value, int line, int column) {
		super(line, column);
		this.value = value;
	}
	@Override
	public String toString() {
		return "\""+value+"\"";
	}
}
