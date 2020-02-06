package it.unive.dais.staticanalyzer.cfg.expression;

public class StringConstant implements Constant {
	private String value;
	public StringConstant(String value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "\""+value+"\"";
	}
}
