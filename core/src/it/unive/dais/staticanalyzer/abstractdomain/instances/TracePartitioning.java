package it.unive.dais.staticanalyzer.abstractdomain.instances;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.abstractdomain.generic.FunctionalDomain;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.statement.SkipStatement;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

public class TracePartitioning<Domain extends SemanticDomain<Domain> & Lattice<Domain>> 
		extends FunctionalDomain<Map<Integer, Integer>, Domain, TracePartitioning<Domain>> 
		implements SemanticDomain<TracePartitioning<Domain>>, Lattice<TracePartitioning<Domain>> {
	
	private final Map<Integer, Integer> tokenBounds;
	
	
	//tokenBounds represents for each code line how many "iterations" of a given code line we have to consider as distinct
	public TracePartitioning(Domain valueDomain, Map<Integer, Integer> tokenBounds) {
		super(valueDomain.bottom());
		this.tokenBounds = tokenBounds;
		Map<Integer, Integer> initialKey = new HashMap<>();
		for(Integer line : tokenBounds.keySet())
			initialKey.put(line, Integer.valueOf(0));
		this.function.put(initialKey, valueDomain);
	}

	@Override
	public TracePartitioning<Domain> bottom() {
		return new TracePartitioning<>(super.valueDomain, tokenBounds);
	}

	private final TracePartitioning<Domain> functionalLift(java.util.function.Function<Domain, Domain> lift, java.util.function.Function<Map<Integer, Integer>, Map<Integer, Integer>> retokenizer) {
		TracePartitioning<Domain> result = this.bottom();
		result.function = new HashMap<>();
		Set<Map<Integer, Integer> > keys = new HashSet<>(this.function.keySet());

		for(Map<Integer, Integer> key : keys) {
			Domain previous = result.function.get(retokenizer.apply(key));
			result.function.put(retokenizer.apply(key), previous == null ? lift.apply(this.getState(key)) : previous.lub(lift.apply(this.getState(key))));
		}
		return result;
	}
	
	@Override
	public TracePartitioning<Domain> smallStepSemantics(Statement st) {
		return functionalLift(o1 -> o1.smallStepSemantics(st), t1 -> {
			if(st instanceof SkipStatement)
				return t1;
			if(tokenBounds.keySet().contains(st.getLine())) {
				int line = st.getLine();
				int iterationBound = tokenBounds.get(line);
				if(t1.get(line) < iterationBound) {
					Map<Integer, Integer> result = new HashMap<>(t1);
					result.put(line, t1.get(line)+1);
					return result;
				}
				else return t1;
			}
			else return t1;
		});
	}

	@Override
	public TracePartitioning<Domain> assume(Expression currentExpression) {
		return functionalLift(o1 -> o1.assume(currentExpression), t1 -> t1);
	}

	@Override
	public boolean satisfy(Expression currentExpression) {
		for(Map<Integer, Integer> key : this.function.keySet())
			if(! this.getState(key).satisfy(currentExpression))
				return false;
		return true;
	}
	

	private String keyName;
	@Override
	public String toString() {
		String result = "TRACE PARTITIONING DOMAIN\n";
		for(Map<Integer, Integer> key : this.function.keySet()) {
			keyName = "";
			key.entrySet().forEach(o -> keyName+="(line "+o.getKey()+",iteration "+o.getValue()+") ");
			result+=keyName+"\n---------------\n"+this.function.get(key)+"\n---------------\n";
		}
		return result;
	}

}
