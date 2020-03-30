package it.unive.dais.staticanalyzer.abstractdomain.instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.abstractdomain.instances.utils.Pair;
import it.unive.dais.staticanalyzer.cfg.expression.BinaryArithmeticExpression;
import it.unive.dais.staticanalyzer.cfg.expression.Constant;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.StringConstant;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;
import it.unive.dais.staticanalyzer.cfg.statement.Assignment;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;
import it.unive.dais.staticanalyzer.cfg.statement.VariableDeclaration;

public class CharIsIncludedDomain implements Lattice<CharIsIncludedDomain>, SemanticDomain<CharIsIncludedDomain> {

	//certainly contained characters
	private String C; 
	
	//maybe contained characters
	private String MC; 
	
	private boolean top, bottom;
	
	protected Map<String, Pair<String, String>> map;
	
	public CharIsIncludedDomain(String C, String MC, Map<String, Pair<String, String>> map) {
		this.C = C;
		this.MC = MC;
		this.top = false;
		this.bottom = false;
		this.map = map;
	}
	
	public CharIsIncludedDomain() {
		this.C = new String("");
		this.MC = new String("");
		this.map = new HashMap<>();
	}

	@Override
	public CharIsIncludedDomain lub(CharIsIncludedDomain other) {
		return new CharIsIncludedDomain(StringUtility.intersect(this.C, other.C), StringUtility.union(this.MC, other.MC), this.map);
	}

	@Override
	public boolean lessOrEqual(CharIsIncludedDomain other) {
		boolean flag = true;
		
		for (int i = 0; i < other.C.length(); i++) {
			//if C does not contain a char of other.C
	        if (C.indexOf(other.C.charAt(i)) == -1) {
	            flag = false;
	        }
	    }
		
		for (int i = 0; i < MC.length(); i++) {
			//if other.MC does not contain a char of MC
	        if (other.MC.indexOf(MC.charAt(i)) == -1) {
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
		charIsIncludedDomain.bottom = true;
		
		return charIsIncludedDomain;
	}
	

	@Override
	public String toString() {
		if(this.bottom)
			return "_|_";
		if(this.top)
			return "T";
		return "Certainly contained characters: '" + C + "'\n Maybe contained characters: '" + MC + "'";
	}
	
	private CharIsIncludedDomain top() {
		CharIsIncludedDomain charIsIncludedDomain = new CharIsIncludedDomain();
		
		charIsIncludedDomain.top = true;
		return charIsIncludedDomain;
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
				
				Pair<String, String> toAdd = new Pair<>(value, "");
				
				map.put(key, toAdd);
				
				return new CharIsIncludedDomain(value, "", map);
			}
			
			if(expr instanceof BinaryArithmeticExpression) {
				BinaryArithmeticExpression assignedExpr = (BinaryArithmeticExpression)expr;
				if(assignedExpr.getOperator().equals("+")) {
					Expression leftSide = assignedExpr.getLeft();
					Expression rightSide = assignedExpr.getRight(); 
					
					if(leftSide instanceof VariableIdentifier) {
						if(rightSide instanceof StringConstant) {
							String variable = ((VariableIdentifier)leftSide).getName();
							String constant = ((StringConstant)rightSide).toString();
							
							Pair<String, String> storedValue = map.get(variable);
							Pair<String,String> updatedValue = new Pair<String, String>(storedValue.getLeft() + constant, storedValue.getRight());
						
							map.put(variable, updatedValue);
							
							return new CharIsIncludedDomain(updatedValue.getLeft(), updatedValue.getRight(), map);
						}
					}
				}
			}
			
			
			return new CharIsIncludedDomain("", "", map);
			
		}
		return new CharIsIncludedDomain("", "", map);
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((C == null) ? 0 : C.hashCode());
		result = prime * result + ((MC == null) ? 0 : MC.hashCode());
		result = prime * result + (bottom ? 1231 : 1237);
		result = prime * result + ((map == null) ? 0 : map.hashCode());
		result = prime * result + (top ? 1231 : 1237);
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
		if (C == null) {
			if (other.C != null)
				return false;
		} else if (!C.equals(other.C))
			return false;
		if (MC == null) {
			if (other.MC != null)
				return false;
		} else if (!MC.equals(other.MC))
			return false;
		if (bottom != other.bottom)
			return false;
		if (map == null) {
			if (other.map != null)
				return false;
		} else if (!map.equals(other.map))
			return false;
		if (top != other.top)
			return false;
		return true;
	}
	
	

	
	

}
