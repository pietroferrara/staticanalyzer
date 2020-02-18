package it.unive.dais.staticanalyzer.api;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AnalysisOptions {
	private String input, output, cfg, domain, checker, xmlanalysisoptionsfile, xmlanalysisresultfile;
	
	@SuppressWarnings("unused")
	private AnalysisOptions() {
		
	}
	
	public AnalysisOptions(String input, String output, String cfg, String domain, String checker, String xmlanalysisoptionsfile, String xmlanalysisresultfile) {
		this.setInput(input);
		this.output = output;
		this.cfg = cfg;
		this.domain = domain;
		this.checker = checker;
		this.xmlanalysisoptionsfile = xmlanalysisoptionsfile;
		this.xmlanalysisresultfile = xmlanalysisresultfile;
	}

	@XmlAttribute
	public String getXmlanalysisoptionsfile() {
		return xmlanalysisoptionsfile;
	}

	@XmlAttribute
	public String getXmlanalysisresultfile() {
		return xmlanalysisresultfile;
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
	


	public void setOutput(String output) {
		this.output = output;
	}

	public void setCfg(String cfg) {
		this.cfg = cfg;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setChecker(String checker) {
		this.checker = checker;
	}

	public void setXmlanalysisoptionsfile(String xmlanalysisoptionsfile) {
		this.xmlanalysisoptionsfile = xmlanalysisoptionsfile;
	}

	public void setXmlanalysisresultfile(String xmlanalysisresultfile) {
		this.xmlanalysisresultfile = xmlanalysisresultfile;
	}


}
