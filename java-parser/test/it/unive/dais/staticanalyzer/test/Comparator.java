package it.unive.dais.staticanalyzer.test;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.cli.ParseException;

import it.unive.dais.staticanalyzer.JavaCLI;
import it.unive.dais.staticanalyzer.api.AnalysisOptions;
import it.unive.dais.staticanalyzer.api.AnalysisResult;
import it.unive.dais.staticanalyzer.api.Warning;
import it.unive.dais.staticanalyzer.cfg.CFGAnalysisResults;

public class Comparator {
	final static Logger logger = Logger.getLogger(CFGAnalysisResults.class.getName());

	static class ComparisonResults {
		private Set<Warning> onlyInFirst = new HashSet<>(), onlyInSecond = new HashSet<>();
		
		public Set<Warning> getOnlyInFirst() {
			return onlyInFirst;
		}

		public Set<Warning> getOnlyInSecond() {
			return onlyInSecond;
		}

		ComparisonResults(Collection<Warning> first, Collection<Warning> second) {
			onlyInFirst.addAll(first);
			onlyInFirst.removeAll(second);
			onlyInSecond.addAll(second);
			onlyInSecond.removeAll(first);
		}
		
		boolean success() { return onlyInFirst.isEmpty() && onlyInSecond.isEmpty();}
	}
	
	public static boolean runAndCompare(AnalysisResult previousResult) throws IOException, ParseException {
		AnalysisOptions option = previousResult.getOptions();
		AnalysisResult nextResult = JavaCLI.runAnalysis(option);
		ComparisonResults comparisonResult = new ComparisonResults(previousResult.getWarnings(), nextResult.getWarnings());
		if(comparisonResult.success()) {
			logger.info("The analysis produced the same results");
			return true;
		}
		else {
			logger.warning("The analysis produced different results");
			Set<Warning> onlyPrevious = comparisonResult.getOnlyInFirst();
			Set<Warning> onlyNew = comparisonResult.getOnlyInSecond();
			if(! onlyPrevious.isEmpty()) {
				logger.warning(onlyPrevious.size()+" expected warnings are missing:");
				for(Warning w : onlyPrevious)
					logger.warning(w.toString());
			}
			if(! onlyNew.isEmpty()) {
				logger.warning(onlyNew.size()+" warnings are added:");
				for(Warning w : onlyNew)
					logger.warning(w.toString());
			}
			return false;
		}
	}
	
}
