package it.unive.dais.staticanalyzer.cfg.statement;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;

/**
 * A return statement (return [<exp>])
 * @author Pietro Ferrara
 *
 */
public class ReturnStatement extends Assignment {

	/**
	 * 
	 * @param returnedExpression the returned expression (might be null)
	 * @param line the line where the return statement begins
	 * @param column the column where the return statement begins
	 */
	public ReturnStatement(Expression returnedExpression, int line, int column) {
		super(new VariableIdentifier("<ret>", line, column), returnedExpression, line, column);
	}
	
	@Override
	public String toString() {
		return "return "+ (super.getExpression()!=null?super.getExpression().toString() : "")+";";
	}
	
	@Override
	public boolean isTerminatingStatement() {
		return true;
	}
}
