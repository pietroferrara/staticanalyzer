package it.unive.dais.staticanalyzer.cfg;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.IntegerComponentNameProvider;

import it.unive.dais.staticanalyzer.AnalysisConstants;
import it.unive.dais.staticanalyzer.abstractdomain.AbstractAnalysisState;
import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.abstractdomain.generic.FunctionalDomain;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

/**
 * The class representing the results of the static analysis on a program represented by a 
 * control flow graph. For each statement of the cfg, we have an entry and an exit state.
 * @author Pietro Ferrara
 *
 * @param <T> the abstract domain of the analysis
 */
public class CFGAnalysisResults<T extends SemanticDomain<T> & Lattice<T>> extends FunctionalDomain<Statement, AbstractAnalysisState<T>, CFGAnalysisResults<T>> {
	private CFG cfg;
	AbstractAnalysisState<T> entryState;
	final static Logger logger = Logger.getLogger(CFGAnalysisResults.class.getName());
	

	CFGAnalysisResults(CFG cfg, AbstractAnalysisState<T> entryState) {
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
	
	/**
	 * 
	 * @param st a statement of the program
	 * @return the abstract state representing the entry state of the given statement
	 */
	public AbstractAnalysisState<T> getEntryState(Statement st) {
		return this.getState(st);
	}

	/**
	 * 
	 * @param st a statement of the program
	 * @return the abstract state representing the exit state of the given statement
	 */
	public AbstractAnalysisState<T> getExitState(Statement st) {
		return this.getState(st).smallStepSemantics(st);
	}
	
	/**
	 * Compute the fixpoint of the analysis
	 * @param <T> the exact type of the abstract domain
	 * @param cfg the control flow graph of the analyzed program
	 * @param entryState the entry state of the analysis
	 * @return the result of the analysis
	 */
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
		Collection<Statement> statements = cfg.getOrderedStatements(AnalysisConstants.statementOrdering);
		int size = statements.size();
		logger.fine("# statements:"+size);
		Map<Statement, AbstractAnalysisState<T>> poststates = new HashMap<>();
		int i = 1;
		for(Statement st : statements) {
			if(i%10==0)
				logger.fine("Analyzed "+i+" statements");
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


	/**
	 * 
	 * @return the control flow graph of the analyzed program
	 */
	public CFG getCfg() {
		return cfg;
	}
	
	/**
	 * Dump the results of the analysis to a dot file reporting for each statement the entry and exit state.
	 * @param output the path of the file to be dumped
	 * @throws IOException
	 */
	public void dumpToDotFile(String output) throws IOException {

		DOTExporter<Statement, DefaultWeightedEdge> exporter2 = new DOTExporter<Statement, DefaultWeightedEdge>(
				new IntegerComponentNameProvider<Statement>(),

				new ComponentNameProvider<Statement>() {

					@Override
					public String getName(Statement component) {
						return "Entry state:\n"+getEntryState(component)+"\n"
								+component+
								"\nExit state:\n"+(getEntryState(component)==null ? "_|_" : getEntryState(component).smallStepSemantics(component));
					}
					
				},
				new ComponentNameProvider<DefaultWeightedEdge>() {

					@Override
					public String getName(DefaultWeightedEdge component) {
						Boolean b;
						try {
							b = CFG.getBooleanFromWeight(getCfg().getGraph().getEdgeWeight(component));
						} catch (ParsingException e) {
							return "<error>";
						}
						if(b==null) return "";
						else return String.valueOf(b.booleanValue());
					}
					
				}
		);
		try(FileWriter writer = new FileWriter(output)) {
			exporter2.exportGraph(getCfg().getGraph(), writer);
		}
		
	}

}
