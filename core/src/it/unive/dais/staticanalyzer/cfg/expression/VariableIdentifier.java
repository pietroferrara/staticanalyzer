package it.unive.dais.staticanalyzer.cfg.expression;

/**
 * The class representing variables
 * @author Pietro Ferrara
 *
 */
public class VariableIdentifier extends AssignableExpression {
	
	private final String id;

	/**
	 * 
	 * @return the name of the variable
	 */
	public String getName() {
		return id;
	}
	/**
	 * 
	 * @param id the name of the variable
	 * @param line the line the name of the variable starts
	 * @param column the column the name of the variable starts
	 */
	public VariableIdentifier(String id, int line, int column) {
		super(line, column);
		this.id = id;
	}
	@Override
	public String toString() {
		return id;
	}

}
