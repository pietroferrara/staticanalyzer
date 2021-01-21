package it.unive.dais.staticanalyzer.abstractdomain.instances;

import it.unive.dais.staticanalyzer.AnalysisConstants;
import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.cfg.Type;
import it.unive.dais.staticanalyzer.cfg.Type.*;
import it.unive.dais.staticanalyzer.cfg.expression.AssignableExpression;
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
import it.unive.dais.staticanalyzer.cfg.statement.ReturnStatement;
import it.unive.dais.staticanalyzer.cfg.statement.SkipStatement;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;
import it.unive.dais.staticanalyzer.cfg.statement.VariableDeclaration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import apron.*;
import gmp.Mpfr;

/**
 * Numerical abstract domain based on the Apron library.
 * Note: in order to run the analysis with Apron, it is required to set up the environment variable
 * LD_LIBRARY_PATH to point to the installation directory of Apron (thus, it is required to compile
 * and install Apron on the machine the analysis is ran).
 * @author Pietro Ferrara
 *
 */
public class Apron implements SemanticDomain<Apron>, Lattice<Apron> {
	final static Logger logger = Logger.getLogger(Apron.class.getName());

	private static Manager manager;
	
	final Abstract1 state;
	
	/**
	 * The Aprong numerical domain 
	 * @author Pietro Ferrara
	 *
	 */
	public enum NumericalDomain {
		/**
		 * Intervals
		 */
	    Box, 
	    
	    /**
	     * Octagons
	     */
	    Octagon, 
	    
	    /**
	     * Convex polyhedra
	     */
	    Polka, 
	    
	    /**
	     * Linear equalities
	     */
	    PolkaEq, 
	    
	    /**
	     * Reduced product of the Polka convex polyhedra and PplGrid the linear congruence equalities domains
	     * Compile Apron with the specific flag for PPL set to 1 in order to use such domain.
	     */
	    PolkaGrid, 
	    
	    /**
	     * Parma Polyhedra Library linear congruence equalities domain
	     * Compile Apron with the specific flag for PPL set to 1 in order to use such domain.
	     */
	    PplGrid, 
	    
	    /**
	     * The Parma Polyhedra libraryconvex polyhedra domain
	     * Compile Apron with the specific flag for PPL set to 1 in order to use such domain.
	     */
	    PplPoly;
	}
	
	public Lincons1[] getConstraints() throws ApronException {
		return state.toLincons(manager);
	}
	
	/**
	 * 
	 * @param numericalDomain The specific numerical domain used by the analysis.
	 */
	public static void setManager(NumericalDomain numericalDomain) {
		/*if(manager!=null)
			logger.warning("Re-setting the manager! Allowed only in tests");*/
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
	
	/**
	 * Instances the Apron abstract domain. It is required to call setManager before instantating the domain.
	 */
	public Apron() {
		try {
			String[] vars = {"<ret>"}; //Variable needed to represent the value returned
			state = new Abstract1(manager, new apron.Environment(new String[0], vars));
		}
		catch(ApronException e) {
			throw new UnsupportedOperationException("Apron library crashed", e);
		}
	}
	
	Apron(Abstract1 state) {
		this.state = state;
	}
	
	@Override
	public Apron smallStepSemantics(Statement st) {
		try {
			if(this.state.isBottom(manager))
				return this.bottom();
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
				AssignableExpression assigned = assignment.getAssignedVariable();
				if(! (assigned instanceof VariableIdentifier))
					throw new UnsupportedOperationException("Only assignment of variables is supported by Apron");
				String assignedVariable = ((VariableIdentifier) assigned).getName();
				Var variable = new StringVar(assignedVariable);
				if(AnalysisConstants.isForget(assignment.getExpression()))
					return new Apron(state.forgetCopy(manager, variable, false));
				else if(st instanceof ReturnStatement && assignment.getExpression()==null)
					return new Apron(state);
				else return new Apron(state.assignCopy(manager, variable, new Texpr1Intern(state.getEnvironment(), convertExpressionToApronFormat(assignment.getExpression())), null));
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
			if(this.state.isBottom(manager))
				return this.bottom();
			if(expr instanceof NumericalComparisonExpression)
				return new Apron(state.meetCopy(manager, convertNumericalComparisonToApronFormat((NumericalComparisonExpression) expr)));
			else if(expr instanceof NegatedBooleanExpression) {
				Expression inner = ((NegatedBooleanExpression) expr).getExpression();
				if(inner instanceof NegatedBooleanExpression)
					return assume(((NegatedBooleanExpression) inner).getExpression());
				if(inner instanceof NumericalComparisonExpression)
					return assume(((NumericalComparisonExpression) inner).negate());
				if(inner instanceof BooleanExpression)
					return assume(((BooleanExpression) inner).negate());
				if(inner instanceof VariableIdentifier)
					return new Apron(state);//TODO: We do not track information about Boolean variables
				else throw new UnsupportedOperationException("Assumption of the negation of expression "+inner.getClass().getTypeName()+" not yet supported");
			}
			else if(expr instanceof BooleanExpression) {
				BooleanExpression bexp = (BooleanExpression) expr;
				Apron left, right;
				switch(bexp.getOperator()) {
					case "&&":
						left = this.assume(bexp.getLeft()); 
						right = this.assume(bexp.getRight());
						return new Apron(left.state.meetCopy(manager, right.state));
					case "||":
						left = this.assume(bexp.getLeft()); 
						right = this.assume(bexp.getRight());
						return new Apron(left.state.joinCopy(manager, right.state));
					default: new UnsupportedOperationException("Boolean operator "+bexp.getOperator()+" not yet supported"); 
				}
			}
			else if(expr instanceof VariableIdentifier)//TODO: We do not track information about Boolean variables
				return new Apron(state);
		}
		catch(ApronException e) {
			throw new UnsupportedOperationException("Apron library crashed", e);
		}
		throw new UnsupportedOperationException("Assumption of expression "+expr.getClass().getTypeName()+" not yet supported");
	}

	
	@Override
	public boolean satisfy(Expression expr) {
		try {
			if(this.state.isBottom(manager))
				return true;
		}
		catch(ApronException e) {
			throw new UnsupportedOperationException("Apron library crashed", e);
		}
		if(expr instanceof NumericalComparisonExpression)
			try {
				return state.satisfy(manager, convertNumericalComparisonToApronFormat((NumericalComparisonExpression) expr));
			} catch (ApronException e) {
				throw new UnsupportedOperationException("Apron library crashed", e);
			}
		else if(expr instanceof NegatedBooleanExpression) {
			Expression inner = ((NegatedBooleanExpression) expr).getExpression();
			if(inner instanceof NegatedBooleanExpression)
				return satisfy(((NegatedBooleanExpression) inner).getExpression());
			if(inner instanceof NumericalComparisonExpression)
				return satisfy(((NumericalComparisonExpression) inner).negate());
			if(inner instanceof BooleanExpression)
				return satisfy(((BooleanExpression) inner).negate());
			if(inner instanceof VariableIdentifier)
				return false;//TODO: We do not track information about Boolean variables
			else throw new UnsupportedOperationException("Assumption of the negation of expression "+inner.getClass().getTypeName()+" not yet supported");
		}
		throw new UnsupportedOperationException("Checking if expression "+expr.getClass().getCanonicalName()+" holds is not yet supported by Apron");
	}
	
	private Tcons1 convertNumericalComparisonToApronFormat(NumericalComparisonExpression expr) {
		Expression combinedExpr = new BinaryArithmeticExpression(expr.getLeft(), expr.getRight(), "-", expr.getLine(), expr.getColumn()); //Apron supports only "expr <comparison> 0", so we need to move everything on the left node 
		switch(expr.getOperator()) {
			case "==":
			case "!=":
			case ">":
			case ">=": return new Tcons1(state.getEnvironment(), convertComparisonOperator(expr.getOperator()), convertExpressionToApronFormat(combinedExpr));
			//For the other cases Need to revert the operator since Apron has a limited support for comparison operators
			case "<": combinedExpr = new BinaryArithmeticExpression(new IntegerConstant(0, combinedExpr.getLine(), combinedExpr.getColumn()), combinedExpr, "-", combinedExpr.getLine(), combinedExpr.getColumn());
				return new Tcons1(state.getEnvironment(), convertComparisonOperator(">"), convertExpressionToApronFormat(combinedExpr));
			case "<=": combinedExpr = new BinaryArithmeticExpression(new IntegerConstant(0, combinedExpr.getLine(), combinedExpr.getColumn()), combinedExpr, "-", combinedExpr.getLine(), combinedExpr.getColumn());
				return new Tcons1(state.getEnvironment(), convertComparisonOperator(">="), convertExpressionToApronFormat(combinedExpr));
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
			if(other.state.isBottom(manager))
				return this;
			if(this.state.isBottom(manager))
				return other;
			return new Apron(state.joinCopy(manager, other.state));
		} catch (ApronException e) {
			throw new UnsupportedOperationException("Apron library crashed", e);
		}
	}

	@Override
	public boolean lessOrEqual(Apron other) {
		try {
			if(this.state.isBottom(manager))
				return true;
			if(other.state.isBottom(manager))
				return false;
			return state.isIncluded(manager, other.state);
		} catch (ApronException e) {
			throw new UnsupportedOperationException("Apron library crashed", e);
		}
	}

	@Override
	public Apron widening(Apron succ) {
		try {
			if(succ.state.isBottom(manager))
				return this;
			if(this.state.isBottom(manager))
				return succ;
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
	
	static public Apron getStateFromString(apron.Environment env, String state) {
		Lincons1[] translatedconstraints = getConstraintsFromString(env, state);
		try {
			return new Apron(new Abstract1(manager, translatedconstraints));
		} catch (ApronException e) {
			throw new UnsupportedOperationException("Apron library crashed", e);
		}
	}

	static public Lincons1[] getConstraintsFromString(apron.Environment env, String state) {
		String[] constraints = state.substring(1, state.length()-1).split(";");
		List<Lincons1> translatedconstraints = new ArrayList<>();
		for(String constr : constraints)
			translatedconstraints.add(Apron.translate(env, constr));
		return translatedconstraints.toArray(new Lincons1[translatedconstraints.size()]);
	}
	
	static private class SingleComponent {
		private String coefficient;
		private String variable;
		private int characters;

		private SingleComponent(String coefficient, String variable, int characters) {
			this.coefficient = coefficient;
			this.variable = variable;
			this.characters = characters;
		}
		
		static SingleComponent parseNextComponent(String s, int start) {
			int i = start;
			while(
				i < s.length() && (	
				s.charAt(i) == '+' ||
				s.charAt(i) == '-' ||
				s.charAt(i) == '.' ||
				Character.isDigit(s.charAt(i)))
			)
				i++;
			if(i==start) return null;
			String coefficient = s.substring(start, i);
			int current = i;
			while(i < s.length() && (
					Character.isAlphabetic(s.charAt(i)) || Character.isDigit(s.charAt(i)) || s.charAt(i)=='_'))
				i++;
			String variable = i==current ? null : s.substring(current, i);
			return new SingleComponent(coefficient, variable, i);
		}
		@Override
		public String toString() {
			return coefficient + (variable == null ? "" : variable);
		}
	}

	private static Lincons1 translate(apron.Environment env, String constr) {
		DoubleScalar[] scalars = new DoubleScalar[env.getSize()];
		DoubleScalar constantvalue = null;
		String constrCleaned = constr.trim().replace(" ", "").replace("\n", "");
		SingleComponent current = SingleComponent.parseNextComponent(constrCleaned, 0);
		List<SingleComponent> components = new ArrayList<>();
		int start = current.characters;
		while(current!=null) {
			components.add(current);
			if(current.variable != null) {
				int dim = env.dimOfVar(current.variable);
				if(scalars[dim]!=null)
					throw new UnsupportedOperationException("The same variables is contained more than once in the linear constraint");
				scalars[dim] = new DoubleScalar(Double.parseDouble(current.coefficient));
			}
			else {
				if(constantvalue!=null) throw new UnsupportedOperationException("The same variables is contained more than once in the linear constraint");
				constantvalue = new DoubleScalar(Double.parseDouble(current.coefficient));
			}
			current = SingleComponent.parseNextComponent(constrCleaned, start);
			if(current!=null)
				start = current.characters;
		}
		
		String operator = extractOperator(constrCleaned, start);
		start += operator.length();
		
		int k; 
		switch(operator) {
			case ">=" : k = Lincons1.SUPEQ; break;
			case ">" : k = Lincons1.SUP; break;
			case "=" : k = Lincons1.EQ; break;
			case "<>" : k = Lincons1.DISEQ; break;
			default : throw new UnsupportedOperationException("This operator is not supported: "+operator);
		}
		
		SingleComponent rightSide = SingleComponent.parseNextComponent(constrCleaned, start);
		if(rightSide.variable!=null)
			throw new UnsupportedOperationException("This is not suppoerted");
		Scalar scalar = new DoubleScalar(Double.parseDouble(rightSide.coefficient));
		
		if(! constrCleaned.substring(rightSide.characters).isEmpty())
			throw new UnsupportedOperationException("Cannot parse constraint"+constr);
		
		for(int i = 0; i < scalars.length; i++)
			if(scalars[i]==null)
				scalars[i] = new DoubleScalar(0);
		if(constantvalue == null)
			constantvalue = new DoubleScalar(0);
		return new Lincons1(k, new Linexpr1(env, scalars, constantvalue), scalar);
	}
	
	public apron.Environment getEnvironment() {
		return this.state.getEnvironment();
	}
	
	static String extractOperator(String s, int start) {
		int i = start;
		while(s.charAt(i) == '>' ||
			s.charAt(i) == '<' ||
			s.charAt(i) == '=' ||
			s.charAt(i) == '!'
		)
			i++;
		if(i==start) return null;
		else return s.substring(start, i);
	}

	public static Manager getManager() {
		return manager;
	}
}
