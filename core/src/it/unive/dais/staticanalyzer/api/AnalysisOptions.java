package it.unive.dais.staticanalyzer.api;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AnalysisOptions {
	private String input, output, cfg, domain, checker;
	
	private AnalysisOptions() {
		
	}

	public AnalysisOptions(String input, String output, String cfg, String domain, String checker) {
		this.setInput(input);
		this.output = output;
		this.cfg = cfg;
		this.domain = domain;
		this.checker = checker;
	}
	
	@XmlAttribute
	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	@XmlAttribute
	public String getCfg() {
		return cfg;
	}

	@XmlAttribute
	public String getDomain() {
		return domain;
	}

	@XmlAttribute
	public String getChecker() {
		return checker;
	}

	@XmlAttribute
	public String getOutput() {
		return output;
	}

}
