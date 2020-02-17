package it.unive.dais.staticanalyzer.property;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import it.unive.dais.staticanalyzer.abstractdomain.AbstractAnalysisState;
import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.api.Warning;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.NegatedBooleanExpression;
import it.unive.dais.staticanalyzer.cfg.statement.AssertStatement;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

public class AssertChecker implements SingleStatementChecker {

	public AssertChecker() { }

	@Override
	public Collection<Warning> check(Statement statement, AbstractAnalysisState state) {
		if(statement instanceof AssertStatement) {
			Expression exp = ((AssertStatement) statement).getExpression();
			if(! ((SemanticDomain) state.getSemanticDomainState()).satisfy(exp)) {
				Set<Warning> result = new HashSet<>();
				result.add(new Warning(statement.getLine(), statement.getColumn(), "This assert statement might not hold"));
				return result;
			}
		}
		return null;
		
	}

}
