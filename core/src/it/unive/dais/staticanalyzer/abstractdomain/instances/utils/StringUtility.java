package it.unive.dais.staticanalyzer.abstractdomain.instances.utils;

public class StringUtility {
	
	
	public static String removeDoubleQuotes(String in) {
		
		String str = new String(in);
		int i = 0;
		while(i < str.length()){
	        char c = str.charAt(i);
	        if(c == '"') 
	            str = str.substring(0, i) + str.substring(i+1, str.length());
	        else i++; 
	    }
		
		return str;
		
	}
	
	/**
	 * @param in 
	 * @return remove the occurrences from the string in input
	 */
	private static String removeOccurrences(String in) {
		String s = new String(in);
		
		int i = 0;
		while(i < s.length()){
	        char c = s.charAt(i);
	        if(i != s.lastIndexOf(c)) 
	            s = s.substring(0, i) + s.substring(i+1, s.length());
	        else i++; 
	    }
		
		return s;
	}
	
	/**
	 * @param s1
	 * @param s2
	 * @return the union of the two strings, case insensitive
	 */
	public static String union(String s1, String s2){
	    String s = (s1 + s2).toLowerCase(); //start with entire contents of both strings
	    s = StringUtility.removeOccurrences(s);
	    
	    return s;
	}

	/**
	 * @param s1
	 * @param s2
	 * @return the intersection of the two strings, case insensitive
	 */
	public static String intersect(String s1, String s2){
	    String s = new String();
	    
	    s2 = s2.toLowerCase();
	    for(char c : s1.toLowerCase().toCharArray()){
	        if(s2.indexOf(c) != -1 && s.indexOf(c) == -1)
	            s += c;
	    }
	   
	    return s;
	}

	public static String stringToSet(String in) {
		String s = in.toLowerCase();
		
		s.replaceAll(" ", "");
		s = StringUtility.removeOccurrences(s);
		s = StringUtility.removeDoubleQuotes(s);
		
		return s;
	}

}
