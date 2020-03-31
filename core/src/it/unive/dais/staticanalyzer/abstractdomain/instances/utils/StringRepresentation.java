package it.unive.dais.staticanalyzer.abstractdomain.instances.utils;

public class StringRepresentation {
	
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
		this.C = "";
		this.MC = "";
	}
	
	public StringRepresentation(String C, String MC) {
		this.C = C;
		this.MC = MC;
	}

	@Override
	public String toString() {
		if(this.bound == Bounds.BOTTOM)
			return "_|_";
		if(this.bound == Bounds.TOP)
			return "T";
		return "C: '" + C + "' MC: '" + MC + "'";
	}
	
	
}
