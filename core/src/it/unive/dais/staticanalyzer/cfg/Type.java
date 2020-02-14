package it.unive.dais.staticanalyzer.cfg;

public abstract class Type extends ParsedBlock {
	
	public Type(int line, int column) {
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
	
	public static class ObjectType extends Type {
		private String typesignature;

		public ObjectType(String typesignature, int line, int column) {
			super(line, column);
			this.typesignature = typesignature;
		}
		

		public String toString() {
			return typesignature;
		}
	
		
	}

	public static abstract class PrimitiveType extends Type {
		public PrimitiveType(int line, int column) {
			super(line, column);
		}

		public static PrimitiveType parse(String s, int line, int column) {
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

	public static class BooleanType extends PrimitiveType {
		public BooleanType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "bool";
		}
	}
	public static class CharType extends PrimitiveType {
		public CharType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "char";
		}
	}
	public static class ByteType extends PrimitiveType {
		public ByteType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "byte";
		}
	}
	public static class ShortType extends PrimitiveType {
		public ShortType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "short";
		}
	}
	public static class IntType extends PrimitiveType {
		public IntType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "int";
		}
	}
	public static class LongType extends PrimitiveType {
		public LongType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "long";
		}
	}
	public static class FloatType extends PrimitiveType {
		public FloatType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "float";
		}
	}
	public static class DoubleType extends PrimitiveType {
		public DoubleType(int line, int column) {
			super(line, column);
		}

		public String toString() {
			return "double";
		}
	}
	
}
