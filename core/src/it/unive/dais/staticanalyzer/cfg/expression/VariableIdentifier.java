package it.unive.dais.staticanalyzer.cfg.expression;

public class VariableIdentifier implements Expression {
	
	private final String id;

	public String getName() {
		return id;
	}
	public VariableIdentifier(String id) {
		super();
		this.id = id;
	}
	@Override
	public String toString() {
		return id;
	}

}
