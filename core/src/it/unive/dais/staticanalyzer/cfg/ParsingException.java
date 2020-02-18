package it.unive.dais.staticanalyzer.cfg;

/**
 * An exception occured during the parsing of a source code file
 * @author Pietro Ferrara
 *
 */
public class ParsingException extends Exception {
	private static final long serialVersionUID = 2840334420747686328L;

	/**
	 * 
	 */
	public ParsingException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param message the exception message
	 */
	public ParsingException(String message) {
		super(message);
	}
	
	/**
	 * 
	 * @param message the exception message
	 * @param cause the cause of the exception
	 */
	public ParsingException(String message, Throwable cause) {
		super(message, cause);
	}

}
