package it.unive.dais.staticanalyzer.api;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * The class representing a warning
 * @author Pietro Ferrara
 *
 */
public class Warning {
	private int line, column;
	private String message;

	/**
	 * 
	 * @param line the line of the code the warning refers to
	 */
	public void setLine(int line) {
		this.line = line;
	}

	/**
	 * 
	 * @param column the column of the code the warning refers to
	 */
	public void setColumn(int column) {
		this.column = column;
	}

	/**
	 * 
	 * @param message the warning message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 
	 * @param line the line of the code the warning refers to
	 * @param column the column of the code the warning refers to
	 * @param message the warning message
	 */
	public Warning(int line, int column, String message) {
		this.line = line;
		this.column = column;
		this.message = message;
	}

	@SuppressWarnings("unused")
	private Warning() {}
	
	/**
	 * 
	 * @return the line of the code the warning refers to
	 */
	@XmlAttribute
	public int getLine() {
		return line;
	}

	/**
	 * 
	 * @return the column of the code the warning refers to
	 */
	@XmlAttribute
	public int getColumn() {
		return column;
	}

	/**
	 * 
	 * @return the warning message
	 */
	@XmlElement
	public String getMessage() {
		return message;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result + line;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
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
		Warning other = (Warning) obj;
		if (column != other.column)
			return false;
		if (line != other.line)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "At line " + line + ", column " + column + ": " + message;
	}
	
}
