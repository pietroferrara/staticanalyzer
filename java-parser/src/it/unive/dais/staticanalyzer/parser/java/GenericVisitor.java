package it.unive.dais.staticanalyzer.parser.java;

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
import it.unive.dais.staticanalyzer.cfg.expression.IntegerConstant;
import it.unive.dais.staticanalyzer.cfg.expression.NegatedBooleanExpression;
import it.unive.dais.staticanalyzer.cfg.expression.NullConstant;
import it.unive.dais.staticanalyzer.cfg.expression.NumericalComparisonExpression;
import it.unive.dais.staticanalyzer.cfg.expression.StringConstant;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;
import it.unive.dais.staticanalyzer.cfg.statement.Assignment;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.ClassOrInterfaceTypeContext;
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

public class GenericVisitor extends JavaParserBaseVisitor<ParsedBlock>{

	public static final GenericVisitor instance = new GenericVisitor();
	
	private GenericVisitor() {
	}
	
	@Override
	public Type visitTypeType(TypeTypeContext ctx) {
		if(ctx.annotation()!=null)
			throw new UnsupportedOperationException("Annotation in types not yet supported");
		avoidArrays(ctx);
		if(ctx.classOrInterfaceType()!=null)
			return this.visitClassOrInterfaceType(ctx.classOrInterfaceType());
		else return this.visitPrimitiveType(ctx.primitiveType());
	}

	private void avoidArrays(ParserRuleContext ctx) {
		for(ParseTree child : ctx.children)
			if(child instanceof TerminalNode && ((TerminalNode) child).getText().equals("["))
				throw new UnsupportedOperationException("Arrays are not yet supported");
	}
	
	@Override
	public Type visitPrimitiveType(PrimitiveTypeContext ctx) {
		return PrimitiveType.parse(ctx.children.get(0).getText());
	}
	
	@Override
	public Type visitClassOrInterfaceType(ClassOrInterfaceTypeContext ctx) {
		return new ObjectType(ctx.getText());
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
		return new VariableIdentifier(ctx.IDENTIFIER().getText());
	}
	
	@Override
	public Expression visitExpression(ExpressionContext ctx) {
		avoidArrays(ctx);
		if(ctx.methodCall()!=null)
			throw new UnsupportedOperationException("Method calls not yet supported");
		if(ctx.NEW()!=null)
			throw new UnsupportedOperationException("Instantation of object not yet supported");
		if(ctx.typeType()!=null)
			throw new UnsupportedOperationException("Type casting not yet supported");
		if(ctx.postfix!=null)
			throw new UnsupportedOperationException("Expression with postfix operators (e.g., ++ and --) not yet supported");
		if(ctx.lambdaExpression()!=null)
			throw new UnsupportedOperationException("Lambda expressions not yet supported");
		if(ctx.primary()!=null)
			return this.visitPrimary(ctx.primary());
		if(ctx.bop == null)
			throw new UnsupportedOperationException("Binary operator cannot be null here!");
		if(ctx.prefix!=null) {
			String prefixValue = ctx.prefix.getText();
			switch(prefixValue) {
				case "!": return new NegatedBooleanExpression(this.visitExpression(ctx.expression(0)));
				default : throw new UnsupportedOperationException("Expression with prefix operators (e.g., ++ and --) not yet supported"); 
			}
		}
		
		
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
			case "=": return new Assignment(this.visitExpression(ctx.expression(0)), this.visitExpression(ctx.expression(1)));
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
			case "|": return new BinaryArithmeticExpression(this.visitExpression(ctx.expression(0)), this.visitExpression(ctx.expression(1)), binaryOperator);
			case "<=":
			case ">=":
			case "<":
			case ">":
			case "==":
			case "!=": return new NumericalComparisonExpression(this.visitExpression(ctx.expression(0)), this.visitExpression(ctx.expression(1)), binaryOperator);
			case "&&":
			case "||": return new BooleanExpression(this.visitExpression(ctx.expression(0)), this.visitExpression(ctx.expression(1)), binaryOperator);	
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
			return new VariableIdentifier("this");
		if(ctx.SUPER()!=null)
			return new VariableIdentifier("super");
		if(ctx.IDENTIFIER()!=null)
			return new VariableIdentifier(ctx.IDENTIFIER().getText());
		if(ctx.literal()!=null)
			return this.visitLiteral(ctx.literal());
		throw new UnsupportedOperationException("Unsupported primary expression: "+ctx.getText());
	}
	
	@Override
	public Constant visitLiteral(LiteralContext ctx) {
		if(ctx.NULL_LITERAL()!=null)
			return new NullConstant();
		if(ctx.BOOL_LITERAL()!=null)
			return new BooleanConstant(Boolean.parseBoolean(ctx.getText()));
		if(ctx.integerLiteral()!=null)
			return this.visitIntegerLiteral(ctx.integerLiteral());
		if(ctx.floatLiteral()!=null)
			return this.visitFloatLiteral(ctx.floatLiteral());
		if(ctx.CHAR_LITERAL()!=null)
			return new CharConstant(ctx.getText().charAt(1));
		if(ctx.STRING_LITERAL()!=null)
			return new StringConstant(ctx.getText().substring(1, ctx.getText().length()-1));
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
		return new IntegerConstant(value);
	}
	
	@Override
	public FloatConstant visitFloatLiteral(FloatLiteralContext ctx) {
		double value;
		if(ctx.FLOAT_LITERAL()!=null)
			value = Long.decode(ctx.FLOAT_LITERAL().getText());
		else if(ctx.HEX_FLOAT_LITERAL()!=null)
				value = Integer.decode(ctx.HEX_FLOAT_LITERAL().getText());
		else throw new UnsupportedOperationException("Numerical constant not supported: "+ctx.getText());
		return new FloatConstant(value);
	}
	

}
