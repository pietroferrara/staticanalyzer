package it.unive.dais.staticanalyzer.abstractdomain.instances;

public class StringUtility {
	
	private static String removeOccurrences(String in) {
		String s = new String(in);
		
		int i = 0;
		while(i < s.length()){
	        char c = s.charAt(i);
	        if(i != s.lastIndexOf(c) || c == '"') //If c occurs multiple times in s, remove first one
	            s = s.substring(0, i) + s.substring(i+1, s.length());
	        else i++; //otherwise move pointer forward
	    }
		
		return s;
	}
	
	/** Returns the union of the two strings, case insensitive. 
    Takes O( (|S1| + |S2|) ^2 ) time. */
	public static String union(String s1, String s2){
	    String s = (s1 + s2).toLowerCase(); //start with entire contents of both strings
	    s = StringUtility.removeOccurrences(s);
	    
	    
	    
	    return s;
	}

	/** Returns the intersection of the two strings, case insensitive. 
    Takes O( |S1| * |S2| ) time. */
	public static String intersect(String s1, String s2){
	    String s = "";
	    s2 = s2.toLowerCase();
	    for(char c : s1.toLowerCase().toCharArray()){
	        if(s2.indexOf(c) != -1 && s.indexOf(c) == -1)
	            s += c;
	    }
	    //System.out.println(s);
	    return s;
	}

	public static String stringToSet(String in) {
		String s = in.toLowerCase();
		
		s.replaceAll(" ", "");
		s = StringUtility.removeOccurrences(s);
		
		return s;
	}

}
