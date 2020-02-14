package it.unive.dais.staticanalyzer;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;

public class AnalysisConstants {
	public static final int WIDENING_LIMIT = 2;//Number of iterations of the fixpoint algorithm before applying widening
	
	private static String forgetVariable = "random";//Special variable name representing a random value
	
	public static boolean isForget(Expression e) {
		if(e instanceof VariableIdentifier)
			return forgetVariable.equals(((VariableIdentifier) e).getName());
		else return false;
	}
}
