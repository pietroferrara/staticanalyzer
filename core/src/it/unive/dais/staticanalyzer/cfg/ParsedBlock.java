package it.unive.dais.staticanalyzer.cfg;

/**
 * A class representing a parsed block
 * @author Pietro Ferrara
 *
 */
public abstract class ParsedBlock {
	private final int line, column;
	
	/**
	 * 
	 * @param line the line where the parsed block starts
	 * @param column the column where the parsed block starts
	 */
	public ParsedBlock(int line, int column) {
		this.line = line;
		this.column = column;
	}
	
	/**
	 * 
	 * @return the line where the parsed block starts
	 */
	public int getLine() {
		return line;
	}
	
	/**
	 * 
	 * @return the column where the parsed block starts
	 */
	public int getColumn() {
		return column;
	}

}
