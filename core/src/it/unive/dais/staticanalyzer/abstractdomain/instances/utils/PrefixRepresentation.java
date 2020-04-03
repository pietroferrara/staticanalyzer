package it.unive.dais.staticanalyzer.abstractdomain.instances.utils;

import it.unive.dais.staticanalyzer.abstractdomain.instances.utils.Utils.Bounds;

public class PrefixRepresentation {

	public String prefix;
	public Bounds bound;
	public boolean cutted;
	
	public PrefixRepresentation() {
		this.prefix = new String();
		this.bound = null;
		this.cutted = false;
	}
	
	private void removeDoubleQuotes() {
		
		int i = 0;
		while(i < this.prefix.length()){
	        char c = this.prefix.charAt(i);
	        if(c == '"') 
	            this.prefix = this.prefix.substring(0, i) + this.prefix.substring(i+1, this.prefix.length());
	        else i++; 
	    }
		
	}
	
	public PrefixRepresentation(String prefix) {
		this.prefix = new String(prefix);
		this.bound = null;
		if(prefix == "")
			this.bound = Utils.Bounds.TOP;
		this.cutted = false;
		
		removeDoubleQuotes();
	}
	
	public int getLength() {
		return prefix.length();
	}
	
	public char charAt(int i) {
		return prefix.charAt(i);
	}

	@Override
	public String toString() {
		if(this.bound == Utils.Bounds.BOTTOM)
			return "_|_";
		if(this.bound == Utils.Bounds.TOP)
			return "T";
		return this.prefix;
	}
	
	public void add(PrefixRepresentation pr) {
		this.prefix += pr.prefix;
		removeDoubleQuotes();
	}
	
	
}
