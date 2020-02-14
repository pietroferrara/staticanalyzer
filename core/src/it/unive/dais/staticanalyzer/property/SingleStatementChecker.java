package it.unive.dais.staticanalyzer.property;

import java.util.Collection;

import it.unive.dais.staticanalyzer.abstractdomain.AbstractAnalysisState;
import it.unive.dais.staticanalyzer.api.Warning;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

public interface SingleStatementChecker {
	public Collection<Warning> check(Statement results, AbstractAnalysisState abstractAnalysisState);
}
