package it.unive.dais.staticanalyzer.property;

import java.util.Collection;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.api.Warning;
import it.unive.dais.staticanalyzer.cfg.CFGAnalysisResults;

/**
 * The class representing a property checker, that given the abstract results of the analysis returns
 * a set of warnings.
 * @author Pietro Ferrara
 *
 */
public interface Checker {
	/**
	 * 
	 * @param <T> the type of the abstract domain applied by the analysis
	 * @param results the results of the static analysis
	 * @return a set of warnings pointing to program points that might not respect the property of interest
	 */
	public <T extends Lattice<T> & SemanticDomain<T>> Collection<Warning> check(CFGAnalysisResults<T> results);

}
