package it.unive.dais.staticanalyzer.property;

import java.util.Collection;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.api.Warning;
import it.unive.dais.staticanalyzer.cfg.CFGAnalysisResults;

public interface Checker {
	public <T extends Lattice<T> & SemanticDomain<T>> Collection<Warning> check(CFGAnalysisResults<T> results);

}
