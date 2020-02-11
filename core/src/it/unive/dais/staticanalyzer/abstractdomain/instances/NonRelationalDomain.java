package it.unive.dais.staticanalyzer.abstractdomain.instances;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;

public interface NonRelationalDomain<T extends NonRelationalDomain<T>> extends Lattice<T> {
	public T eval(Expression expr, Environment<T> env);
}
