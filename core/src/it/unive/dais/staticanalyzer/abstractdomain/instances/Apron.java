package it.unive.dais.staticanalyzer.abstractdomain.instances;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.cfg.Type;
import it.unive.dais.staticanalyzer.cfg.Type.*;
import it.unive.dais.staticanalyzer.cfg.expression.BinaryArithmeticExpression;
import it.unive.dais.staticanalyzer.cfg.expression.BooleanExpression;
import it.unive.dais.staticanalyzer.cfg.expression.Constant;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.FloatConstant;
import it.unive.dais.staticanalyzer.cfg.expression.IntegerConstant;
import it.unive.dais.staticanalyzer.cfg.expression.NegatedBooleanExpression;
import it.unive.dais.staticanalyzer.cfg.expression.NumericalComparisonExpression;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;
import it.unive.dais.staticanalyzer.cfg.statement.Assignment;
import it.unive.dais.staticanalyzer.cfg.statement.SkipStatement;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;
import it.unive.dais.staticanalyzer.cfg.statement.VariableDeclaration;


import apron.*;
import gmp.Mpfr;

public class Apron implements SemanticDomain<Apron>, Lattice<Apron> {

	public static Manager manager;
	
	private final Abstract1 state;
	
	public enum NumericalDomain {
	    Box, Octagon, Polka, PolkaEq, PolkaGrid, PplGrid, PplPoly;
	}
	
	public static void setManager(NumericalDomain numericalDomain) {
		if(manager!=null)
			throw new UnsupportedOperationException("The Apron manager can be set only once");
		switch(numericalDomain) {
			case Box: manager=new Box(); break;
			case Octagon: manager=new Octagon(); break;
			case Polka: manager=new Polka(false); break;
			case PolkaEq: manager=new PolkaEq(); break;
			case PolkaGrid: manager=new PolkaGrid(true); break;
			case PplGrid: manager=new PplGrid(); break;
			case PplPoly: manager=new PplPoly(false); break;
			default: throw new UnsupportedOperationException("Numerical domain "+numericalDomain+" unknown in Apron");
		}
	}
	
	public Apron() {
		try {
			String[] vars = {"<ret>"}; //Variable needed to represent the value returned
			state = new Abstract1(manager, new apron.Environment(new String[0], vars));
		}
		catch(ApronException e) {
			throw new UnsupportedOperationException("Apron library crashed", e);
		}
	}
	
	private Apron(Abstract1 state) {
		this.state = state;
	}
	
	@Override
	public Apron smallStepSemantics(Statement st) {
		try {
			if(st instanceof SkipStatement)
				return new Apron(this.state);
			if(st instanceof VariableDeclaration) {
				VariableDeclaration var = (VariableDeclaration) st;
				Type typ = var.getType();
				apron.Environment env = state.getEnvironment();
				Var variable = new StringVar(var.getId().getName());
				Var[] vars = {variable};
				if(typ instanceof IntType || typ instanceof CharType || typ instanceof ByteType || typ instanceof LongType)
					env = env.add(vars, new Var[0]);
				else if(typ instanceof DoubleType || typ instanceof FloatType)
					env = env.add(new Var[0], vars);
				Abstract1 newState = state.changeEnvironmentCopy(manager, env, false);
				return new Apron(newState);
			}
			if(st instanceof Assignment) {
				Assignment assignment = (Assignment) st;
				String assignedVariable = assignment.getAssignedVariable().getName();
				Var variable = new StringVar(assignedVariable);
				return new Apron(state.assignCopy(manager, variable, new Texpr1Intern(state.getEnvironment(), convertExpressionToApronFormat(((Assignment) st).getExpression())), null));
			}
			if(st instanceof Expression)
				return new Apron(state);
		}
		catch(ApronException e) {
			throw new UnsupportedOperationException("Apron library crashed", e);
		}
		throw new UnsupportedOperationException("Statement "+st.getClass().getTypeName()+" not yet supported");
	}

	@Override
	public Apron assume(Expression expr) {
		try {
			if(expr instanceof NumericalComparisonExpression)
				return new Apron(state.meetCopy(manager, convertNumericalComparisonToApronFormat((NumericalComparisonExpression) expr)));
			else if(expr instanceof NegatedBooleanExpression) {
				Expression inner = ((NegatedBooleanExpression) expr).getExpression();
				if(inner instanceof NumericalComparisonExpression)
					return assume(((NumericalComparisonExpression) inner).negate());
			}
		}
		catch(ApronException e) {
			throw new UnsupportedOperationException("Apron library crashed", e);
		}
		throw new UnsupportedOperationException("Assumption of expression "+expr.getClass().getTypeName()+" not yet supported");
	}
	
	private Tcons1 convertNumericalComparisonToApronFormat(NumericalComparisonExpression expr) {
		Expression combinedExpr = new BinaryArithmeticExpression(expr.getLeft(), expr.getRight(), "-"); //Apron supports only "expr <comparison> 0", so we need to move everything on the left sode 
		switch(expr.getOperator()) {
			case "==":
			case "!=":
			case ">":
			case ">=": return new Tcons1(state.getEnvironment(), convertComparisonOperator(expr.getOperator()), convertExpressionToApronFormat(combinedExpr));
			//For the other cases Need to revert the operator since Apron has a limited support for comparison operators
			case "<": combinedExpr = new BinaryArithmeticExpression(new IntegerConstant(0), combinedExpr, "-");
				return new Tcons1(state.getEnvironment(), convertComparisonOperator(">="), convertExpressionToApronFormat(combinedExpr));
			case "<=": combinedExpr = new BinaryArithmeticExpression(new IntegerConstant(0), combinedExpr, "-");
				return new Tcons1(state.getEnvironment(), convertComparisonOperator(">"), convertExpressionToApronFormat(combinedExpr));
			default: throw new UnsupportedOperationException("Comparison operator "+expr.getOperator()+" not yet supported"); 
		}
	}
	
	private int convertComparisonOperator(String operator) {
		switch(operator) {
			case ">=": return Tcons1.SUPEQ;
			case ">": return Tcons1.SUP;
			case "==": return Tcons1.EQ;
			case "!=": return Tcons1.DISEQ;
			default: throw new UnsupportedOperationException("Comparison operator "+operator+" not yet supported by Apron interface");
		}
	}

	private Texpr1Node convertExpressionToApronFormat(Expression expr) {
		if(expr instanceof BooleanExpression)
			throw new UnsupportedOperationException("Boolean expressions not yet supported by Apron interface");
		if(expr instanceof NegatedBooleanExpression)
			throw new UnsupportedOperationException("Negation of Boolean expressions not yet supported by Apron interface");
		if(expr instanceof NumericalComparisonExpression)
			throw new UnsupportedOperationException("Numerical comparisons not yet supported by Apron interface");
		if(expr instanceof VariableIdentifier) {
			String name = ((VariableIdentifier) expr).getName();
			return new Texpr1VarNode(name);
		}
		if(expr instanceof Constant) {
			Coeff coeff;
			if(expr instanceof IntegerConstant) {
				int value = (int) ((IntegerConstant) expr).getValue();
				coeff = new MpqScalar(value);
			}
			else if(expr instanceof FloatConstant) {
				double value = ((FloatConstant) expr).getValue();
				coeff = new MpfrScalar(value, Mpfr.getDefaultPrec());
			}
			else coeff = new MpfrScalar();
			return new Texpr1CstNode(coeff);
		}
		else if(expr instanceof BinaryArithmeticExpression) {
			BinaryArithmeticExpression binaryexpr = (BinaryArithmeticExpression) expr;
			return new Texpr1BinNode(convertOperator(binaryexpr.getOperator()), convertExpressionToApronFormat(binaryexpr.getLeft()), convertExpressionToApronFormat(binaryexpr.getRight()));
		}
		else throw new UnsupportedOperationException("Expression "+expr.getClass().getTypeName()+" not yet supported by Apron interface");
	}

	private int convertOperator(String operator) {
		switch(operator) {
			case "+": return Texpr1BinNode.OP_ADD;
			case "*": return Texpr1BinNode.OP_MUL;
			case "-": return Texpr1BinNode.OP_SUB;
			case "/": return Texpr1BinNode.OP_DIV;
			case "%": return Texpr1BinNode.OP_MOD;
			case "^": return Texpr1BinNode.OP_POW;
			default: throw new UnsupportedOperationException("Operator "+operator+" not yet supported by Apron interface");
		}
	}

	@Override
	public Apron lub(Apron other) {
		try {
			return new Apron(state.joinCopy(manager, other.state));
		} catch (ApronException e) {
			throw new UnsupportedOperationException("Apron library crashed", e);
		}
	}

	@Override
	public boolean lessOrEqual(Apron other) {
		try {
			return state.isIncluded(manager, other.state);
		} catch (ApronException e) {
			throw new UnsupportedOperationException("Apron library crashed", e);
		}
	}

	@Override
	public Apron widening(Apron succ) {
		try {
			return new Apron(state.widening(manager, succ.state));
		} catch (ApronException e) {
			throw new UnsupportedOperationException("Apron library crashed", e);
		}
	}

	@Override
	public Apron bottom() {
		try {
			return new Apron(new Abstract1(manager, new apron.Environment(), true));
		} catch (ApronException e) {
			throw new UnsupportedOperationException("Apron library crashed", e);
		}
	}
	
	@Override
	public String toString() {
		return state.toString();
	}

}