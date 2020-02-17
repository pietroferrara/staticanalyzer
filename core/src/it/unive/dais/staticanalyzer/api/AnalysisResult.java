package it.unive.dais.staticanalyzer.api;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AnalysisResult {
	private AnalysisOptions options;
	private Collection<Warning> warnings;
	
	private AnalysisResult() { }
	
	public AnalysisResult(AnalysisOptions options, Collection<Warning> warnings) {
		super();
		this.options = options;
		this.warnings = warnings;
	}

	@XmlElement
	public AnalysisOptions getOptions() {
		return options;
	}

	@XmlElementWrapper(name="warnings")
    @XmlElement(name="warning")
	public Collection<Warning> getWarnings() {
		return warnings;
	}
	


}
