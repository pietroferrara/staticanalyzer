package it.unive.dais.staticanalyzer.cfg.statement;

public class SkipStatement implements Statement {
	public SkipStatement() { }
	
	@Override
	public String toString() {
		return "skip";
	}
	
	@Override
	public boolean isTerminatingStatement() {
		return false;
	}
}
