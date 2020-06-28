package it.unive.dais.staticanalyzer.cfg;

import org.antlr.v4.runtime.tree.*;

import it.unive.dais.staticanalyzer.cfg.*;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;
import it.unive.dais.staticanalyzer.cfg.statement.AssertStatement;
import it.unive.dais.staticanalyzer.cfg.statement.ReturnStatement;
import it.unive.dais.staticanalyzer.cfg.statement.SkipStatement;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;
import it.unive.dais.staticanalyzer.cfg.statement.VariableDeclaration;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.BlockContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.BlockStatementContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.LocalVariableDeclarationContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.MethodBodyContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.ParExpressionContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.StatementContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser.VariableDeclaratorContext;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParserBaseVisitor;

public class JavaBodyVisitor extends JavaParserBaseVisitor<CFG> {
	
	@Override
	public CFG visitBlock(BlockContext block) {
		CFG result = new CFG(block.start.getLine(), block.start.getStartIndex());
		for(BlockStatementContext child : block.blockStatement())
			try {
				result.append(this.visitBlockStatement(child), null);
			} catch (ParsingException e) {
				throw new RuntimeException("Failure when building up the CFG of the body", e);
			}
		return result;
	}
	
	@Override
	public CFG visitMethodBody(MethodBodyContext ctx) {
		return this.visit(ctx.block());
	}
	
	@Override
	public CFG visitBlockStatement(BlockStatementContext ctx) {
		if(ctx.localTypeDeclaration()!=null)
			throw new UnsupportedOperationException("Inner classes are not yet supported");
		if(ctx.localVariableDeclaration()!=null)
			return this.visitLocalVariableDeclaration(ctx.localVariableDeclaration());
		else return this.visitStatement(ctx.statement());
	}
	
	@Override
	public CFG visitErrorNode(ErrorNode node) {
		System.err.println("Error while parsing "+node);
		throw new UnsupportedOperationException();
	}
	
	@Override
	public CFG visitStatement(StatementContext ctx) {
		if(ctx.DO()!=null)
			throw new UnsupportedOperationException("Do-while statements not yet supported");
		if(ctx.TRY()!=null)
			throw new UnsupportedOperationException("Exceptions not yet supported");
		if(ctx.SWITCH()!=null)
			throw new UnsupportedOperationException("Switch statements not yet supported");
		if(ctx.SYNCHRONIZED()!=null)
			throw new UnsupportedOperationException("Synchronized statements not yet supported");
		if(ctx.THROW()!=null)
			throw new UnsupportedOperationException("Exceptions not yet supported");
		if(ctx.BREAK()!=null)
			throw new UnsupportedOperationException("Break statements not yet supported");
		if(ctx.CONTINUE()!=null)
			throw new UnsupportedOperationException("Continue statements not yet supported");
		if(ctx.identifierLabel!=null)
			throw new UnsupportedOperationException("Labels not yet supported");
		if(ctx.FOR()!=null) 
			throw new UnsupportedOperationException("For statements not yet supported");
		
		if(ctx.ASSERT()!=null) {
			try {
				return new CFG(new AssertStatement(JavaGenericVisitor.instance.visitExpression(ctx.expression(0)), ctx.start.getLine(), ctx.start.getStartIndex()));
			} catch (ParsingException e) {
				throw new UnsupportedOperationException("Parsing of assert statement "+ctx.getText()+" failed");
			}
		}
		
		
		if(ctx.block() != null)
			return this.visitBlock(ctx.block());
		
		if(ctx.RETURN()!=null)
				try {
					if(ctx.expression().size()==0)
						return new CFG(new ReturnStatement(null, ctx.start.getLine(), ctx.start.getStartIndex()));
					else if(ctx.expression().size()==1)
						return new CFG(new ReturnStatement(JavaGenericVisitor.instance.visitExpression(ctx.expression().get(0)), ctx.start.getLine(), ctx.start.getStartIndex()));
					else throw new UnsupportedOperationException("Multiple expressions in a return statement are not supported");
				} catch (ParsingException e) {
					throw new UnsupportedOperationException("Parsing of return statement "+ctx.getText()+" failed");
				}
		
		if(ctx.statementExpression!=null)
			try {
				return new CFG(JavaGenericVisitor.instance.visitOnlyAssignmentInExpression(ctx.statementExpression));
			} catch (ParsingException e) {
				throw new UnsupportedOperationException("Parsing of assignment statement "+ctx.getText()+" failed");
			}
		
		if(ctx.IF()!=null) {
			try {
				CFG condition = this.visitParExpression(ctx.parExpression());
				CFG thenbranch = this.visitStatement(ctx.statement(0));
				CFG elsebranch = ctx.statement().size()==2 ? this.visitStatement(ctx.statement(1)) : new CFG(new SkipStatement(thenbranch.getLastAdded().getLine(), thenbranch.getLastAdded().getColumn()+1));
				CFG result = new CFG(ctx.start.getLine(), ctx.start.getStartIndex());
				result.append(condition, null);
				Statement afterCondition = result.getLastAdded();
				result.append(thenbranch, true);
				Statement lastAfterThen = result.getLastAdded();
				result.append(elsebranch, afterCondition, false);
				SkipStatement joinStatement = new SkipStatement(elsebranch.getLastAdded().getLine(), elsebranch.getLastAdded().getColumn()+1);
				if(! result.getLastAdded().isTerminatingStatement()) 
					result.append(joinStatement);
				if(! lastAfterThen.isTerminatingStatement() && elsebranch!=null)
					result.addAndCheckEdge(lastAfterThen, joinStatement, null);
				return result;
			} catch (ParsingException e) {
				throw new UnsupportedOperationException("Parsing of if statement "+ctx.getText()+" failed", e);
			}
		}


		if(ctx.WHILE()!=null) {
			try {
				CFG condition = this.visitParExpression(ctx.parExpression());
				CFG body = this.visitStatement(ctx.statement(0));
				CFG result = new CFG(ctx.start.getLine(), ctx.start.getStartIndex());
				result.append(condition, null);
				result.append(body, true);
				result.append(new SkipStatement(result.getLastAdded().getLine(), result.getLastAdded().getColumn()+1));
				result.addAndCheckEdge(result.getLastAdded(), condition.getEntryPoint(), null);
				Statement skip = new SkipStatement(result.getLastAdded().getLine(), result.getLastAdded().getColumn()+1);
				result.append(new CFG(skip), condition.getLastAdded(), false);
				return result;
			} catch (ParsingException e) {
				throw new UnsupportedOperationException("Parsing of if statement "+ctx.getText()+" failed");
			}
		}
		
		
		
		
		throw new UnsupportedOperationException("Statement "+ctx.getText()+" not yet supported");
	}
	
	@Override
	public CFG visitLocalVariableDeclaration(LocalVariableDeclarationContext ctx) {
		CFG result = new CFG(ctx.start.getLine(), ctx.start.getStartIndex());
		Type t = JavaGenericVisitor.instance.visitTypeType(ctx.typeType());
		for(VariableDeclaratorContext vardec : ctx.variableDeclarators().variableDeclarator()) {
			VariableIdentifier varname = JavaGenericVisitor.instance.visitVariableDeclarator(vardec);
			try {
				result.append(new VariableDeclaration(t, varname, ctx.start.getLine(), ctx.start.getStartIndex()));
			} catch (ParsingException e) {
				throw new RuntimeException("Failure when building up the CFG of the body", e);
			}
		}
		return result;
	}
	
	@Override
	public CFG visitParExpression(ParExpressionContext ctx) {
		try {
			return new CFG(JavaGenericVisitor.instance.visitExpression(ctx.expression()));
		} catch (ParsingException e) {
			throw new UnsupportedOperationException("Parsing of expression "+ctx.getText()+" failed");
		}
	}
	
}