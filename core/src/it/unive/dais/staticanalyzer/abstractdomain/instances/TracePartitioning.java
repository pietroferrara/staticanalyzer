package it.unive.dais.staticanalyzer.abstractdomain.instances;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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
	private final Set<Integer> joinPoints;
	
	
	//tokenBounds represents for each code line how many "iterations" of a given code line we have to consider as distinct
	public TracePartitioning(Domain valueDomain, Map<Integer, Integer> tokenBounds, Set<Integer> joinPoints) {
		super(valueDomain.bottom());
		this.tokenBounds = tokenBounds;
		Map<Integer, Integer> initialKey = new HashMap<>();
		for(Integer line : tokenBounds.keySet())
			initialKey.put(line, Integer.valueOf(0));
		this.function.put(initialKey, valueDomain);
		this.joinPoints = joinPoints;
	}

	@Override
	public TracePartitioning<Domain> bottom() {
		return new TracePartitioning<>(super.valueDomain, tokenBounds, joinPoints);
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
			return computeNewTokens(st, t1);
		});
	}

	private Map<Integer, Integer> computeNewTokens(Statement st, Map<Integer, Integer> t1) {
		if(joinPoints.contains(Integer.valueOf(st.getLine())))
			return new HashMap<>();
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
	}

	@Override
	public TracePartitioning<Domain> assume(Expression currentExpression) {
		return functionalLift(o1 -> o1.assume(currentExpression), t1 -> {
			return computeNewTokens(currentExpression, t1);
		});
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

	public void dumpToJSON(String path) throws IOException {
		JSONObject result = new JSONObject();
		try(FileWriter writer = new FileWriter(path)) {
			for(Map<Integer, Integer> key : this.function.keySet()) {
				JSONObject jsonkey = new JSONObject(key);
				result.put(jsonkey, this.function.get(key).toString());
			}	
			writer.write(result.toString());
		}
	}


	public static <T extends Lattice<T> & SemanticDomain<T>>  TracePartitioning loadFromJSON(T domain, apron.Environment env, String path) throws IOException {
		TracePartitioning result = new TracePartitioning(domain, new HashMap<>(), new HashSet<>());
		result.function = new HashMap<>();
        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(path)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            for(Object key : jsonObject.keySet()) {
            	Map<Integer, Integer> keyTranslated = getMapIntInt((String) key);
            	result.function.put(keyTranslated, Apron.getStateFromString(env, jsonObject.get(key).toString()));
            }
        }
        catch(Exception e) {throw new UnsupportedOperationException("Error while reading the JSON file", e);}
        return result;
	}

	private static Map<Integer, Integer> getMapIntInt(String key) {
		Map<Integer, Integer> result = new HashMap<>();
		for(String o : key.split(";")) {
			String[] s = o.replace("\"", "").replace("}", "").replace("{", "").split(":");
			result.put(Integer.valueOf(s[0]), Integer.valueOf(s[1]));
		}
		return result;
	}
	
}
