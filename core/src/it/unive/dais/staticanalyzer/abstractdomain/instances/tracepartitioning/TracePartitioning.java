package it.unive.dais.staticanalyzer.abstractdomain.instances.tracepartitioning;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.NegatedBooleanExpression;
import it.unive.dais.staticanalyzer.cfg.statement.SkipStatement;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

public class TracePartitioning<Domain extends SemanticDomain<Domain> & Lattice<Domain>>
		implements SemanticDomain<TracePartitioning<Domain>>, Lattice<TracePartitioning<Domain>> {

	private final Domain domainBottom;

	private final Map<Integer, Integer> iterationBounds;

	private final Map<Integer, Set<Integer>> linesToMerge;

	private final Token<Domain> tokens;

	private TracePartitioning(Domain domainBottom, Map<Integer, Integer> iterationBounds,
			Map<Integer, Set<Integer>> linesToMerge, Token<Domain> tokens) {
		this.domainBottom = domainBottom;
		this.iterationBounds = iterationBounds;
		this.linesToMerge = linesToMerge;
		this.tokens = tokens;
	}

	public TracePartitioning(Domain domainValue, Map<Integer, Integer> iterationBounds,
			Map<Integer, Set<Integer>> linesToMerge) {
		this.domainBottom = domainValue.bottom();
		this.iterationBounds = iterationBounds;
		this.linesToMerge = linesToMerge;
		this.tokens = new Token<Domain>(domainValue);
	}

	private TracePartitioning<Domain> withTokens(Token<Domain> tokens) {
		return new TracePartitioning<>(this.domainBottom, this.iterationBounds, this.linesToMerge, tokens);
	}

	@Override
	public TracePartitioning<Domain> lub(TracePartitioning<Domain> other) {
		return this.withTokens(doJoin(tokens, other.tokens, false));
	}

	public Token<Domain> doJoin(Token<Domain> x, Token<Domain> y, boolean widening) {
		if (x == null) {
			return y;
		}
		if (y == null) {
			return x;
		}
		Token<Domain> result = x.withDomainValue(joinDomainValues(x.getDomainValue(), y.getDomainValue(), widening));
		Set<Integer> ids = new HashSet<>(x.getSubTokensMap().keySet());
		ids.addAll(y.getSubTokensMap().keySet());
		for (int id : ids) {
			result.putSubToken(doJoin(x.getSubTokensMap().get(id), y.getSubTokensMap().get(id), widening));
		}
		return result;
	}

	private Domain joinDomainValues(Domain x, Domain y, boolean widening) {
		if (x == null && y == null) {
			return null;
		} else if (x == null) {
			return y;
		} else if (y == null) {
			return x;
		}
		if (widening) {
			return x.widening(y);
		}
		return x.lub(y);
	}

	private Domain joinDomainValues(Domain x, Domain y) {
		return joinDomainValues(x, y, false);
	}

	@Override
	public boolean lessOrEqual(TracePartitioning<Domain> other) {
		return doLessOrEqual(tokens, other.tokens);
	}

	private boolean doLessOrEqual(Token<Domain> lhs, Token<Domain> rhs) {
		if (lhs.isOngoingToken()) {
			if (!lhs.getDomainValue().lessOrEqual(joinTokenDomainValues(rhs))) {
				return false;
			}
		}
		for (Token<Domain> subLhs : lhs.getSubTokens()) {
			Token<Domain> subRhs = rhs.getSubTokensMap().get(subLhs.getDirectiveId());
			if (subRhs == null) {
				return false;
			}
			if (!doLessOrEqual(subLhs, subRhs)) {
				return false;
			}
		}
		return true;
	}

	private Domain joinTokenDomainValues(Token<Domain> token) {
		Domain result = token.getDomainValue();
		for (Token<Domain> subToken : token.getSubTokens()) {
			Domain subResult = joinTokenDomainValues(subToken);
			result = joinDomainValues(result, subResult);
		}
		return result;
	}

	@Override
	public TracePartitioning<Domain> widening(TracePartitioning<Domain> succ) {
		return this.withTokens(doJoin(tokens, succ.tokens, true));
	}

	@Override
	public TracePartitioning<Domain> bottom() {
		return this.withTokens(new Token<Domain>(domainBottom.bottom()));
	}

	@Override
	public TracePartitioning<Domain> smallStepSemantics(Statement statement) {
		Token<Domain> tokensAfterMerge = tokens;
		if (!(statement instanceof SkipStatement)) {
			tokensAfterMerge = mergePartition(tokens, linesToMerge.get(statement.getLine()));
		}
		TracePartitioning<Domain> result = this.withTokens(doSmallStepSemantics(tokensAfterMerge, statement));
		return result;
	}

	private Token<Domain> mergePartition(Token<Domain> token, Set<Integer> lines) {
		if (lines == null || lines.isEmpty()) {
			return token;
		}
		Domain resultDomainValue = token.getDomainValue();
		Set<Token<Domain>> resultSubTokens = new HashSet<>();
		for (Token<Domain> subToken : token.getSubTokens()) {
			if (lines.contains(subToken.getLine())) {
				resultDomainValue = joinDomainValues(resultDomainValue, joinTokenDomainValues(subToken));
			} else {
				resultSubTokens.add(mergePartition(subToken, lines));
			}
		}
		Token<Domain> result = token.withDomainValue(resultDomainValue);
		for (Token<Domain> subToken : resultSubTokens) {
			result.putSubToken(subToken);
		}
		return result;
	}

	private Token<Domain> doSmallStepSemantics(Token<Domain> token, Statement statement) {
		Token<Domain> result = token
				.withDomainValue(token.isOngoingToken() ? token.getDomainValue().smallStepSemantics(statement) : null);
		for (Token<Domain> subToken : token.getSubTokens()) {
			result.putSubToken(doSmallStepSemantics(subToken, statement));
		}
		return result;
	}

	@Override
	public TracePartitioning<Domain> assume(Expression expression) {
		Token<Domain> tokensAfterPartition = createPartition(tokens, expression.getLine(), guessBranch(expression));
		return this.withTokens(doAssume(tokensAfterPartition, expression));
	}

	private boolean guessBranch(Expression expression) {
		boolean result = true;
		while (expression instanceof NegatedBooleanExpression) {
			result = !result;
			expression = ((NegatedBooleanExpression) expression).getExpression();
		}
		return result;
	}

	private Token<Domain> createPartition(Token<Domain> token, int line, boolean branch) {
		Token<Domain> result = null;
		if (token.isOngoingToken()) {
			int iteration = (token.getLine() == line ? token.getIteration() + 1 : 0);
			if (iteration <= iterationBounds.getOrDefault(line, -1)) {
				result = token.withDomainValue(null);
				result.putSubToken(new Token<Domain>(line, iteration, branch, token.getDomainValue()));
			}
		}
		if (result == null) {
			result = token.withDomainValue(token.getDomainValue());
		}
		for (Token<Domain> subToken : token.getSubTokens()) {
			result.putSubToken(createPartition(subToken, line, branch));
		}
		return result;
	}

	private Token<Domain> doAssume(Token<Domain> token, Expression expression) {
		Token<Domain> result = null;
		if (token.isOngoingToken()) {
			Domain assumedValue = token.getDomainValue().assume(expression);
			result = token.withDomainValue(!assumedValue.lessOrEqual(domainBottom) ? assumedValue : null);
		} else {
			result = token.withDomainValue(null);
		}
		for (Token<Domain> subToken : token.getSubTokens()) {
			result.putSubToken(doAssume(subToken, expression));
		}
		return result;
	}

	@Override
	public boolean satisfy(Expression currentExpression) {
		return doSatisfy(tokens, currentExpression);
	}

	private boolean doSatisfy(Token<Domain> token, Expression expression) {
		if (token.isOngoingToken()) {
			if (!token.getDomainValue().satisfy(expression)) {
				return false;
			}
		}
		for (Token<Domain> subToken : token.getSubTokens()) {
			if (!doSatisfy(subToken, expression)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domainBottom == null) ? 0 : domainBottom.hashCode());
		result = prime * result + ((iterationBounds == null) ? 0 : iterationBounds.hashCode());
		result = prime * result + ((linesToMerge == null) ? 0 : linesToMerge.hashCode());
		result = prime * result + ((tokens == null) ? 0 : tokens.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TracePartitioning other = (TracePartitioning) obj;
		if (domainBottom == null) {
			if (other.domainBottom != null)
				return false;
		} else if (!domainBottom.equals(other.domainBottom))
			return false;
		if (iterationBounds == null) {
			if (other.iterationBounds != null)
				return false;
		} else if (!iterationBounds.equals(other.iterationBounds))
			return false;
		if (linesToMerge == null) {
			if (other.linesToMerge != null)
				return false;
		} else if (!linesToMerge.equals(other.linesToMerge))
			return false;
		if (tokens == null) {
			if (other.tokens != null)
				return false;
		} else if (!tokens.equals(other.tokens))
			return false;
		return true;
	}

	public Set<String> ongoingTokensToStringSet() {
		return doOngoingTokensToStringSet(tokens);
	}

	private Set<String> doOngoingTokensToStringSet(Token<Domain> token) {
		String tokenString = token.toString();
		Set<String> result = new HashSet<>();
		for (Token<Domain> subToken : token.getSubTokens()) {
			for (String subTokenResult : doOngoingTokensToStringSet(subToken)) {
				result.add(subTokenResult + " :: " + tokenString);
			}
		}
		if (token.isOngoingToken()) {
			result.add(tokenString);
		}
		return result;
	}

	@Override
	public String toString() {
		return String.join(" ; ", ongoingTokensToStringSet());
	}

}
