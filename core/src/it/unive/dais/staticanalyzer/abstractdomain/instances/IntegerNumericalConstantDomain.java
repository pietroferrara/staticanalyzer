package it.unive.dais.staticanalyzer.abstractdomain.instances;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.IntegerConstant;

/**
 * The integer constant propagation non relational abstract domain
 * @author Pietro Ferrara
 *
 */
public class IntegerNumericalConstantDomain implements NonRelationalDomain<IntegerNumericalConstantDomain> {

	private boolean top, bottom;
	private long value;
	
	/**
	 * 
	 * @param value the integer constant value represented by the instance
	 */
	public IntegerNumericalConstantDomain(long value) {
		this.value = value;
		this.top = false;
		this.bottom = false;
	}
	
	private IntegerNumericalConstantDomain() { }
	
	@Override
	public IntegerNumericalConstantDomain lub(IntegerNumericalConstantDomain other) {
		if(this.top || other.top)
			return top();
		if(this.bottom)
			return other;
		if(other.bottom)
			return this;
		if(this.value==other.value)
			return new IntegerNumericalConstantDomain(this.value);
		else return top();
	}
	
	@Override
	public boolean lessOrEqual(IntegerNumericalConstantDomain other) {
		if(this.bottom) return true;
		if(other.bottom) return false;
		if(other.top) return true;
		if(this.top) return false;
		return this.value == other.value;
	}
	@Override
	public IntegerNumericalConstantDomain widening(IntegerNumericalConstantDomain succ) {
		return this.lub(succ);
	}
	@Override
	public IntegerNumericalConstantDomain bottom() {
		IntegerNumericalConstantDomain result = new IntegerNumericalConstantDomain();
		result.bottom = true;
		return result;
	}
	
	@Override
	public IntegerNumericalConstantDomain eval(Expression expr, Environment<IntegerNumericalConstantDomain> env) {
		if(expr instanceof IntegerConstant)
			return new IntegerNumericalConstantDomain(((IntegerConstant) expr).getValue());
		else return top();
	}
	
	private IntegerNumericalConstantDomain top() {
		IntegerNumericalConstantDomain result = new IntegerNumericalConstantDomain();
		result.top = true;
		return result;
	}
	@Override
	public String toString() {
		if(this.bottom)
			return "_|_";
		if(this.top)
			return "T";
		else return String.valueOf(value);
	}
}
