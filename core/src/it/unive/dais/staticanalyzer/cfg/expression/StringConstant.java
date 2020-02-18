package it.unive.dais.staticanalyzer.cfg.expression;
/**
 * A string constant value "<value>"
 * @author Pietro Ferrara
 *
 */
public class StringConstant extends Constant {
	private String value;
	
	/**
	 * 
	 * @param value the string value
	 * @param line the line where the string constant begins
	 * @param column the column where the string constant begins
	 */
	public StringConstant(String value, int line, int column) {
		super(line, column);
		this.value = value;
	}
	@Override
	public String toString() {
		return "\""+value+"\"";
	}
}
