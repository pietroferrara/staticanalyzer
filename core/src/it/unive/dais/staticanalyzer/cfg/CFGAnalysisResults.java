package it.unive.dais.staticanalyzer.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;

import it.unive.dais.staticanalyzer.AnalysisConstants;
import it.unive.dais.staticanalyzer.abstractdomain.AbstractAnalysisState;
import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.abstractdomain.generic.FunctionalDomain;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

public class CFGAnalysisResults<T extends SemanticDomain<T> & Lattice<T>> extends FunctionalDomain<Statement, AbstractAnalysisState<T>, CFGAnalysisResults<T>> {
	private CFG cfg;
	AbstractAnalysisState<T> entryState;
	
	public CFGAnalysisResults(CFG cfg, AbstractAnalysisState<T> entryState) {
		super(entryState);
		this.cfg = cfg;
		this.entryState = entryState;
		function.put(cfg.getEntryPoint(), entryState);
	}

	
	private CFGAnalysisResults(CFG cfg, Map<Statement, AbstractAnalysisState<T>> function) {
		super(function.get(cfg.getEntryPoint()));
		this.cfg = cfg;
		this.function = function;
	}
	
	public AbstractAnalysisState<T> getEntryState(Statement st) {
		return this.getState(st);
	}

	public AbstractAnalysisState<T> getExitState(Statement st) {
		return this.getState(st).smallStepSemantics(st);
	}
	
	public static <T extends SemanticDomain<T> & Lattice<T>> CFGAnalysisResults<T> computeFixpoint(CFG cfg, AbstractAnalysisState<T> entryState) {
		CFGAnalysisResults<T> prevIteration = new CFGAnalysisResults<T>(cfg, entryState).singleIteration();
		CFGAnalysisResults<T> nextIteration = prevIteration.singleIteration();
		int iteration = 0;
		while(! nextIteration.lessOrEqual(prevIteration)) {
			if(iteration <= AnalysisConstants.WIDENING_LIMIT)
				prevIteration = prevIteration.lub(nextIteration);
			else prevIteration = prevIteration.widening(nextIteration);
			nextIteration = prevIteration.singleIteration();
		}
		return nextIteration;
		
	}
	
	private CFGAnalysisResults<T> singleIteration() {
		Set<Statement> statements = getCfg().vertexSet();
		Map<Statement, AbstractAnalysisState<T>> poststates = new HashMap<>();
		for(Statement st : statements)
			if(function.containsKey(st) && function.get(st) != null)
					poststates.put(st, function.get(st).smallStepSemantics(st));
		return computeNewPrestatesFromPostStates(poststates);
	}

	private CFGAnalysisResults<T> computeNewPrestatesFromPostStates(Map<Statement, AbstractAnalysisState<T>> poststates) {
		Map<Statement, AbstractAnalysisState<T>> newPrestates = new HashMap<>(); 
		function.put(getCfg().getEntryPoint(), entryState);
		for(Statement st : getCfg().vertexSet()) {
			AbstractAnalysisState<T> state = getCfg().getEntryPoint().equals(st) ? entryState : null;
			for(DefaultWeightedEdge edge : this.getCfg().incomingEdgesOf(st)) {
				Statement source = getCfg().getEdgeSource(edge);
				AbstractAnalysisState<T> newState = poststates.get(source);
				if(newState!=null)
					state = state==null ? newState : newState.lub(state);
			}
			newPrestates.put(st, state);
		}
		return new CFGAnalysisResults<T>(getCfg(), newPrestates);
	}


	@Override
	public CFGAnalysisResults<T> bottom() {
		CFGAnalysisResults<T> result = new CFGAnalysisResults<T>(getCfg(), valueDomain);
		result.function = null;
		return result;
	}


	public CFG getCfg() {
		return cfg;
	}

}
