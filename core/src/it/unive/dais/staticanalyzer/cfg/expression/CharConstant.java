package it.unive.dais.staticanalyzer.cfg.expression;

public class CharConstant extends Constant {
	private char value;
	public CharConstant(char value, int line, int column) {
		super(line, column);
		this.value = value;
	}
	@Override
	public String toString() {
		return "'"+String.valueOf(value)+"'";
	}
}
