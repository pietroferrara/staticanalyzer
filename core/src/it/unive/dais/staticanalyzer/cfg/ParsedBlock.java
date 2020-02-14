package it.unive.dais.staticanalyzer.cfg;

public abstract class ParsedBlock {
	private final int line, column;
	
	public ParsedBlock(int line, int column) {
		this.line = line;
		this.column = column;
	}
	
	public int getLine() {
		return line;
	}
	
	public int getColumn() {
		return column;
	}

}
