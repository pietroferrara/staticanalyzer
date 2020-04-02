package it.unive.dais.staticanalyzer.abstractdomain.instances.utils;

import it.unive.dais.staticanalyzer.abstractdomain.instances.StringUtility;

public class StringRepresentation {
	
	public String name;
	//certainly contained characters
	public String C;
	
	//maybe contained characters
	public String MC;
	
	public enum Bounds {
		  BOTTOM,
		  TOP
		}
	
	public Bounds bound;
	
	public StringRepresentation() {
		this.C = new String("");
		this.MC = new String("");
		bound = null;
	}
	
	public StringRepresentation(String name, String C, String MC) {
		this.name = name;
		this.C = new String(StringUtility.stringToSet(C));
		this.MC = new String(StringUtility.stringToSet(MC));
		bound = null;
	}
	
	public StringRepresentation(StringRepresentation copy) {
		this(copy.name, copy.C, copy.MC);
	}

	@Override
	public String toString() {
		if(this.bound == Bounds.BOTTOM)
			return "_|_";
		if(this.bound == Bounds.TOP)
			return "T";
		return "C: '" + C + "' MC: '" + MC + "'";
	}
	
	public void add(StringRepresentation str) {
		this.C += str.C;
		this.MC += str.MC;
	}
	
	public void paramsToSets() {
		this.C = StringUtility.stringToSet(this.C);
		this.MC = StringUtility.stringToSet(this.MC);
	}
	
	
}
