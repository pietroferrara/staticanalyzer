package it.unive.dais.staticanalyzer.cfg;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
	final static Logger logger = Logger.getLogger(CFGAnalysisResults.class.getName());
	
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
		int iteration = 1;
		while(! nextIteration.lessOrEqual(prevIteration)) {
			logger.info("Iteration n."+iteration);
			if(iteration <= AnalysisConstants.WIDENING_LIMIT)
				prevIteration = prevIteration.lub(nextIteration);
			else prevIteration = prevIteration.widening(nextIteration);
			nextIteration = prevIteration.singleIteration();
			iteration++;
		}
		return nextIteration;
		
	}
	
	private CFGAnalysisResults<T> singleIteration() {
		Collection<Statement> statements = cfg.getOrderedStatements();
		int size = statements.size();
		logger.info("# statements:"+size);
		Map<Statement, AbstractAnalysisState<T>> poststates = new HashMap<>();
		int i = 1;
		for(Statement st : statements) {
			if(i%10==0)
				logger.info("Analyzed "+i+" statements");
			i++;
			AbstractAnalysisState<T> state = getEntryStateFromPoststates(poststates, st);
			if(function.containsKey(st) && function.get(st) != null)
				state = state==null? function.get(st) : state.lub(function.get(st));
			if(state!=null)
				poststates.put(st, state.smallStepSemantics(st));
		}
		return computeNewPrestatesFromPostStates(poststates);
	}

	private CFGAnalysisResults<T> computeNewPrestatesFromPostStates(Map<Statement, AbstractAnalysisState<T>> poststates) {
		Map<Statement, AbstractAnalysisState<T>> newPrestates = new HashMap<>(); 
		function.put(getCfg().getEntryPoint(), entryState);
		for(Statement st : getCfg().getGraph().vertexSet()) {
			AbstractAnalysisState<T> state = getEntryStateFromPoststates(poststates, st);
			newPrestates.put(st, state);
		}
		return new CFGAnalysisResults<T>(getCfg(), newPrestates);
	}


	private AbstractAnalysisState<T> getEntryStateFromPoststates(Map<Statement, AbstractAnalysisState<T>> poststates,
			Statement st) {
		AbstractAnalysisState<T> state = getCfg().getEntryPoint().equals(st) ? entryState : null;
		for(DefaultWeightedEdge edge : getCfg().getGraph().incomingEdgesOf(st)) {
			Statement source = getCfg().getGraph().getEdgeSource(edge);
			AbstractAnalysisState<T> newState = poststates.get(source);
			Boolean condition;
			try {
				condition = CFG.getBooleanFromWeight(getCfg().getGraph().getEdgeWeight(edge));
			} catch (ParsingException e) {
				throw new UnsupportedOperationException("Unkown edge weight", e);
			}
			if(newState!=null) {
				if(condition!=null) {
					if(condition.booleanValue())
						newState = newState.assumeExpressionHolds();
					else newState = newState.assumeExpressionDoesNotHold();
				}
				state = state==null ? newState : newState.lub(state);
			}
		}
		return state;
	}


	@Override
	public CFGAnalysisResults<T> bottom() {
		CFGAnalysisResults<T> result = new CFGAnalysisResults<T>(getCfg(), valueDomain);
		result.function = null;
		return result;
	}
	
	@Override
	public boolean isBottom() {
		return this.function == null;
	}


	public CFG getCfg() {
		return cfg;
	}

}
