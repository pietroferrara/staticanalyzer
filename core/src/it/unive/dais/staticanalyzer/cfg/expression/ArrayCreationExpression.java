package it.unive.dais.staticanalyzer.cfg.expression;

import it.unive.dais.staticanalyzer.cfg.Type;

/**
 * The creation of an array new <innerType>[<size>]
 * @author Pietro Ferrara
 *
 */
public class ArrayCreationExpression extends Expression {
	private Expression size;
	private Type innerType;
	/**
	 * 
	 * @param innerType the type of the created array
	 * @param size the size of the created array
	 * @param line the line where the negated expression begins
	 * @param column the column where the negated expression begins
	 */
	public ArrayCreationExpression(Type innerType, Expression size, int line, int column) {
		super(line, column);
		this.size= size;
		this.innerType = innerType;
	}
	@Override
	public String toString() {
		return "new "+innerType.toString()+"["+size.toString()+"]";
	}
	public Expression getSize() {
		return size;
	}
	public Type getInnerType() {
		return innerType;
	}
}

