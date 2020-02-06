package it.unive.dais.staticanalyzer.cfg;

public abstract class Type implements ParsedBlock {
	
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

		public ObjectType(String typesignature) {
			super();
			this.typesignature = typesignature;
		}
		

		public String toString() {
			return typesignature;
		}
	
		
	}

	public static abstract class PrimitiveType extends Type {
		public static PrimitiveType parse(String s) {
			switch(s) {
				case "boolean" : return BooleanType.instance;
				case "char" : return CharType.instance;
				case "byte" : return ByteType.instance;
				case "short" : return ShortType.instance;
				case "int" : return IntType.instance;
				case "long" : return LongType.instance;
				case "float" : return FloatType.instance;
				case "double" : return DoubleType.instance;
				default : throw new UnsupportedOperationException("Primitive type "+s+" not yet supported");
			}
		}
	}

	public static class BooleanType extends PrimitiveType {
		public static final BooleanType instance = new BooleanType();
		
		public String toString() {
			return "bool";
		}
	}
	public static class CharType extends PrimitiveType {
		public static final CharType instance = new CharType();
		
		public String toString() {
			return "char";
		}
	}
	public static class ByteType extends PrimitiveType {
		public static final ByteType instance = new ByteType();
		
		public String toString() {
			return "byte";
		}
	}
	public static class ShortType extends PrimitiveType {
		public static final ShortType instance = new ShortType();
		
		public String toString() {
			return "short";
		}
	}
	public static class IntType extends PrimitiveType {
		public static final IntType instance = new IntType();
		
		public String toString() {
			return "int";
		}
	}
	public static class LongType extends PrimitiveType {
		public static final LongType instance = new LongType();
		
		public String toString() {
			return "long";
		}
	}
	public static class FloatType extends PrimitiveType {
		public static final FloatType instance = new FloatType();
		
		public String toString() {
			return "float";
		}
	}
	public static class DoubleType extends PrimitiveType {
		public static final DoubleType instance = new DoubleType();
		
		public String toString() {
			return "double";
		}
	}
	
}
