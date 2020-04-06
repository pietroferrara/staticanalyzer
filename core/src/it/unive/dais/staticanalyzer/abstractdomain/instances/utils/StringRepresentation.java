package it.unive.dais.staticanalyzer.abstractdomain.instances.utils;

/**
 * A string is represented by a set of certainly included characters C and a set of maybe included characters MC.
 * The 'bound' variable specify if the string is the bottom or the top element.
 * 
 * The top element is represented by (empty set, K). The toString function follows the notation of the paper.
 * 
 * @author Carlo Zen
 *
 */
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
		this.bound = null;
	}
	
	public StringRepresentation(String name, String C, String MC) {
		this.name = name;
		this.C = new String(StringUtility.stringToSet(C));
		this.MC = new String(StringUtility.stringToSet(MC));
		this.bound = null;
	}
	
	public StringRepresentation(StringRepresentation copy) {
		this(copy.name, copy.C, copy.MC);
		this.bound = null;
	}

	@Override
	public String toString() {
		if(bound == Utils.Bounds.BOTTOM)
			return "_|_";
		if(bound == Utils.Bounds.TOP)
			return "C: '" + C + "' MC: 'K'";
		return "C: '" + C + "' MC: '" + MC + "'";
	}
	
	public void add(StringRepresentation str) {
		this.C += str.C;
		
		if(str.bound == Utils.Bounds.TOP) {
			this.bound = Utils.Bounds.TOP;
			this.MC = "k";
		}
		
		if(this.bound != Utils.Bounds.TOP)
			this.MC += str.MC;	
		
	}
	
	public void paramsToSets() {
		this.C = StringUtility.stringToSet(this.C);
		this.MC = StringUtility.stringToSet(this.MC);
	}
	
	public void setTop() {
		this.bound = Utils.Bounds.TOP;
		this.MC = "k";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((C == null) ? 0 : C.hashCode());
		result = prime * result + ((MC == null) ? 0 : MC.hashCode());
		result = prime * result + ((bound == null) ? 0 : bound.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		StringRepresentation other = (StringRepresentation) obj;
		if (C == null) {
			if (other.C != null)
				return false;
		} else if (!C.equals(other.C))
			return false;
		if (MC == null) {
			if (other.MC != null)
				return false;
		} else if (!MC.equals(other.MC))
			return false;
		if (bound != other.bound)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	
}
