package it.unive.dais.staticanalyzer.abstractdomain.instances;

import it.unive.dais.staticanalyzer.cfg.expression.BinaryArithmeticExpression;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.IntegerConstant;
import it.unive.dais.staticanalyzer.cfg.expression.NegatedBooleanExpression;
import it.unive.dais.staticanalyzer.cfg.expression.NumericalComparisonExpression;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;

public class IntervalDomain implements NonRelationalDomain<IntervalDomain> {

	private final long inf, sup;

	public IntervalDomain() {
		this.inf = 1;
		this.sup = 0;
	}

	public IntervalDomain(long k) {
		this.inf = k;
		this.sup = k;
	}

	public IntervalDomain(long a, long b) {
		this.inf = Math.min(a, b);
		this.sup = Math.max(a, b);
	}

	public long getInf() {
		return inf;
	}

	public long getSup() {
		return sup;
	}

	@Override
	public IntervalDomain lub(IntervalDomain other) {
		if (inf > sup) {
			return other;
		}
		if (other.inf > other.sup) {
			return this;
		}
		return new IntervalDomain(Math.min(inf, other.inf), Math.max(sup, other.sup));
	}

	@Override
	public boolean lessOrEqual(IntervalDomain other) {
		if (inf > sup) {
			return true;
		}
		if (other.inf > other.sup) {
			return false;
		}
		return (other.inf <= inf && sup <= other.sup);
	}

	@Override
	public IntervalDomain widening(IntervalDomain succ) {
		return this.lub(succ);
	}

	@Override
	public IntervalDomain bottom() {
		return new IntervalDomain();
	}

	@Override
	public IntervalDomain eval(Expression expr, Environment<IntervalDomain> env) {
		if (expr instanceof IntegerConstant) {
			IntegerConstant icExpr = (IntegerConstant) expr;
			return new IntervalDomain(icExpr.getValue());
		} else if (expr instanceof VariableIdentifier) {
			VariableIdentifier viExpr = (VariableIdentifier) expr;
			return env.getState(viExpr.getName());
		}
		if (expr instanceof BinaryArithmeticExpression) {
			BinaryArithmeticExpression baExpr = (BinaryArithmeticExpression) expr;
			String op = baExpr.getOperator();
			IntervalDomain lhs = eval(baExpr.getLeft(), env), rhs = eval(baExpr.getRight(), env);
			if (lhs.isBottom()) {
				return lhs;
			} else if (rhs.isBottom()) {
				return rhs;
			}
			if (op.equals("+")) {
				return new IntervalDomain(lhs.inf + rhs.inf, lhs.sup + rhs.sup);
			} else if (op.equals("-")) {
				return new IntervalDomain(lhs.inf - rhs.sup, lhs.sup - rhs.inf);
			} else if (op.equals("*")) {
				long inf = 0, sup = 0;
				for (int i = 0; i < 4; ++i) {
					long maybeBound = (i / 2 == 0 ? lhs.inf : lhs.sup) * (i % 2 == 0 ? rhs.inf : rhs.sup);
					if (i == 0) {
						inf = sup = maybeBound;
					} else {
						inf = Math.min(inf, maybeBound);
						sup = Math.max(sup, maybeBound);
					}
				}
				return new IntervalDomain(inf, sup);
			} else if (op.equals("/")) {
				if (rhs.inf == 0 && rhs.sup == 0) {
					return new IntervalDomain();
				}
				if (rhs.inf == 0) {
					rhs = new IntervalDomain(1, rhs.sup);
				} else if (rhs.sup == 0) {
					rhs = new IntervalDomain(rhs.inf, -1);
				}
				long inf = 0, sup = 0;
				for (int i = 0; i < 4; ++i) {
					long maybeBound = (i / 2 == 0 ? lhs.inf : lhs.sup) / (i % 2 == 0 ? rhs.inf : rhs.sup);
					if (i == 0) {
						inf = sup = maybeBound;
					} else {
						inf = Math.min(inf, maybeBound);
						sup = Math.max(sup, maybeBound);
					}
				}
				return new IntervalDomain(inf, sup);
			} else {
				throw new UnsupportedOperationException("Arithmetic operator '" + op + "' is not supported yet");
			}
		} else if (expr instanceof NumericalComparisonExpression) {
			NumericalComparisonExpression ncExpr = (NumericalComparisonExpression) expr;
			String op = ncExpr.getOperator();
			IntervalDomain lhs = eval(ncExpr.getLeft(), env), rhs = eval(ncExpr.getRight(), env);
			if (lhs.isBottom()) {
				return lhs;
			} else if (rhs.isBottom()) {
				return rhs;
			}
			if (op.equals("==")) {
				if (lhs.inf <= rhs.sup && rhs.inf <= lhs.sup) {
					return new IntervalDomain(Math.max(lhs.inf, rhs.inf), Math.min(lhs.sup, rhs.sup));
				} else {
					return new IntervalDomain();
				}
			} else if (op.equals("!=")) {
				return new IntervalDomain(Math.min(lhs.inf, rhs.inf), Math.max(lhs.sup, rhs.sup));
			} else if (op.equals("<=")) {
				if (lhs.inf <= rhs.sup) {
					return new IntervalDomain(lhs.inf, Math.min(lhs.sup, rhs.sup));
				} else {
					return new IntervalDomain();
				}
			} else if (op.equals("<")) {
				if (lhs.inf < rhs.inf) {
					return new IntervalDomain(lhs.inf, Math.min(lhs.sup, rhs.inf - 1));
				} else {
					return new IntervalDomain();
				}
			} else if (op.equals(">=")) {
				if (lhs.sup >= rhs.inf) {
					return new IntervalDomain(Math.max(lhs.inf, rhs.inf), lhs.sup);
				} else {
					return new IntervalDomain();
				}
			} else if (op.equals(">")) {
				if (lhs.sup > rhs.sup) {
					return new IntervalDomain(Math.max(lhs.inf, rhs.sup + 1), lhs.sup);
				} else {
					return new IntervalDomain();
				}
			} else {
				throw new UnsupportedOperationException("Comparison operator '" + op + "' is not supported yet");
			}
		} else if (expr instanceof NegatedBooleanExpression) {
			NegatedBooleanExpression nbExpr = (NegatedBooleanExpression) expr;
			if (nbExpr.getExpression() instanceof NumericalComparisonExpression) {
				return eval(((NumericalComparisonExpression) nbExpr.getExpression()).negate(), env);
			} else {
				throw new UnsupportedOperationException(
						"Negation of " + nbExpr.getExpression().getClass().getName() + " is not supported yet");
			}
		} else {
			throw new UnsupportedOperationException(expr.getClass().getName() + " is not supported yet");
		}
	}

	public boolean isBottom() {
		return inf > sup;
	}

	@Override
	public String toString() {
		if (inf > sup) {
			return "_|_";
		}
		return "[" + inf + ", " + sup + "]";
	}
}
