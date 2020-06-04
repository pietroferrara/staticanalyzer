/*package it.unive.dais.staticanalyzer.abstractdomain.instances;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.cfg.expression.BinaryArithmeticExpression;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.IntegerConstant;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;
import it.unive.dais.staticanalyzer.cfg.statement.Assignment;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

public class IisPositiveDomain implements Lattice<IisPositiveDomain>, SemanticDomain<IisPositiveDomain> {

	private Boolean value;
	
	public IisPositiveDomain() {
		value = null;
	}

	public IisPositiveDomain(boolean value) {
		this.value = value;
	}
	@Override
	public IisPositiveDomain smallStepSemantics(Statement st) {
		if(st instanceof Assignment) {
			Assignment ass = (Assignment) st;
			if(ass.getAssignedVariable().getName().equals("i")) {
				Expression expr = ass.getExpression();
				if(expr instanceof IntegerConstant) {
					if(((IntegerConstant) expr).getValue() > 0)
						return new IisPositiveDomain(true);
				}
				if(expr instanceof BinaryArithmeticExpression) {
					BinaryArithmeticExpression assignedExpr = (BinaryArithmeticExpression) expr;
					if(assignedExpr.getOperator().equals("*") || assignedExpr.getOperator().equals("+")) {
						Expression leftSide = assignedExpr.getLeft();
						Expression rightSide = assignedExpr.getRight();
						if(leftSide instanceof VariableIdentifier)
							if(rightSide instanceof IntegerConstant) {
								String variable = ((VariableIdentifier) leftSide).getName();
								long constant = ((IntegerConstant) rightSide).getValue();
								if(
										variable.equals("i") 
										&& this.value!=null && this.value.booleanValue()
										&& constant >= 0
										)
									return new IisPositiveDomain(true);
							}
					}
				}
				return new IisPositiveDomain(false);
			}
			return this;
		}
		return this;
	}

	@Override
	public IisPositiveDomain assume(Expression currentExpression) {
		//TODO to be refined later
		return this;
	}

	@Override
	public boolean satisfy(Expression currentExpression) {
		// TODO to be refined later
		return false;
	}

	@Override
	public IisPositiveDomain lub(IisPositiveDomain other) {
		if(other == null)
			return bottom();
		if(this.value==null)
			return other;
		Boolean otherValue = other.value;
		if(otherValue==null)
			return this;
		if(this.value.booleanValue() && otherValue.booleanValue())
			return new IisPositiveDomain(true);
		else return new IisPositiveDomain(false);
	}

	@Override
	public boolean lessOrEqual(IisPositiveDomain other) {
		if(this.value == null)
			return true;
		if(other.value == null)
			return false;
		if(other.value.booleanValue() == false)
			return true;
		if(this.value.booleanValue())
			return true;
		else return false;
	}

	@Override
	public IisPositiveDomain widening(IisPositiveDomain succ) {
		return this.lub(succ);
	}

	@Override
	public IisPositiveDomain bottom() {
		return new IisPositiveDomain();
	}

	@Override
	public String toString() {
		if(value==null)
			return "_|_";
		if(value==true)
			return "i is positive!";
		else return "we don't know if i is positive :(";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		IisPositiveDomain other = (IisPositiveDomain) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}*/
