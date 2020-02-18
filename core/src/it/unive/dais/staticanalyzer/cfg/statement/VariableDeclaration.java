package it.unive.dais.staticanalyzer.cfg.statement;

import it.unive.dais.staticanalyzer.cfg.Type;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;

/**
 * A variable declaration (<type> <var>)
 * @author Pietro Ferrara
 *
 */
public class VariableDeclaration extends Statement {
	private VariableIdentifier id;
	private Type type;
	/**
	 * 
	 * @param type the type of the declared variable
	 * @param id the name of the variable
	 * @param line the line where the variable declaration begins
	 * @param column the column where the variable declaration begins
	 */
	public VariableDeclaration(Type type, VariableIdentifier id, int line, int column) {
		super(line, column);
		this.type = type;
		this.id = id;
	}
	/**
	 * 
	 * @return the name of the declared variable
	 */
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

	/**
	 * 
	 * @return the type of the declared variable
	 */
	public Type getType() {
		return type;
	}
}
