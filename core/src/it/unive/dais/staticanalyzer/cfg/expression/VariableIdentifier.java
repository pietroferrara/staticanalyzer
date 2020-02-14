package it.unive.dais.staticanalyzer.cfg.expression;

public class VariableIdentifier extends Expression {
	
	private final String id;

	public String getName() {
		return id;
	}
	public VariableIdentifier(String id, int line, int column) {
		super(line, column);
		this.id = id;
	}
	@Override
	public String toString() {
		return id;
	}

}
