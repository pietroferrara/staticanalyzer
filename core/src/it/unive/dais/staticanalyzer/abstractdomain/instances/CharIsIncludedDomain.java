package it.unive.dais.staticanalyzer.abstractdomain.instances;

import java.util.HashMap;
import java.util.Map;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.abstractdomain.instances.utils.StringRepresentation;
import it.unive.dais.staticanalyzer.cfg.expression.BinaryArithmeticExpression;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.StringConstant;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;
import it.unive.dais.staticanalyzer.cfg.statement.Assignment;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;



public class CharIsIncludedDomain implements Lattice<CharIsIncludedDomain>, SemanticDomain<CharIsIncludedDomain> {
	
	StringRepresentation string; 
	
	protected Map<String, StringRepresentation> map;
	
	public CharIsIncludedDomain(StringRepresentation strRepr, Map<String, StringRepresentation> map) {
		this.string = new StringRepresentation(strRepr.name, strRepr.C, strRepr.MC);
		
		
		
		this.map = new HashMap<>();
		
		for (Map.Entry<String, StringRepresentation> entry : map.entrySet()) {
			StringRepresentation s = new StringRepresentation(entry.getValue());
		    this.map.put(entry.getKey(), s);
		}
	}
	
	public CharIsIncludedDomain() {
		this.string = new StringRepresentation();
		this.map = new HashMap<>();
	}

	@Override
	public CharIsIncludedDomain lub(CharIsIncludedDomain other) {
		String C = StringUtility.intersect(this.string.C, other.string.C);
		String MC = StringUtility.union(this.string.MC, other.string.MC);
		
		//System.out.println("C:"+C+" MC:"+MC);
		
		StringRepresentation str = new StringRepresentation(string.name, C, MC);
		if(string != null && string.name != null)
			map.put(string.name, str);
		
		return new CharIsIncludedDomain(str, this.map);
	}

	@Override
	public boolean lessOrEqual(CharIsIncludedDomain other) {
		boolean flag = true;
		
		for (int i = 0; i < other.string.C.length(); i++) {
			//if C does not contain a char of other.C
	        if (string.C.indexOf(other.string.C.charAt(i)) == -1) {
	            flag = false;
	        }
	    }
		
		for (int i = 0; i < string.MC.length(); i++) {
			//if other.MC does not contain a char of MC
	        if (other.string.MC.indexOf(string.MC.charAt(i)) == -1) {
	            flag = false;
	        }
	    }
		
		return flag;
	}

	@Override
	public CharIsIncludedDomain widening(CharIsIncludedDomain succ) {
		return lub(succ);
	}

	@Override
	public CharIsIncludedDomain bottom() {
		CharIsIncludedDomain charIsIncludedDomain = new CharIsIncludedDomain();
		charIsIncludedDomain.string.bound = StringRepresentation.Bounds.BOTTOM;
		
		return charIsIncludedDomain;
	}
	


	
	private CharIsIncludedDomain top() {
		CharIsIncludedDomain charIsIncludedDomain = new CharIsIncludedDomain();
		
		charIsIncludedDomain.string.bound = StringRepresentation.Bounds.TOP;
		return charIsIncludedDomain;
	}
	
	private StringRepresentation getContent(Expression expr) {
		StringRepresentation res = new StringRepresentation();
		
		if(expr instanceof StringConstant) {
			StringConstant str = (StringConstant)expr;
			res.C += str.toString();
			res.MC += str.toString();
		}
		
		if(expr instanceof VariableIdentifier) {
			VariableIdentifier vi = (VariableIdentifier)expr;
			
			StringRepresentation storedValue = map.get(vi.toString());
			if(storedValue == null) {
				res.MC += "K";
			} else {
				//add 
				res.C += storedValue.C;
				res.MC += storedValue.MC;
			}
		}
		
		return res;
	}


	@Override
	public CharIsIncludedDomain smallStepSemantics(Statement st) {
		System.out.println(st.getClass().toString());
		if(st instanceof Assignment) {
			Assignment ass = (Assignment)st;
			String key = ass.getAssignedVariable().getName();
			
			Expression expr = ass.getExpression();
			
			if(expr instanceof StringConstant) {
				String value = ((StringConstant)expr).toString();
				
				StringRepresentation toAdd = new StringRepresentation(key, value, value);
				
				map.put(key, toAdd);
				
				return new CharIsIncludedDomain(toAdd, map);
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
						//TODO
					
					}
				
				}
				
				result.add(getContent(leftSide));
				result.add(getContent(rightSide));
				
				result.paramsToSets();
				
				map.put(key, result);
				
				
				
				return new CharIsIncludedDomain(result, map);
				
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

	

	
	
	

	
	

}
