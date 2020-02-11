package it.unive.dais.staticanalyzer.abstractdomain.generic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;

public abstract class FunctionalDomain<D, T extends Lattice<T>, V extends FunctionalDomain<D, T, V>> implements Lattice<V> {
	protected Map<D, T> function;
	protected final T valueDomain;
	
	protected FunctionalDomain(T valueDomain) {
		this.valueDomain = valueDomain;
		this.function = new HashMap<>();
	}

	protected FunctionalDomain(T valueDomain, Map<D, T> state) {
		this.valueDomain = valueDomain;
		this.function = state;
	}

	protected boolean isBottom() {
		return function==null;
	}
	
	protected T getState(D st) {
		if(! this.isBottom() && function.containsKey(st))
			return function.get(st);
		else return valueDomain;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final V lub(V other) {
		if(this.isBottom())
			return other;
		if(other.isBottom())
			return (V) this;
		return functionalLift(other, (o1, o2) -> o1 == null ? o2 : o1.lub(o2));
	}

	private final V functionalLift(V other, java.util.function.BiFunction<T, T, T> lift) {
		V result = this.bottom();
		result.function = new HashMap<>();
		Set<D> keys = new HashSet<>(this.function.keySet());
		
		keys.addAll(other.function.keySet());
		for(D key : keys)
			result.function.put(key, lift.apply(this.getState(key), other.getState(key)));
		return result;
	}
	
	@Override
	public final boolean lessOrEqual(V other) {
		if(this.isBottom())
			return true;
		if(other.isBottom())
			return false;
		for(D key : function.keySet())
			if(this.getState(key)!=null && (! this.getState(key).lessOrEqual(other.getState(key))))
				return false;
		return true;
	}

	@Override
	public final V widening(V succ) {
		if(this.isBottom())
			return succ;
		if(succ.isBottom())
			return succ.bottom();
		return functionalLift(succ, (o1, o2) -> o1.widening(o2));
	}
	
	@Override
	public String toString() {
		if(this.isBottom())
			return "_|_";
		else return function.toString();
	}
}
