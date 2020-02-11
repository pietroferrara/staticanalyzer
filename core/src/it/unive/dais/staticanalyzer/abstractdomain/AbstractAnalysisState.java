package it.unive.dais.staticanalyzer.abstractdomain;

import static org.junit.Assert.assertEquals;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

public final class AbstractAnalysisState<T extends SemanticDomain<T> & Lattice<T>> implements SemanticDomain<AbstractAnalysisState<T>>, Lattice<AbstractAnalysisState<T>> {
	private final Expression currentExpression;
	private final T abstractState;
	
	public AbstractAnalysisState(Expression currentExpression, T abstractState) {
		this.currentExpression = currentExpression;
		this.abstractState = abstractState;
	}

	public AbstractAnalysisState<T> bottom() {
		return new AbstractAnalysisState<>(null, null);
	}
	
	private boolean isBottom() {
		return currentExpression == null && abstractState == null;
	}
	
	@Override
	public AbstractAnalysisState<T> lub(AbstractAnalysisState<T> other) {
		if(this.isBottom()) return other;
		if(other == null || other.isBottom()) return this;
		assertEquals(currentExpression, other.currentExpression);
		return new AbstractAnalysisState<T>(currentExpression, this.abstractState.lub(other.abstractState));
	}

	@Override
	public boolean lessOrEqual(AbstractAnalysisState<T> other) {
		if(this.isBottom()) return true;
		if(other == null || other.isBottom()) return false;
		assertEquals(currentExpression, other.currentExpression);
		return this.abstractState.lessOrEqual(other.abstractState);
	}

	@Override
	public AbstractAnalysisState<T> widening(AbstractAnalysisState<T> succ) {
		if(this.isBottom()) return succ;
		if(succ.isBottom()) return this;
		assertEquals(currentExpression, succ.currentExpression);
		return new AbstractAnalysisState<T>(currentExpression, this.abstractState.widening(succ.abstractState));
	}

	@Override
	public AbstractAnalysisState<T> smallStepSemantics(Statement st) {
		if(this.isBottom()) return this.bottom();
		T newAbstractState = abstractState.smallStepSemantics(st);
		if(st instanceof Expression)
			return new AbstractAnalysisState<T>((Expression) st, newAbstractState);
		else
			return new AbstractAnalysisState<T>(null, newAbstractState);
	}
	
	@Override
	public String toString() {
		return "Expression:"+currentExpression+
				"\nState:\n"+abstractState;
	}
}
