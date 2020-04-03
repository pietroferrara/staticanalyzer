package it.unive.dais.staticanalyzer.abstractdomain.instances;

import java.util.HashMap;
import java.util.Map;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.abstractdomain.instances.utils.PrefixRepresentation;
import it.unive.dais.staticanalyzer.abstractdomain.instances.utils.Utils;
import it.unive.dais.staticanalyzer.cfg.expression.BinaryArithmeticExpression;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.StringConstant;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;
import it.unive.dais.staticanalyzer.cfg.statement.Assignment;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

public class StringPrefixDomain implements Lattice<StringPrefixDomain>, SemanticDomain<StringPrefixDomain> {

	HashMap<String, PrefixRepresentation> map;
	
	public StringPrefixDomain() {
		this.map = new HashMap<String, PrefixRepresentation>();
	}
	
	public StringPrefixDomain(HashMap<String, PrefixRepresentation> map) {
		this.map = new HashMap<String, PrefixRepresentation>(map);
	}
	
	private String getContent(Expression expr, HashMap<String, PrefixRepresentation> map) {
		if(expr instanceof StringConstant) {
			StringConstant str = (StringConstant)expr;
			return new String(str.toString());
		}
		
		if(expr instanceof VariableIdentifier) {
			VariableIdentifier vi = (VariableIdentifier)expr;
			
			PrefixRepresentation storedValue = map.get(vi.toString());

			if(storedValue == null) {
				return "*";
			} else {
				if(storedValue.bound == Utils.Bounds.TOP)
					return "*";
				
				if(storedValue.cutted)
					return new String(storedValue.prefix.toString() + "*");
				else 
					return new String(storedValue.prefix.toString());
			}
		}
		
		return "";
	}
	
	public String getResult(BinaryArithmeticExpression bae, HashMap<String, PrefixRepresentation> map) {
		Expression leftSide = bae.getLeft();
		Expression rightSide = bae.getRight();
		
		if(leftSide instanceof BinaryArithmeticExpression)
			return getResult((BinaryArithmeticExpression)leftSide, map) + getContent(rightSide, map);
		
		if(rightSide instanceof BinaryArithmeticExpression) 
			return getContent(leftSide, map) + getResult((BinaryArithmeticExpression)rightSide, map);
			
		return getContent(leftSide, map) + getContent(rightSide, map);
	}

	
	@Override
	public StringPrefixDomain smallStepSemantics(Statement st) {
		
		if(st instanceof Assignment) {
			Assignment ass = (Assignment)st;
			String key = ass.getAssignedVariable().getName();
			
			Expression expr = ass.getExpression();
			
			if(expr instanceof StringConstant) {
				String value = ((StringConstant)expr).toString();
				
				PrefixRepresentation toAdd = new PrefixRepresentation(value);
				
				HashMap<String, PrefixRepresentation> HMtoAdd = new HashMap<String, PrefixRepresentation>(this.map);
				HMtoAdd.put(key, toAdd);
				
				return new StringPrefixDomain(HMtoAdd);
			}
			
			if(expr instanceof BinaryArithmeticExpression) {
				BinaryArithmeticExpression assignedExpr = (BinaryArithmeticExpression)expr;
			
				String res = getResult(assignedExpr, this.map);
				
				PrefixRepresentation pr;
				if(res.contains("*")) {
					res = res.split("\\*")[0];
					pr = new PrefixRepresentation(res);
					pr.cutted = true;
				} else {
					pr = new PrefixRepresentation(res);
				}
				
				HashMap<String, PrefixRepresentation> HMtoAdd = new HashMap<String, PrefixRepresentation>(this.map);
				
				HMtoAdd.put(key, pr);
				
				return new StringPrefixDomain(HMtoAdd);
			}
			
			
		}
		
		return this;
	}

	@Override
	public StringPrefixDomain assume(Expression currentExpression) {
		return this;
	}

	@Override
	public boolean satisfy(Expression currentExpression) {
		return false;
	}

	@Override
	public StringPrefixDomain lub(StringPrefixDomain other) {
		
		
		HashMap<String, PrefixRepresentation> hm = new HashMap<String, PrefixRepresentation>(map);	
		
		for (Map.Entry<String, PrefixRepresentation> entry : map.entrySet()) {
			String key = entry.getKey();
			PrefixRepresentation otherValue = other.map.get(key);
			PrefixRepresentation thisValue = entry.getValue();
			
			String res = "";
			int i = 0;
			
			while(i < thisValue.getLength() && i < otherValue.getLength() && thisValue.charAt(i) == otherValue.charAt(i)) {
				res += thisValue.charAt(i);
				i++;
			}
			
			PrefixRepresentation pr = new PrefixRepresentation(res);
			if(thisValue.cutted || otherValue.cutted || i < thisValue.getLength() - 1 || i < otherValue.getLength() - 1)
				pr.cutted = true;
			
			hm.put(key, pr);
		}
		
		
		return new StringPrefixDomain(hm);			
	}

	@Override
	public boolean lessOrEqual(StringPrefixDomain other) {
		
		boolean singleFlag = true;
		boolean generalFlag = true;
		
		for (Map.Entry<String, PrefixRepresentation> entry : map.entrySet()) {
			String key = entry.getKey();
			PrefixRepresentation otherValue = other.map.get(key);
			PrefixRepresentation thisValue = this.map.get(key);
			
			if(thisValue.bound != Utils.Bounds.BOTTOM){
				
				if(otherValue.getLength() <= thisValue.getLength()) {
				
					for(int i = 0; i < otherValue.getLength(); i++) {
						if(otherValue.charAt(i) != thisValue.charAt(i)) {
							singleFlag = false;
						}
					}
					
				} else {
					singleFlag = false;
				}
			}
			
			if(!singleFlag) 
				generalFlag = false;
			
		}
		
		return generalFlag;
	}

	@Override
	public StringPrefixDomain widening(StringPrefixDomain succ) {
		/* the domain respects the ascending chain condition (ACC), and we do not
		need to define a widening operator to ensure the convergence of the analysis */
		return null;
	}

	@Override
	public StringPrefixDomain bottom() {
		HashMap<String, PrefixRepresentation> hm = new HashMap<String, PrefixRepresentation>();
		
		for (Map.Entry<String, PrefixRepresentation> entry : map.entrySet()) {
			
			PrefixRepresentation pr = new PrefixRepresentation(entry.getValue().prefix);
			pr.bound = Utils.Bounds.BOTTOM;
			hm.put(entry.getKey(), pr);
			
		}
		
		StringPrefixDomain spd = new StringPrefixDomain(hm);
		
		return spd;
	}
	
	@Override
	public String toString() {
		String out = "";
		for (Map.Entry<String, PrefixRepresentation> entry : map.entrySet()) {
		    out += entry.getKey() + ": " + entry.getValue().toString() + "\n";
		}
		return out;
	}

}
