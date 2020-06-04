package it.unive.dais.staticanalyzer.cfg;

/**
 * The class representing types
 * @author Pietro Ferrara
 *
 */
public abstract class Type extends ParsedBlock {
	
	Type(int line, int column) {
		super(line, column);
	}

	@Override
	public boolean equals(Object obj) {
		return this.toString().equals(obj.toString());
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	/**
	 * Object type
	 * @author Pietro Ferrara
	 *
	 */
	public static class ObjectType extends Type {
		private String typesignature;

		ObjectType(String typesignature, int line, int column) {
			super(line, column);
			this.typesignature = typesignature;
		}
		

		public String toString() {
			return typesignature;
		}
	
		
	}

	/**
	 * Array type
	 * @author Pietro Ferrara
	 *
	 */
	public static class ArrayType extends Type {
		private Type innerType;

		ArrayType(Type innerType, int line, int column) {
			super(line, column);
			this.innerType = innerType;
		}
		

		public String toString() {
			return innerType.toString()+"[]";
		}
	
		
	}
	/**
	 * Primitive types (int, long, double, etc..)
	 * @author Pietro Ferrara
	 *
	 */
	static abstract class PrimitiveType extends Type {
		PrimitiveType(int line, int column) {
			super(line, column);
		}

		static PrimitiveType parse(String s, int line, int column) {
			switch(s) {
				case "boolean" : return new BooleanType(line, column);
				case "char" : return new CharType(line, column);
				case "byte" : return new ByteType(line, column);
				case "short" : return new ShortType(line, column);
				case "int" : return new IntType(line, column);
				case "long" : return new LongType(line, column);
				case "float" : return new FloatType(line, column);
				case "double" : return new DoubleType(line, column);
				default : throw new UnsupportedOperationException("Primitive type "+s+" not yet supported");
			}
		}
	}

	/**
	 * boolean
	 * @author Pietro Ferrara
	 *
	 */
	public static class BooleanType extends PrimitiveType {
		BooleanType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "bool";
		}
	}
	
	/**
	 * char
	 * @author Pietro Ferrara
	 *
	 */
	public static class CharType extends PrimitiveType {
		CharType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "char";
		}
	}
	
	/**
	 * byte
	 * @author Pietro Ferrara
	 *
	 */
	public static class ByteType extends PrimitiveType {
		ByteType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "byte";
		}
	}
	
	/**
	 * short
	 * @author Pietro Ferrara
	 *
	 */
	public static class ShortType extends PrimitiveType {
		ShortType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "short";
		}
	}
	
	/**
	 * int
	 * @author Pietro Ferrara
	 *
	 */
	public static class IntType extends PrimitiveType {
		IntType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "int";
		}
	}
	
	/**
	 * long
	 * @author Pietro Ferrara
	 *
	 */
	public static class LongType extends PrimitiveType {
		LongType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "long";
		}
	}
	
	/**
	 * float
	 * @author Pietro Ferrara
	 *
	 */
	public static class FloatType extends PrimitiveType {
		FloatType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "float";
		}
	}
	
	/**
	 * double
	 * @author Pietro Ferrara
	 *
	 */
	public static class DoubleType extends PrimitiveType {
		public DoubleType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "double";
		}
	}
	
}
