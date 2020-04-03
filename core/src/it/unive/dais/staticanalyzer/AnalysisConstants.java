package it.unive.dais.staticanalyzer;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;

/**
 * Constants used by the analysis.
 * @author Pietro Ferrara
 *
 */
public class AnalysisConstants {
	/**
	 * Number of iterations of the fixpoint algorithm before applying widening
	 */
	public static int WIDENING_LIMIT = 20;
	
	private static String forgetVariable = "random";//Special variable name representing a random value
	
	/**
	 * 
	 * @param e an expression
	 * @return true iff the expression returns a random number.
	 */
	public static boolean isForget(Expression e) {
		if(e instanceof VariableIdentifier)
			return forgetVariable.equals(((VariableIdentifier) e).getName());
		else return false;
	}
}
