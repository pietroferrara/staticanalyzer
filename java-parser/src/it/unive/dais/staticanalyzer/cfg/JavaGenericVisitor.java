package it.unive.dais.staticanalyzer.cfg;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import it.unive.dais.staticanalyzer.cfg.expression.FloatConstant;
import it.unive.dais.staticanalyzer.cfg.ParsedBlock;
import it.unive.dais.staticanalyzer.cfg.Type;
import it.unive.dais.staticanalyzer.cfg.Type.ObjectType;
import it.unive.dais.staticanalyzer.cfg.Type.PrimitiveType;
import it.unive.dais.staticanalyzer.cfg.expression.BinaryArithmeticExpression;
import it.unive.dais.staticanalyzer.cfg.expression.BooleanConstant;
import it.unive.dais.staticanalyzer.cfg.expression.BooleanExpression;
import it.unive.dais.staticanalyzer.cfg.expression.CharConstant;
import it.unive.dais.staticanalyzer.cfg.expression.Constant;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.ArrayAccessExpression;
import it.unive.dais.staticanalyzer.cfg.expression.ArrayCreationExpression;
import it.unive.dais.staticanalyzer.cfg.expression.AssignableExpression;
import it.unive.dais.staticanalyzer.cfg.expression.IntegerConstant;
import it.unive.dais.staticanalyzer.cfg.expression.NegatedBooleanExpression;
import it.unive.dais.staticanalyzer.cfg.expression.NullConstant;
import it.unive.dais.staticanalyzer.cfg.expression.NumericalComparisonExpression;
import it.unive.dais.staticanalyzer.cfg.expression.StringConstant;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;
import it.unive.dais.staticanalyzer.cfg.statement.Assignment;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.ArrayCreatorRestContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.ClassOrInterfaceTypeContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.CreatedNameContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.CreatorContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.ExpressionContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.FloatLiteralContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.IntegerLiteralContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.LiteralContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.PrimaryContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.PrimitiveTypeContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.TypeTypeContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.VariableDeclaratorContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.VariableDeclaratorIdContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParserBaseVisitor;

public class JavaGenericVisitor extends JavaParserBaseVisitor<ParsedBlock>{

	public static final JavaGenericVisitor instance = new JavaGenericVisitor();
	
	private JavaGenericVisitor() {
	}
	
	@Override
	public Type visitTypeType(TypeTypeContext ctx) {
		if(ctx.annotation()!=null)
			throw new UnsupportedOperationException("Annotation in types not yet supported");
		if(isArrayType(ctx))
			return this.visitArrayType(ctx);
		if(ctx.classOrInterfaceType()!=null)
			return this.visitClassOrInterfaceType(ctx.classOrInterfaceType());
		else return this.visitPrimitiveType(ctx.primitiveType());
	}

	private Type visitArrayType(TypeTypeContext ctx) {
		Object inner = ctx.children.get(0);
		if(inner instanceof PrimitiveTypeContext) {
			if(ctx.children.size()!=3 || ! ctx.children.get(1).toString().equals("[") || ! ctx.children.get(2).toString().equals("]"))
				throw new UnsupportedOperationException("Only monodimensional arrays are supported");
			Type innerType = this.visitPrimitiveType((PrimitiveTypeContext) inner);
			return new Type.ArrayType(innerType,  ctx.start.getLine(), ctx.start.getStartIndex());
		}
		else throw new UnsupportedOperationException("Only arrays of primitive types are supported");
	}

	private boolean isArrayType(ParserRuleContext ctx) {
		for(ParseTree child : ctx.children)
			if(child instanceof TerminalNode && ((TerminalNode) child).getText().equals("["))
				return true;
		return false;
	}

	private void avoidArrays(ParserRuleContext ctx) {
		for(ParseTree child : ctx.children)
			if(child instanceof TerminalNode && ((TerminalNode) child).getText().equals("["))
				throw new UnsupportedOperationException("Arrays are not yet supported");
	}
	
	@Override
	public Type visitPrimitiveType(PrimitiveTypeContext ctx) {
		return PrimitiveType.parse(ctx.children.get(0).getText(), ctx.start.getLine(), ctx.start.getStartIndex());
	}
	
	@Override
	public Type visitClassOrInterfaceType(ClassOrInterfaceTypeContext ctx) {
		return new ObjectType(ctx.getText(), ctx.start.getLine(), ctx.start.getStartIndex());
	}
	
	@Override
	public VariableIdentifier visitVariableDeclarator(VariableDeclaratorContext ctx) {
		if(ctx.variableInitializer()!=null)
			throw new UnsupportedOperationException("Assignemnts in variable declarations are not yet supported");
		return this.visitVariableDeclaratorId(ctx.variableDeclaratorId());
	}
	
	@Override
	public VariableIdentifier visitVariableDeclaratorId(VariableDeclaratorIdContext ctx) {
		avoidArrays(ctx);
		return new VariableIdentifier(ctx.IDENTIFIER().getText(), ctx.start.getLine(), ctx.start.getStartIndex());
	}

	@Override
	public Expression visitExpression(ExpressionContext ctx) {
		if(ctx.methodCall()!=null)
			throw new UnsupportedOperationException("Method calls not yet supported");
		if(ctx.NEW()!=null) {
			CreatorContext creator = (CreatorContext) ctx.children.get(1);
			if(creator.children.size()==2 &&
				creator.children.get(0) instanceof CreatedNameContext && 
				creator.children.get(1) instanceof ArrayCreatorRestContext
			) {
				ParseTree innerType = creator.getChild(0).getChild(0);
				if(! (innerType instanceof PrimitiveTypeContext))
					throw new UnsupportedOperationException("Only arrays with primitive types are supported");
				Type arraytype = this.visitPrimitiveType((PrimitiveTypeContext) innerType);
				ParseTree arrayRestCreator = creator.getChild(1);
				if(! (arrayRestCreator instanceof ArrayCreatorRestContext))
					throw new UnsupportedOperationException("Unsupported creation of arrays");
				Expression exp = this.visitArrayCreatorRest((ArrayCreatorRestContext) arrayRestCreator);
				return new ArrayCreationExpression(arraytype, exp, ctx.start.getLine(), ctx.start.getStartIndex());
			}
			throw new UnsupportedOperationException("Instantation of object not yet supported");
			
		}
		if(ctx.getChildCount()==4 && ctx.getChild(1).toString().equals("[") && ctx.getChild(3).toString().equals("]")) {
			VariableIdentifier variableName = (VariableIdentifier) this.visitExpression((ExpressionContext) ctx.getChild(0));
			Expression index = this.visitExpression((ExpressionContext) ctx.getChild(2));
			return new ArrayAccessExpression(variableName, index, ctx.start.getLine(), ctx.start.getStartIndex());
		}
		avoidArrays(ctx);
		if(ctx.typeType()!=null)
			throw new UnsupportedOperationException("Type casting not yet supported");
		if(ctx.postfix!=null)
			throw new UnsupportedOperationException("Expression with postfix operators (e.g., ++ and --) not yet supported");
		if(ctx.lambdaExpression()!=null)
			throw new UnsupportedOperationException("Lambda expressions not yet supported");
		if(ctx.primary()!=null)
			return this.visitPrimary(ctx.primary());
		if(ctx.prefix!=null) {
			String prefixValue = ctx.prefix.getText();
			switch(prefixValue) {
				case "!": return new NegatedBooleanExpression(this.visitExpression(ctx.expression(0)), ctx.start.getLine(), ctx.start.getStartIndex());
				case "+": return this.visitExpression(ctx.expression(0));
				case "-": return new BinaryArithmeticExpression(new IntegerConstant(0, ctx.start.getLine(), ctx.start.getStartIndex()), this.visitExpression(ctx.expression(0)), "-", ctx.start.getLine(), ctx.start.getStartIndex());
				default : throw new UnsupportedOperationException("Prefix operator "+prefixValue+" not yet supported"); 
			}
		}
		if(ctx.bop == null)
			throw new UnsupportedOperationException("Binary operator cannot be null here!");
		
		
		String binaryOperator = ctx.bop.getText();
		switch(binaryOperator) {
			case ".": throw new UnsupportedOperationException("Dereference operator not yet supported");
			case "?": throw new UnsupportedOperationException("? operator not yet supported");
			case "instanceof": throw new UnsupportedOperationException("instanceof operator not yet supported");
			case "+=": 
			case "-=":
			case "*=":
			case "/=":
			case "&=":
			case "|=":
			case "^=":
			case ">>=":
			case ">>>=":
			case "<<=":
			case "%=": throw new UnsupportedOperationException("Assignment operator "+binaryOperator+" not yet supported");
			case "=": throw new UnsupportedOperationException("Assignment is not yet supported as an expression");
			case "+":
			case "-":
			case "*":
			case "/":
			case "%":
			case "<<":
			case ">>>":
			case ">>":
			case "&":
			case "^":
			case "|": return new BinaryArithmeticExpression(this.visitExpression(ctx.expression(0)), this.visitExpression(ctx.expression(1)), binaryOperator, ctx.start.getLine(), ctx.start.getStartIndex());
			case "<=":
			case ">=":
			case "<":
			case ">":
			case "==":
			case "!=": return new NumericalComparisonExpression(this.visitExpression(ctx.expression(0)), this.visitExpression(ctx.expression(1)), binaryOperator, ctx.start.getLine(), ctx.start.getStartIndex());
			case "&&":
			case "||": return new BooleanExpression(this.visitExpression(ctx.expression(0)), this.visitExpression(ctx.expression(1)), binaryOperator, ctx.start.getLine(), ctx.start.getStartIndex());	
		}
		throw new UnsupportedOperationException("Unsupported expression: "+ctx.getText());
	}
	
	public Statement visitOnlyAssignmentInExpression(ExpressionContext ctx) {
		avoidArrays(ctx);
		String binaryOperator = ctx.bop.getText();
		switch(binaryOperator) {
			case "=":
				Expression assignedVariable = this.visitExpression(ctx.expression(0));
				if(assignedVariable instanceof AssignableExpression)
					return new Assignment((AssignableExpression) assignedVariable, this.visitExpression(ctx.expression(1)), ctx.start.getLine(), ctx.start.getStartIndex());
				else throw new UnsupportedOperationException("Assignment of "+assignedVariable+" (type "+assignedVariable.getClass().getTypeName()+") not yet supported");
		}
		throw new UnsupportedOperationException("Unsupported expression: "+ctx.getText());
	}
	
	@Override
	public Expression visitPrimary(PrimaryContext ctx) {
		if(ctx.nonWildcardTypeArguments()!=null)
			throw new UnsupportedOperationException("Generic types not yet supported");
		if(ctx.CLASS()!=null)
			throw new UnsupportedOperationException("Access to .class not yet supported");
		if(ctx.expression()!=null)
			return this.visitExpression(ctx.expression());
		if(ctx.THIS()!=null)
			return new VariableIdentifier("this", ctx.start.getLine(), ctx.start.getStartIndex());
		if(ctx.SUPER()!=null)
			return new VariableIdentifier("super", ctx.start.getLine(), ctx.start.getStartIndex());
		if(ctx.IDENTIFIER()!=null)
			return new VariableIdentifier(ctx.IDENTIFIER().getText(), ctx.start.getLine(), ctx.start.getStartIndex());
		if(ctx.literal()!=null)
			return this.visitLiteral(ctx.literal());
		throw new UnsupportedOperationException("Unsupported primary expression: "+ctx.getText());
	}
	
	@Override
	public Expression visitArrayCreatorRest(ArrayCreatorRestContext ctx) {
		if(ctx.getChild(0).toString().equals("[") && ctx.getChild(2).toString().equals("]")) {
			ExpressionContext size = (ExpressionContext) ctx.getChild(1);
			return this.visitExpression(size);
		}
		else throw new UnsupportedOperationException("Not supported creation of arrays");
	}
	
	@Override
	public Constant visitLiteral(LiteralContext ctx) {
		if(ctx.NULL_LITERAL()!=null)
			return new NullConstant(ctx.start.getLine(), ctx.start.getStartIndex());
		if(ctx.BOOL_LITERAL()!=null)
			return new BooleanConstant(Boolean.parseBoolean(ctx.getText()), ctx.start.getLine(), ctx.start.getStartIndex());
		if(ctx.integerLiteral()!=null)
			return this.visitIntegerLiteral(ctx.integerLiteral());
		if(ctx.floatLiteral()!=null)
			return this.visitFloatLiteral(ctx.floatLiteral());
		if(ctx.CHAR_LITERAL()!=null)
			return new CharConstant(ctx.getText().charAt(1), ctx.start.getLine(), ctx.start.getStartIndex());
		if(ctx.STRING_LITERAL()!=null)
			return new StringConstant(ctx.getText().substring(1, ctx.getText().length()-1), ctx.start.getLine(), ctx.start.getStartIndex());
		throw new UnsupportedOperationException("Unsupported primary expression: "+ctx.getText());
	}
	
	@Override
	public IntegerConstant visitIntegerLiteral(IntegerLiteralContext ctx) {
		long value;
		if(ctx.DECIMAL_LITERAL()!=null)
			value = Long.decode(ctx.DECIMAL_LITERAL().getText());
		else if(ctx.HEX_LITERAL()!=null)
				value = Long.decode(ctx.HEX_LITERAL().getText());
		else if(ctx.OCT_LITERAL()!=null)
			value = Long.decode(ctx.OCT_LITERAL().getText());
		else if(ctx.BINARY_LITERAL()!=null)
			value = Long.decode(ctx.BINARY_LITERAL().getText());
		else throw new UnsupportedOperationException("Numerical constant not supported: "+ctx.getText());
		return new IntegerConstant(value, ctx.start.getLine(), ctx.start.getStartIndex());
	}
	
	@Override
	public FloatConstant visitFloatLiteral(FloatLiteralContext ctx) {
		double value;
		if(ctx.FLOAT_LITERAL()!=null)
			value = Double.valueOf(ctx.FLOAT_LITERAL().getText());
		else if(ctx.HEX_FLOAT_LITERAL()!=null)
				value = Double.valueOf(ctx.HEX_FLOAT_LITERAL().getText());
		else throw new UnsupportedOperationException("Numerical constant not supported: "+ctx.getText());
		return new FloatConstant(value, ctx.start.getLine(), ctx.start.getStartIndex());
	}
	

}
