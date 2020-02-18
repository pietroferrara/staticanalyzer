package it.unive.dais.staticanalyzer.property;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import it.unive.dais.staticanalyzer.abstractdomain.AbstractAnalysisState;
import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.api.Warning;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.statement.AssertStatement;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

/**
 * Check for each assert statement (assert <exp>) if the asserted expression <exp> definitely 
 * holds on the entry abstract state.
 * @author Pietro Ferrara
 *
 */
public class AssertChecker implements SingleStatementChecker {

	/**
	 * 
	 */
	public AssertChecker() { }

	@Override
	public <T extends Lattice<T> & SemanticDomain<T>> Collection<Warning> check(Statement statement, AbstractAnalysisState<T> state) {
		if(statement instanceof AssertStatement) {
			Expression exp = ((AssertStatement) statement).getExpression();
			if(! state.getSemanticDomainState().satisfy(exp)) {
				Set<Warning> result = new HashSet<>();
				result.add(new Warning(statement.getLine(), statement.getColumn(), "This assert statement might not hold"));
				return result;
			}
		}
		return null;
		
	}

}
