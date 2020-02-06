package it.unive.dais.staticanalyzer.cfg.expression;

public class NullConstant implements Constant {
	public NullConstant() {	}
	@Override
	public String toString() {
		return "null";
	}
}
