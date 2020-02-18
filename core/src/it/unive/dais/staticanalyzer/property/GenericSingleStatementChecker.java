package it.unive.dais.staticanalyzer.property;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.api.Warning;
import it.unive.dais.staticanalyzer.cfg.CFGAnalysisResults;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

/**
 * A checker that applies a SingleStatementChecker to each statement of the program
 * @author Pietro Ferrara
 *
 * @param <T> the type of the SingleStatementChecker
 */
public class GenericSingleStatementChecker<T extends SingleStatementChecker> implements Checker{
	private T singleStatementChecker;
	
	/**
	 * 
	 * @param singleStatementChecker the single statement checker to be applied to all the statements of the program
	 */
	public GenericSingleStatementChecker(T singleStatementChecker) {
		this.singleStatementChecker = singleStatementChecker;
	}

	@Override
	public <T1 extends Lattice<T1> & SemanticDomain<T1>> Collection<Warning> check(CFGAnalysisResults<T1> results) {
		Set<Warning> result = new HashSet<>();
		for(Statement st : results.getCfg().statements()) {
			Collection<Warning> singlelineresults = singleStatementChecker.check(st, results.getEntryState(st));
			if(singlelineresults!=null && singlelineresults.size()>0)
				result.addAll(singlelineresults);
		}
		return result;
	}

}
