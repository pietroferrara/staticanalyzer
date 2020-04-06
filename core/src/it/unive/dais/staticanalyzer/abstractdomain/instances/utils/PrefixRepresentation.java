package it.unive.dais.staticanalyzer.abstractdomain.instances.utils;

import it.unive.dais.staticanalyzer.abstractdomain.instances.utils.Utils.Bounds;
import it.unive.dais.staticanalyzer.abstractdomain.instances.utils.StringUtility;

/**
 * A string is represented by:
 *  - prefix: its prefix, a sequence of characters
 *  - bound: specifies if the string is the bottom or top element
 *  - cutted: specifies if the sequence of characters 'prefix' has been cutted 
 *               due to the lub operator or the sum with an other cutted string
 *   
 * @author Carlo Zen
 *
 */

public class PrefixRepresentation {

	public String prefix;
	public Bounds bound;
	public boolean cutted;
	
	public PrefixRepresentation() {
		this.prefix = new String();
		this.bound = null;
		this.cutted = false;
	}
	
	public PrefixRepresentation(String prefix) {
		this.prefix = new String(prefix);
		this.bound = null;
		
		if(prefix == "")
			this.bound = Utils.Bounds.TOP;
		this.cutted = false;
		
		this.prefix = StringUtility.removeDoubleQuotes(this.prefix);
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
		if(!this.cutted) {
			this.prefix = StringUtility.removeDoubleQuotes(this.prefix + pr.prefix);
			
			if(pr.cutted)
				this.cutted = true;
		}
	}
	
	public static PrefixRepresentation add(PrefixRepresentation pr1, PrefixRepresentation pr2) {
		PrefixRepresentation res = new PrefixRepresentation();
		res.prefix = pr1.prefix + pr2.prefix;
		
		if(pr1.cutted || pr2.cutted) {
			res.cutted = true;
			
			if(pr1.cutted)
				res.prefix = pr1.prefix;
		}
		
		return res;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bound == null) ? 0 : bound.hashCode());
		result = prime * result + (cutted ? 1231 : 1237);
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrefixRepresentation other = (PrefixRepresentation) obj;
		if (bound != other.bound)
			return false;
		if (cutted != other.cutted)
			return false;
		if (prefix == null) {
			if (other.prefix != null)
				return false;
		} else if (!prefix.equals(other.prefix))
			return false;
		return true;
	}
	
}
