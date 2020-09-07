package it.unive.dais.staticanalyzer.abstractdomain.instances.tracepartitioning;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class Token<Domain> {

	private static int computeDirectiveId(int line, int iteration, boolean branch) {
		final int prime = 31;
		int result = 1;
		result = prime * result + (branch ? 1231 : 1237);
		result = prime * result + iteration;
		result = prime * result + line;
		return result;
	}

	private final int directiveId;

	private final int line;

	private final int iteration;

	private final boolean branch;

	private final Domain domainValue;

	private final Map<Integer, Token<Domain>> subTokens = new HashMap<>();

	private Token(int directiveId, int line, int iteration, boolean branch, Domain domainValue) {
		this.directiveId = directiveId;
		this.line = line;
		this.iteration = iteration;
		this.branch = branch;
		this.domainValue = domainValue;
	}

	public Token(int line, int iteration, boolean branch, Domain domainValue) {
		this.directiveId = computeDirectiveId(line, iteration, branch);
		this.line = line;
		this.iteration = iteration;
		this.branch = branch;
		this.domainValue = domainValue;
	}

	public Token(Domain domainValue) {
		this.directiveId = 0;
		this.line = 0;
		this.iteration = 0;
		this.branch = true;
		this.domainValue = domainValue;
	}

	public Token<Domain> withDomainValue(Domain domainValue) {
		return new Token<Domain>(directiveId, line, iteration, branch, domainValue);
	}

	public int getDirectiveId() {
		return directiveId;
	}

	public int getLine() {
		return line;
	}

	public int getIteration() {
		return iteration;
	}

	public boolean isTrueBranch() {
		return branch;
	}

	public Map<Integer, Token<Domain>> getSubTokensMap() {
		return subTokens;
	}

	public Collection<Token<Domain>> getSubTokens() {
		return subTokens.values();
	}

	public void putSubToken(Token<Domain> token) {
		subTokens.put(token.directiveId, token);
	}

	public Domain getDomainValue() {
		return domainValue;
	}

	public boolean isOngoingToken() {
		return (domainValue != null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + directiveId;
		result = prime * result + ((domainValue == null) ? 0 : domainValue.hashCode());
		result = prime * result + ((subTokens == null) ? 0 : subTokens.hashCode());
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
		Token other = (Token) obj;
		if (directiveId != other.directiveId)
			return false;
		if (domainValue == null) {
			if (other.domainValue != null)
				return false;
		} else if (!domainValue.equals(other.domainValue))
			return false;
		if (subTokens == null) {
			if (other.subTokens != null)
				return false;
		} else if (!subTokens.equals(other.subTokens))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return (directiveId != 0 ? "(L" + line + ", i" + iteration + ", " + (branch ? "T" : "F") + ")" : "()")
				+ (domainValue != null ? "[" + domainValue.toString() + "]" : "");
	}

}
