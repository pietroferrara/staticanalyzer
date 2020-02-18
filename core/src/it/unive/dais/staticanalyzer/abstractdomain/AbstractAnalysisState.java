package it.unive.dais.staticanalyzer.abstractdomain;

import static org.junit.Assert.assertEquals;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.NegatedBooleanExpression;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

/**
 * The class representing the overall state of the analysis at a given program point
 * @author Pietro Ferrara
 *
 * @param <T> the type of the abstract domain executed during the analysis
 */
public final class AbstractAnalysisState<T extends Lattice<T> & SemanticDomain<T>> implements Lattice<AbstractAnalysisState<T>> {
	private final Expression currentExpression;
	private final T abstractState;
	
	/**
	 * 
	 * @param currentExpression the expression just produced (e.g., to be assumed)
	 * @param abstractState the abstract state
	 */
	public AbstractAnalysisState(Expression currentExpression, T abstractState) {
		this.currentExpression = currentExpression;
		this.abstractState = abstractState;
	}

	@Override
	public AbstractAnalysisState<T> bottom() {
		return new AbstractAnalysisState<>(null, null);
	}
	
	
	private boolean isBottom() {
		return currentExpression == null && getSemanticDomainState() == null;
	}
	
	@Override
	public AbstractAnalysisState<T> lub(AbstractAnalysisState<T> other) {
		if(this.isBottom()) return other;
		if(other == null || other.isBottom()) return this;
		assertEquals(currentExpression, other.currentExpression);
		return new AbstractAnalysisState<T>(currentExpression, this.getSemanticDomainState().lub(other.getSemanticDomainState()));
	}

	@Override
	public boolean lessOrEqual(AbstractAnalysisState<T> other) {
		if(this.isBottom()) return true;
		if(other == null || other.isBottom()) return false;
		assertEquals(currentExpression, other.currentExpression);
		return this.getSemanticDomainState().lessOrEqual(other.getSemanticDomainState());
	}

	@Override
	public AbstractAnalysisState<T> widening(AbstractAnalysisState<T> succ) {
		if(this.isBottom()) return succ;
		if(succ.isBottom()) return this;
		assertEquals(currentExpression, succ.currentExpression);
		return new AbstractAnalysisState<T>(currentExpression, this.getSemanticDomainState().widening(succ.getSemanticDomainState()));
	}

	/**
	 * Performs the abstract semantics of the given statement
	 * @param st the statement on which the semantics is computed
	 * @return the abstract state after the abstract semantic of the given statement on the current abstract state
	 */
	public AbstractAnalysisState<T> smallStepSemantics(Statement st) {
		if(this.isBottom()) return this.bottom();
		T newAbstractState = getSemanticDomainState().smallStepSemantics(st);
		if(st instanceof Expression)
			return new AbstractAnalysisState<T>((Expression) st, newAbstractState);
		else
			return new AbstractAnalysisState<T>(null, newAbstractState);
	}

	/**
	 * Assume that the current expression holds on the current abstract state
	 * @return the state after assuming that the current expression holds
	 */
	public AbstractAnalysisState<T> assumeExpressionHolds() {
		if(this.isBottom()) return this.bottom();
		T newAbstractState = getSemanticDomainState().assume(this.currentExpression);
		return new AbstractAnalysisState<T>(null, newAbstractState);
	}

	/**
	 * Assume that the current expression does not hold on the current abstract state
	 * @return the state after assuming that the current expression does not hold
	 */
	public AbstractAnalysisState<T> assumeExpressionDoesNotHold() {
		if(this.isBottom()) return this.bottom();
		T newAbstractState = getSemanticDomainState().assume(new NegatedBooleanExpression(this.currentExpression, this.currentExpression.getLine(), this.currentExpression.getColumn()));
		return new AbstractAnalysisState<T>(null, newAbstractState);
	}
	
	@Override
	public String toString() {
		return "Expression:"+currentExpression+
				"\nState:\n"+getSemanticDomainState();
	}

	/**
	 * 
	 * @return the abstract state of the analysis
	 */
	public T getSemanticDomainState() {
		return abstractState;
	}
}
