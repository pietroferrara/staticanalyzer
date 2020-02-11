package it.unive.dais.staticanalyzer.cfg.statement;

import it.unive.dais.staticanalyzer.cfg.ParsedBlock;
import it.unive.dais.staticanalyzer.cfg.Type;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;

public class VariableDeclaration implements ParsedBlock, Statement {
	private VariableIdentifier id;
	private Type type;
	public VariableDeclaration(Type type, VariableIdentifier id) {
		super();
		this.type = type;
		this.id = id;
	}
	
	public VariableIdentifier getId() {
		return id;
	}

	@Override
	public String toString() {
		return type.toString()+" "+id.toString();
	}
	
	@Override
	public boolean isTerminatingStatement() {
		return false;
	}
}
