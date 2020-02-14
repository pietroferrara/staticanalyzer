package it.unive.dais.staticanalyzer.cfg.statement;

public class SkipStatement extends Statement {
	public SkipStatement(int line, int column) {
		super(line, column);
	}
	
	@Override
	public String toString() {
		return "skip";
	}
	
	@Override
	public boolean isTerminatingStatement() {
		return false;
	}
}
