package it.unive.dais.staticanalyzer.property;

import java.util.Collection;

import it.unive.dais.staticanalyzer.abstractdomain.AbstractAnalysisState;
import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.api.Warning;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

/**
 * A generic checker that check a property of interest for each program statement in isolation.
 * @author Pietro Ferrara
 *
 */
public interface SingleStatementChecker {
	/**
	 * @param <T> the type of the abstract domain applied by the analysis
	 * @param statement the statement on which the property is checked
	 * @param abstractAnalysisState the abstract entry state of the statement
	 * @return a set of warnings pointing to program points that might not respect the property of interest
	 */
	public <T extends Lattice<T> & SemanticDomain<T>> Collection<Warning> check(Statement statement, AbstractAnalysisState<T> abstractAnalysisState);
}
