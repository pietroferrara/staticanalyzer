package it.unive.dais.staticanalyzer.parser.java;

import org.antlr.v4.runtime.tree.*;

import it.unive.dais.staticanalyzer.cfg.*;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;
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

public class BodyVisitor extends JavaParserBaseVisitor<CFG> {
	
	@Override
	public CFG visitMethodBody(MethodBodyContext ctx) {
		BlockContext block = ctx.block();
		CFG result = new CFG();
		for(BlockStatementContext child : block.blockStatement())
			try {
				result.append(this.visitBlockStatement(child), null);
			} catch (ParsingException e) {
				throw new RuntimeException("Failure when building up the CFG of the body", e);
			}
		return result;
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
		if(ctx.ASSERT()!=null)
			throw new UnsupportedOperationException("Assert statements not yet supported");
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
		
		if(ctx.block() != null)
			return this.visitBlock(ctx.block());
		
		if(ctx.RETURN()!=null)
				try {
					if(ctx.expression().size()==0)
						return new CFG(new ReturnStatement(null));
					else if(ctx.expression().size()==1)
						return new CFG(new ReturnStatement(GenericVisitor.instance.visitExpression(ctx.expression().get(0))));
					else throw new UnsupportedOperationException("Multiple expressions in a return statement are not supported");
				} catch (ParsingException e) {
					throw new UnsupportedOperationException("Parsing of return statement "+ctx.getText()+" failed");
				}
		
		if(ctx.statementExpression!=null)
			try {
				return new CFG(GenericVisitor.instance.visitOnlyAssignmentInExpression(ctx.statementExpression));
			} catch (ParsingException e) {
				throw new UnsupportedOperationException("Parsing of assignment statement "+ctx.getText()+" failed");
			}
		
		if(ctx.IF()!=null) {
			try {
				CFG condition = this.visitParExpression(ctx.parExpression());
				CFG thenbranch = this.visitStatement(ctx.statement(0));
				CFG elsebranch = ctx.statement().size()==2 ? this.visitStatement(ctx.statement(1)) : null;
				CFG result = new CFG();
				result.append(condition, null);
				Statement afterCondition = result.getLastAdded();
				result.append(thenbranch, true);
				Statement lastAfterThen = result.getLastAdded();
				if(elsebranch!=null)
					result.append(elsebranch, afterCondition, false);
				SkipStatement joinStatement = new SkipStatement();
				if(! result.getLastAdded().isTerminatingStatement()) 
					result.append(joinStatement);
				if(! lastAfterThen.isTerminatingStatement())
					result.addAndCheckEdge(lastAfterThen, joinStatement, null);
				return result;
			} catch (ParsingException e) {
				throw new UnsupportedOperationException("Parsing of if statement "+ctx.getText()+" failed");
			}
		}


		if(ctx.WHILE()!=null) {
			try {
				CFG condition = this.visitParExpression(ctx.parExpression());
				CFG body = this.visitStatement(ctx.statement(0));
				CFG result = new CFG();
				result.append(condition, null);
				result.append(body, true);
				result.addAndCheckEdge(result.getLastAdded(), condition.getEntryPoint(), null);
				Statement skip = new SkipStatement();
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
		CFG result = new CFG();
		Type t = GenericVisitor.instance.visitTypeType(ctx.typeType());
		for(VariableDeclaratorContext vardec : ctx.variableDeclarators().variableDeclarator()) {
			VariableIdentifier varname = GenericVisitor.instance.visitVariableDeclarator(vardec);
			try {
				result.append(new VariableDeclaration(t, varname));
			} catch (ParsingException e) {
				throw new RuntimeException("Failure when building up the CFG of the body", e);
			}
		}
		return result;
	}
	
	@Override
	public CFG visitParExpression(ParExpressionContext ctx) {
		try {
			return new CFG(GenericVisitor.instance.visitExpression(ctx.expression()));
		} catch (ParsingException e) {
			throw new UnsupportedOperationException("Parsing of expression "+ctx.getText()+" failed");
		}
	}
	
}