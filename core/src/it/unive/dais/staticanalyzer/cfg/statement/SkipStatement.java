package it.unive.dais.staticanalyzer.cfg.statement;

/**
 * A skip statement, that is, a statement that does nothing (usually useful to represent joint points
 * in the control flow graph)
 * @author Pietro Ferrara
 *
 */
public class SkipStatement extends Statement {
	/**
	 * 
	 * @param line the line where the skip statement begins
	 * @param column the column where the skip statement begins
	 */
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
