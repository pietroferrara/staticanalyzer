package it.unive.dais.staticanalyzer.abstractdomain.instances;

import java.util.HashMap;
import java.util.Map;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.abstractdomain.instances.utils.StringRepresentation;
import it.unive.dais.staticanalyzer.abstractdomain.instances.utils.Utils;
import it.unive.dais.staticanalyzer.cfg.expression.BinaryArithmeticExpression;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.StringConstant;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;
import it.unive.dais.staticanalyzer.cfg.statement.Assignment;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;



public class CharIsIncludedDomain implements Lattice<CharIsIncludedDomain>, SemanticDomain<CharIsIncludedDomain> {
	
	protected Map<String, StringRepresentation> map;
	
	public CharIsIncludedDomain(Map<String, StringRepresentation> map) {
		this.map = new HashMap<>(map);
	}
	
	public CharIsIncludedDomain() {
		this.map = new HashMap<>();
	}


	@Override
	public CharIsIncludedDomain lub(CharIsIncludedDomain other) {
		
		HashMap<String, StringRepresentation> HMtoAdd = new HashMap<String, StringRepresentation>();
		
		for (Map.Entry<String, StringRepresentation> entry : map.entrySet()) {
			String key = entry.getKey();
			
			if(other.map.get(key) != null) {
				String C = StringUtility.intersect(this.map.get(key).C, other.map.get(key).C);
				String MC = StringUtility.union(this.map.get(key).MC, other.map.get(key).MC);
				
				if(MC.contains("K") || MC.contains("k"))
					MC = "k";
				
				HMtoAdd.put(entry.getKey(), new StringRepresentation(key, C, MC));
			}
		}
		return new CharIsIncludedDomain(HMtoAdd);
	}

	@Override
	public boolean lessOrEqual(CharIsIncludedDomain other) {
		
		boolean singleFlag = true;
		boolean generalFlag = true;
		
		for (Map.Entry<String, StringRepresentation> entry : map.entrySet()) {
			String key = entry.getKey();
			StringRepresentation otherValue = other.map.get(key);
			StringRepresentation thisValue = this.map.get(key);
			
			if(thisValue.bound != Utils.Bounds.BOTTOM) {
				if(otherValue == null)
					singleFlag = false;
				
				for (int i = 0; i < otherValue.C.length(); i++) {
					//if C does not contain a char of other.C
			        if (thisValue.C.indexOf(otherValue.C.charAt(i)) == -1) {
			            singleFlag = false;
			        }
			    }
				
				for (int i = 0; i < thisValue.MC.length(); i++) {
					//if other.MC does not contain a char of MC
			        if (otherValue.MC.indexOf(thisValue.MC.charAt(i)) == -1) {
			        	singleFlag = false;
			        }
			    }
				
				if(!singleFlag) {
					generalFlag = false;
				}
			}
			
			
		}
		
		return generalFlag;
	}

	@Override
	public CharIsIncludedDomain widening(CharIsIncludedDomain succ) {
		return lub(succ);
	}

	@Override
	public CharIsIncludedDomain bottom() {
		HashMap<String, StringRepresentation> hm = new HashMap<String, StringRepresentation>();
		for (Map.Entry<String, StringRepresentation> entry : map.entrySet()) {
			StringRepresentation sr = new StringRepresentation(entry.getKey(), entry.getValue().C, entry.getValue().MC);
			hm.put(entry.getKey(), sr);
		}
		CharIsIncludedDomain charIsIncludedDomain = new CharIsIncludedDomain(hm);
		
		return charIsIncludedDomain;
	}
	
	
	private StringRepresentation getContent(Expression expr) {
		StringRepresentation res = new StringRepresentation();
		
		if(expr instanceof StringConstant) {
			StringConstant str = (StringConstant)expr;
			res.C = str.toString();
			res.MC = str.toString();
		}
		
		if(expr instanceof VariableIdentifier) {
			VariableIdentifier vi = (VariableIdentifier)expr;
			
			StringRepresentation storedValue = map.get(vi.toString());
			if(storedValue == null) {
				res.MC = "K";
				res.C = "";
			} else {
				res.C = storedValue.C;
				res.MC = storedValue.MC;
			}
		}
		
		return res;
	}


	@Override
	public CharIsIncludedDomain smallStepSemantics(Statement st) {
		
		if(st instanceof Assignment) {
			Assignment ass = (Assignment)st;
			String key = ass.getAssignedVariable().getName();
			
			Expression expr = ass.getExpression();
			
			if(expr instanceof StringConstant) {
				String value = ((StringConstant)expr).toString();
				
				StringRepresentation toAdd = new StringRepresentation(key, value, value);
				
				HashMap<String, StringRepresentation> HMtoAdd = new HashMap<String, StringRepresentation>(this.map);
				HMtoAdd.put(key, toAdd);
				
				return new CharIsIncludedDomain(HMtoAdd);
			}
			
			if(expr instanceof BinaryArithmeticExpression) {
				BinaryArithmeticExpression assignedExpr = (BinaryArithmeticExpression)expr;
				Expression leftSide = assignedExpr.getLeft();
				Expression rightSide = assignedExpr.getRight();
				
				StringRepresentation result = new StringRepresentation();
				
				while(leftSide instanceof BinaryArithmeticExpression || rightSide instanceof BinaryArithmeticExpression) {
					if(assignedExpr.getOperator().equals("+")) {
						if(leftSide instanceof BinaryArithmeticExpression) {
							
							result.add(getContent(rightSide));
							
							BinaryArithmeticExpression bae = (BinaryArithmeticExpression)leftSide;
							rightSide = bae.getRight();
							leftSide = bae.getLeft();
							
						} else {
							
							result.add(getContent(leftSide));
							
							BinaryArithmeticExpression bae = (BinaryArithmeticExpression)rightSide;
							rightSide = bae.getRight();
							leftSide = bae.getLeft();
						}
					
					
					} else {
						return this;
					
					}
				
				}
				
				
				result.add(getContent(leftSide));
				result.add(getContent(rightSide));
				
				result.paramsToSets();
				
				HashMap<String, StringRepresentation> HMtoAdd = new HashMap<String, StringRepresentation>(this.map);
				HMtoAdd.put(key, result);
				
				return new CharIsIncludedDomain(HMtoAdd);
				
			}
			
			
		}
		
		return this;
	}

	@Override
	public CharIsIncludedDomain assume(Expression currentExpression) {
		//TODO
		return this;
	}

	@Override
	public boolean satisfy(Expression currentExpression) {
		// TODO
		return false;
	}

	@Override
	public String toString() {
		String out = "";
		for (Map.Entry<String, StringRepresentation> entry : map.entrySet()) {
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
		CharIsIncludedDomain other = (CharIsIncludedDomain) obj;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		return true;
	}

}
