package it.unive.dais.staticanalyzer.api;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


 /**
  * The class representing the options of an analysis run. It can be marshelled to and unmarshalled
  * from an xml file.
  * @author Pietro Ferrara
  *
  */
@XmlRootElement
public class AnalysisOptions {
	private String input, output, cfg, domain, checker, xmlanalysisoptionsfile, xmlanalysisresultfile;
	
	@SuppressWarnings("unused")
	private AnalysisOptions() {
		
	}
	
	/**
	 * 
	 * @param input the path to file containing the code under analysis
	 * @param output the path where to dump an output dot file containing entry and exit state for each statement (optional) 
	 * @param cfg the path where to dump a dot file containing the control flow graph of the program under analysis (optional) 
	 * @param domain the abstract domain to apply during the analysis
	 * @param checker the property checker to apply on the analysis results
	 * @param xmlanalysisoptionsfile the path where to dump the xml file of the analysis options (optional)
	 * @param xmlanalysisresultfile the path where to dump the xml file of the analysis results (optional)
	 */
	public AnalysisOptions(String input, String output, String cfg, String domain, String checker, String xmlanalysisoptionsfile, String xmlanalysisresultfile) {
		this.setInput(input);
		this.output = output;
		this.cfg = cfg;
		this.domain = domain;
		this.checker = checker;
		this.xmlanalysisoptionsfile = xmlanalysisoptionsfile;
		this.xmlanalysisresultfile = xmlanalysisresultfile;
	}

	/**
	 * 
	 * @return the path where to dump the xml file of the analysis options (null if the output file is not produced)
	 */
	@XmlAttribute
	public String getXmlanalysisoptionsfile() {
		return xmlanalysisoptionsfile;
	}

	/**
	 * 
	 * @return the path where to dump the xml file of the analysis results (null if the output file is not produced)
	 */
	@XmlAttribute
	public String getXmlanalysisresultfile() {
		return xmlanalysisresultfile;
	}

	/**
	 * 
	 * @return the path to file containing the code under analysis
	 */
	@XmlAttribute
	public String getInput() {
		return input;
	}

	/**
	 * 
	 * @param input the path to file containing the code under analysis
	 */
	public void setInput(String input) {
		this.input = input;
	}

	/**
	 * 
	 * @return the path where to dump a dot file containing the control flow graph of the program under analysis (null if the output file is not produced)
	 */
	@XmlAttribute
	public String getCfg() {
		return cfg;
	}

	/**
	 * 
	 * @return the abstract domain to apply during the analysis
	 */
	@XmlAttribute
	public String getDomain() {
		return domain;
	}

	/**
	 * 
	 * @return the property checker to apply on the analysis results
	 */
	@XmlAttribute
	public String getChecker() {
		return checker;
	}

	/**
	 * 
	 * @return the path where to dump an output dot file containing entry and exit state for each statement (null if the output file is not produced)
	 */
	@XmlAttribute
	public String getOutput() {
		return output;
	}

	/**
	 * 
	 * @param output the path where to dump an output dot file containing entry and exit state for each statement (null if the output file is not produced)
	 */
	public void setOutput(String output) {
		this.output = output;
	}

	/**
	 * 
	 * @param cfg the path where to dump a dot file containing the control flow graph of the program under analysis (null if the output file is not produced)
	 */
	public void setCfg(String cfg) {
		this.cfg = cfg;
	}

	/**
	 * 
	 * @param domain the abstract domain to apply during the analysis
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * 
	 * @param checker the property checker to apply on the analysis results
	 */
	public void setChecker(String checker) {
		this.checker = checker;
	}

	/**
	 * 
	 * @param xmlanalysisoptionsfile the path where to dump the xml file of the analysis options (null if the output file is not produced)
	 */
	public void setXmlanalysisoptionsfile(String xmlanalysisoptionsfile) {
		this.xmlanalysisoptionsfile = xmlanalysisoptionsfile;
	}

	/**
	 * 
	 * @param xmlanalysisresultfile the path where to dump the xml file of the analysis results (null if the output file is not produced)
	 */
	public void setXmlanalysisresultfile(String xmlanalysisresultfile) {
		this.xmlanalysisresultfile = xmlanalysisresultfile;
	}


}
