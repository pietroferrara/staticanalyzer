package it.unive.dais.staticanalyzer.property;

import java.util.Collection;

import it.unive.dais.staticanalyzer.api.Warning;
import it.unive.dais.staticanalyzer.cfg.CFGAnalysisResults;

public interface Checker {
	public Collection<Warning> check(CFGAnalysisResults results);

}
