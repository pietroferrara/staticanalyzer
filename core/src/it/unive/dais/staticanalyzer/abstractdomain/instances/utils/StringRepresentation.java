package it.unive.dais.staticanalyzer.abstractdomain.instances.utils;

import it.unive.dais.staticanalyzer.abstractdomain.instances.StringUtility;

public class StringRepresentation {
	
	public String name;
	//certainly contained characters
	public String C;
	
	//maybe contained characters
	public String MC;
	
	public Utils.Bounds bound;
	
	
	
	public StringRepresentation() {
		this.C = new String("");
		this.MC = new String("");
	}
	
	public StringRepresentation(String name, String C, String MC) {
		this.name = name;
		this.C = new String(StringUtility.stringToSet(C));
		this.MC = new String(StringUtility.stringToSet(MC));
	}
	
	public StringRepresentation(StringRepresentation copy) {
		this(copy.name, copy.C, copy.MC);
	}

	@Override
	public String toString() {
		if(bound == Utils.Bounds.BOTTOM)
			return "_|_";
		if(bound == Utils.Bounds.TOP)
			return "T";
		return "C: '" + C + "' MC: '" + MC + "'";
	}
	
	public void add(StringRepresentation str) {
		this.C += str.C;
		
		this.MC += str.MC;
		if(this.MC.contains("k") || this.MC.contains("K"))	
			this.MC = "k";
		
	}
	
	public void paramsToSets() {
		this.C = StringUtility.stringToSet(this.C);
		this.MC = StringUtility.stringToSet(this.MC);
	}
	
	
}
