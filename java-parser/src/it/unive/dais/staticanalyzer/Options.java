package it.unive.dais.staticanalyzer;

public class Options {
	private String input, output, cfg, domain, checker;

	public Options(String input, String output, String cfg, String domain, String checker) {
		this.setInput(input);
		this.output = output;
		this.cfg = cfg;
		this.domain = domain;
		this.checker = checker;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getCfg() {
		return cfg;
	}

	public String getDomain() {
		return domain;
	}

	public String getChecker() {
		return checker;
	}

	public String getOutput() {
		return output;
	}

}
