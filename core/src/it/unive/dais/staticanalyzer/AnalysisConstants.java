package it.unive.dais.staticanalyzer;

import java.util.Comparator;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

/**
 * Constants used by the analysis.
 * @author Pietro Ferrara
 *
 */
public class AnalysisConstants {
	/**
	 * Number of iterations of the fixpoint algorithm before applying widening
	 */
	public static int WIDENING_LIMIT = 100;
	
	private static String forgetVariable = "random";//Special variable name representing a random value

	/**
	 * Comparator used by the ordering of statements in the fixpoint computation
	 */
	public static Comparator<Statement> statementOrdering = new Comparator<Statement>() {

		@Override
		public int compare(Statement arg0, Statement arg1) {
			if(arg0.getLine()!=arg1.getLine())
				return arg0.getLine() - arg1.getLine();
			if(arg0.getColumn()!=arg1.getColumn())
				return arg0.getColumn() - arg1.getColumn();
			return arg0.toString().compareTo(arg1.toString());
		}
	
	};
	
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
