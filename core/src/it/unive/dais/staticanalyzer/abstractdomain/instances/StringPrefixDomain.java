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


/**
 * A domain that abstracts strings through their prefix.
 * 
 * @author Carlo Zen
 *
 */
public class StringPrefixDomain implements Lattice<StringPrefixDomain>, SemanticDomain<StringPrefixDomain> {

	HashMap<String, PrefixRepresentation> map;
	
	public StringPrefixDomain() {
		this.map = new HashMap<String, PrefixRepresentation>();
	}
	
	public StringPrefixDomain(HashMap<String, PrefixRepresentation> map) {
		this.map = new HashMap<String, PrefixRepresentation>(map);
	}
	
	/**
	 * @param expr: can be a StringConstant or a VariableIdentifier
	 * @param map: the hash map containing all the variables and their value
	 * @return a PrefixRepresentation instance containing the content of expr
	 */
	private PrefixRepresentation getContent(Expression expr, HashMap<String, PrefixRepresentation> map) {
		if(expr instanceof StringConstant) {
			StringConstant str = (StringConstant)expr;
			return new PrefixRepresentation(str.toString());
		}
		
		if(expr instanceof VariableIdentifier) {
			VariableIdentifier vi = (VariableIdentifier)expr;
			
			PrefixRepresentation storedValue = map.get(vi.toString());
			
			PrefixRepresentation pr = new PrefixRepresentation();
			
			if(storedValue == null || storedValue.bound == Utils.Bounds.TOP) {
				pr.cutted = true;
				pr.prefix = "";
				return pr;
			} else {
				if(storedValue.cutted) 
					pr.cutted = true;
					
				pr.prefix = storedValue.prefix.toString();
				return pr;
				
			}
		}
		
		return new PrefixRepresentation();
	}
	
	/**
	 * @param bae: a BinaryArithmeticExpression to evaluate
	 * @param map: the hash map containing all the variables and their value
	 * @return a PrefixRepresentation instance containing the result of bae
	 */
	public PrefixRepresentation getResult(BinaryArithmeticExpression bae, HashMap<String, PrefixRepresentation> map) {
		Expression leftSide = bae.getLeft();
		Expression rightSide = bae.getRight();
		
		if(leftSide instanceof BinaryArithmeticExpression)
			return PrefixRepresentation.add(getResult((BinaryArithmeticExpression)leftSide, map), getContent(rightSide, map));
		
		if(rightSide instanceof BinaryArithmeticExpression) 
			return PrefixRepresentation.add(getContent(leftSide, map), getResult((BinaryArithmeticExpression)rightSide, map));
			
		return PrefixRepresentation.add(getContent(leftSide, map), getContent(rightSide, map));
	}

	/**
	 * If the input statement is a StringConstant, the function simply adds an entry to the hash map.
	 * Otherwise, if st is a BinaryArithmeticExpression, the function evaluates the expression through 
	 * the getResult function and it updates the hash map accordingly.
	 * 
	 * If the input statement is not an Assignment no changes are done. 
	 */
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
			
				PrefixRepresentation res = getResult(assignedExpr, this.map);
				
				HashMap<String, PrefixRepresentation> HMtoAdd = new HashMap<String, PrefixRepresentation>(this.map);
				HMtoAdd.put(key, res);
				
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

	/**
	 * Given two prefixes, their lub is their longest common prefix
	 */
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
			
			if(thisValue.cutted || otherValue.cutted || 
					i < thisValue.getLength() - 1 || i < otherValue.getLength() - 1)
				pr.cutted = true;
			
			hm.put(key, pr);
		}
		
		
		return new StringPrefixDomain(hm);			
	}

	/**
	 * An abstract string S is less or equal than another abstract string T, if T is a prefix of S or if S is the
	 * bottom of the domain
	 */
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

	/**
	 * The domain respects the ascending chain condition (ACC), and we do not
	 * need to define a widening operator to ensure the convergence of the analysis 
	 */
	@Override
	public StringPrefixDomain widening(StringPrefixDomain succ) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
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
		StringPrefixDomain other = (StringPrefixDomain) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}

}
