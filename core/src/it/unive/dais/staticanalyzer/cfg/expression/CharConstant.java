package it.unive.dais.staticanalyzer.cfg.expression;

public class CharConstant implements Constant {
	private char value;
	public CharConstant(char value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "'"+String.valueOf(value)+"'";
	}
}
