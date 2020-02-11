package it.unive.dais.staticanalyzer;

public class Options {
	private String input, output, cfg, domain;

	public Options(String input, String output, String cfg, String domain) {
		this.setInput(input);
		this.output = output;
		this.cfg = cfg;
		this.domain = domain;
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

	public String getOutput() {
		return output;
	}

}
