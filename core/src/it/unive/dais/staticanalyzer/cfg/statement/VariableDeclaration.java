package it.unive.dais.staticanalyzer.cfg.statement;

import it.unive.dais.staticanalyzer.cfg.ParsedBlock;
import it.unive.dais.staticanalyzer.cfg.Type;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;

public class VariableDeclaration extends Statement {
	private VariableIdentifier id;
	private Type type;
	public VariableDeclaration(Type type, VariableIdentifier id, int line, int column) {
		super(line, column);
		this.type = type;
		this.id = id;
	}
	
	public VariableIdentifier getId() {
		return id;
	}

	@Override
	public String toString() {
		return getType().toString()+" "+id.toString();
	}
	
	@Override
	public boolean isTerminatingStatement() {
		return false;
	}

	public Type getType() {
		return type;
	}
}
