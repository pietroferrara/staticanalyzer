package it.unive.dais.staticanalyzer.abstractdomain.instances;

import java.util.HashMap;
import java.util.Map;

import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.abstractdomain.generic.FunctionalDomain;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.statement.Assignment;
import it.unive.dais.staticanalyzer.cfg.statement.SkipStatement;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;
import it.unive.dais.staticanalyzer.cfg.statement.VariableDeclaration;

public final class Environment<T extends NonRelationalDomain <T>> extends FunctionalDomain<String, T, Environment<T>> implements SemanticDomain<Environment<T>> {

	public Environment(T domain) {
		super(domain);
	}

	protected Environment(T domain, Map<String, T> function) {
		super(domain, function);
	}
	
	@Override
	public Environment<T> bottom() {
		return new Environment<T>(this.valueDomain, null);
	}
	
	@Override
	public Environment<T> smallStepSemantics(Statement st) {
		if(st instanceof SkipStatement)
			return new Environment<>(this.valueDomain, function);
		if(st instanceof VariableDeclaration) {
			Map<String, T> map = new HashMap<>(this.function);
			map.put(((VariableDeclaration) st).getId().getName(), this.valueDomain);
			return new Environment<>(this.valueDomain, map);
		}
		if(st instanceof Assignment) {
			Assignment assignment = (Assignment) st;
			String assignedVariable = assignment.getAssignedVariable().getName();
			Map<String, T> map = new HashMap<>(this.function);
			map.put(assignedVariable, this.valueDomain.eval(assignment.getExpression(), this));
			return new Environment<>(this.valueDomain, map);
		}
		if(st instanceof Expression)
			return new Environment<>(this.valueDomain, function);
		throw new UnsupportedOperationException("Statement "+st.getClass().getTypeName()+" not yet supported");
	}

	@Override
	public Environment<T> assume(Expression currentExpression) {
		//TODO: to be refined
		return new Environment<>(valueDomain, function);
	}

	@Override
	public boolean satisfy(Expression currentExpression) {
		//TODO: to be refined
		return false;
	}

}
