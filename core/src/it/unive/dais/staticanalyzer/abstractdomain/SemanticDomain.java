package it.unive.dais.staticanalyzer.abstractdomain;

import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

public interface SemanticDomain<T extends SemanticDomain<T>> {
	public T smallStepSemantics(Statement st);

	public T assume(Expression currentExpression);
	
	public boolean satisfy(Expression currentExpression);
}
