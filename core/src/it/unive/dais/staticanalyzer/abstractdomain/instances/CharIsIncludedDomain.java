package it.unive.dais.staticanalyzer.abstractdomain.instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.abstractdomain.instances.utils.StringRepresentation;
import it.unive.dais.staticanalyzer.cfg.expression.BinaryArithmeticExpression;
import it.unive.dais.staticanalyzer.cfg.expression.Constant;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.NumericalComparisonExpression;
import it.unive.dais.staticanalyzer.cfg.expression.StringConstant;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;
import it.unive.dais.staticanalyzer.cfg.statement.Assignment;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;
import it.unive.dais.staticanalyzer.cfg.statement.VariableDeclaration;



public class CharIsIncludedDomain implements Lattice<CharIsIncludedDomain>, SemanticDomain<CharIsIncludedDomain> {
	
	StringRepresentation string; 
	
	protected Map<String, StringRepresentation> map;
	
	public CharIsIncludedDomain(StringRepresentation strRepr, Map<String, StringRepresentation> map) {
		this.string = strRepr;
		this.map = new HashMap<>(map);
	}
	
	public CharIsIncludedDomain() {
		this.string = new StringRepresentation();
		this.map = new HashMap<>();
	}

	@Override
	public CharIsIncludedDomain lub(CharIsIncludedDomain other) {
		String C = StringUtility.intersect(this.string.C, other.string.C);
		String MC = StringUtility.union(this.string.MC, other.string.MC);
		
		StringRepresentation str = new StringRepresentation(C, MC);
		
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


	@Override
	public CharIsIncludedDomain smallStepSemantics(Statement st) {
		
		if(st instanceof Assignment) {
			Assignment ass = (Assignment)st;
			String key = ass.getAssignedVariable().getName();
			
			Expression expr = ass.getExpression();
			
			if(expr instanceof StringConstant) {
				String value = ((StringConstant)expr).toString();
				
				StringRepresentation toAdd = new StringRepresentation(value, "");
				
				map.put(key, toAdd);
				
				return new CharIsIncludedDomain(toAdd, map);
			}
			
			//TODO: add NumericalComparisonExpression option?
			/*
			if(expr instanceof BinaryArithmeticExpression) {
				BinaryArithmeticExpression assignedExpr = (BinaryArithmeticExpression)expr;
				Expression leftSide = assignedExpr.getLeft();
				Expression rightSide = assignedExpr.getRight();
				
				Pair<String, String> result = new Pair("", "");
				boolean maybeIncluded = false;
				
				while(leftSide instanceof BinaryArithmeticExpression || rightSide instanceof BinaryArithmeticExpression) {
					if(assignedExpr.getOperator().equals("+")) {
						if(leftSide instanceof BinaryArithmeticExpression) {
							if(rightSide instanceof StringConstant) {
								StringConstant str = (StringConstant)rightSide;
								result.setLeft(result.getLeft() + str.toString());
							}
							
							if(rightSide instanceof VariableIdentifier) {
								VariableIdentifier vi = (VariableIdentifier)rightSide;
								
								Pair<String, String> storedValue = map.get(vi);
								if(storedValue == null) {
									maybeIncluded = true;
								} else {
									//add 
									result.setLeft(result.getLeft() + storedValue.getLeft());
									result.setRight(result.getRight() + storedValue.getRight());
								}
							}
							
							BinaryArithmeticExpression bae = (BinaryArithmeticExpression)leftSide;
							rightSide = bae.getRight();
							leftSide = bae.getLeft();
							
						} else {
							BinaryArithmeticExpression bae = (BinaryArithmeticExpression)rightSide;
							rightSide = bae.getRight();
							leftSide = bae.getLeft();
						}
					
					
					} else {
						//TODO
					
					}
				
				}
				
				
				Pair<String, String> storedValues = map.get(key);
				if(maybeIncluded) {
					return new CharIsIncludedDomain("", result.getLeft() + result.getRight(), map);
				} else {
					return new CharIsIncludedDomain(result.getLeft(), result.getRight(), map);
				}
				
				/*if(assignedExpr.getOperator().equals("+")) {
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
				}*/
			/*}
			
			
			return new CharIsIncludedDomain("", "", map);
		*/	
		}
		return new CharIsIncludedDomain(new StringRepresentation(), map);
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
