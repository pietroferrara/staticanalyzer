package it.unive.dais.staticanalyzer.cfg.expression;
/**
 * A char constant value 'c'
 * @author Pietro Ferrara
 *
 */
public class CharConstant extends Constant {
	private char value;
	/**
	 * 
	 * @param value the char value
	 * @param line the line where the char constant begins
	 * @param column the column where the char constant begins
	 */
	public CharConstant(char value, int line, int column) {
		super(line, column);
		this.value = value;
	}
	@Override
	public String toString() {
		return "'"+String.valueOf(value)+"'";
	}
}
