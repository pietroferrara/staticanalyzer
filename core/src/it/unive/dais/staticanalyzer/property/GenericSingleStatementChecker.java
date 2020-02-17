package it.unive.dais.staticanalyzer.property;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import it.unive.dais.staticanalyzer.api.Warning;
import it.unive.dais.staticanalyzer.cfg.CFGAnalysisResults;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

public class GenericSingleStatementChecker<T extends SingleStatementChecker> implements Checker{
	private T singleStatementChecker;
	
	public GenericSingleStatementChecker(T singleStatementChecker) {
		this.singleStatementChecker = singleStatementChecker;
	}

	@Override
	public Collection<Warning> check(CFGAnalysisResults results) {
		Set<Warning> result = new HashSet<>();
		for(Statement st : results.getCfg().getGraph().vertexSet()) {
			Collection<Warning> singlelineresults = singleStatementChecker.check(st, results.getEntryState(st));
			if(singlelineresults!=null && singlelineresults.size()>0)
				result.addAll(singlelineresults);
		}
		return result;
	}

}
