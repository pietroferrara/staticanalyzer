package it.unive.dais.staticanalyzer.cfg.expression;

public class NullConstant extends Constant {
	public NullConstant(int line, int column) {	
		super(line, column);
	}
	@Override
	public String toString() {
		return "null";
	}
}
