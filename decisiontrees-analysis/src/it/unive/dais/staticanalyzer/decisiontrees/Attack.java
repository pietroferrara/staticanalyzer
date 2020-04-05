package it.unive.dais.staticanalyzer.decisiontrees;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Attack {
	private double lowerBound, upperBound, delta;
	private long cost, line;
	
	private Attack(double lowerBound, double upperBound, double delta, long cost, long line) {
		super();
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.delta = delta;
		this.cost = cost;
		this.line = line;
	}

	public double getLowerBound() {
		return lowerBound;
	}

	public double getUpperBound() {
		return upperBound;
	}

	public double getDelta() {
		return delta;
	}

	public long getCost() {
		return cost;
	}

	public long getLine() {
		return line;
	}




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (cost ^ (cost >>> 32));
		long temp;
		temp = Double.doubleToLongBits(delta);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (line ^ (line >>> 32));
		temp = Double.doubleToLongBits(lowerBound);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(upperBound);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attack other = (Attack) obj;
		if (cost != other.cost)
			return false;
		if (Double.doubleToLongBits(delta) != Double.doubleToLongBits(other.delta))
			return false;
		if (line != other.line)
			return false;
		if (Double.doubleToLongBits(lowerBound) != Double.doubleToLongBits(other.lowerBound))
			return false;
		if (Double.doubleToLongBits(upperBound) != Double.doubleToLongBits(other.upperBound))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Attack [lowerBound=" + lowerBound + ", upperBound=" + upperBound + ", delta=" + delta + ", cost=" + cost
				+ ", line=" + line + "]";
	}
	public static long readJSONAttackerBudget(String attacker) throws FileNotFoundException, IOException, ParseException {

        JSONParser parser = new JSONParser();
        try (Reader reader = new FileReader(attacker)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            return Long.parseLong(jsonObject.get("budget").toString());
        }
	}
	
	public static Map<Integer, Attack> readJSONAttacker(String attacker, List<String> header) throws FileNotFoundException, IOException, ParseException {

		Map<Integer, Attack> result = new HashMap<>();
		
        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(attacker)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);

            JSONArray attacks = (JSONArray) jsonObject.get("attacks");
            for(Object object_single_attack : attacks) {
            	JSONObject single_attack = (JSONObject) object_single_attack;
            	if(single_attack.keySet().size()!=1)
            		throw new UnsupportedOperationException("Only one key per attack");
            	String feature = (String) single_attack.keySet().iterator().next();
            	JSONArray inner_single_attack = (JSONArray) single_attack.get(feature);
            	if(inner_single_attack.size()!=1)
            		throw new UnsupportedOperationException("Only one entry per attack");
            	JSONObject map_single_attack = (JSONObject) inner_single_attack.iterator().next();
            	result.put(header.indexOf(feature)+1, parseSingleAttack(map_single_attack));
            }
        }
		return result;
	}

	private static Attack parseSingleAttack(JSONObject map_single_attack) {
		String pre = (String) map_single_attack.get("pre");
		double delta = Double.valueOf(map_single_attack.get("post").toString());
		long cost = (long) map_single_attack.get("cost");
		long line = (long) map_single_attack.get("line");
		String[] bounds = pre.substring(1, pre.length()-1).split(",");
		double lowerBound = Double.parseDouble(bounds[0]);
		double upperBound = Double.parseDouble(bounds[1]);
		return new Attack(lowerBound, upperBound, delta, cost, line);
	}
	

}
