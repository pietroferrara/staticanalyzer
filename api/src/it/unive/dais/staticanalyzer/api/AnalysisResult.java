package it.unive.dais.staticanalyzer.api;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The class representing an analysis run, that is the options of the analysis as well as the warnings
 * produced by the analysis. It can be marshaled to and unmarshaled from an xml file.
 * @author Pietro Ferrara
 *
 */
@XmlRootElement
public class AnalysisResult {
	/**
	 * 
	 * @param options the options of the analysis
	 */
	public void setOptions(AnalysisOptions options) {
		this.options = options;
	}

	/**
	 * 
	 * @param warnings the warnings produced by the analysis
	 */
	public void setWarnings(Collection<Warning> warnings) {
		this.warnings = warnings;
	}

	private AnalysisOptions options;
	private Collection<Warning> warnings;
	
	@SuppressWarnings("unused")
	private AnalysisResult() { }
	
	/**
	 * 
	 * @param options the options of the analysis
	 * @param warnings the warnings produced by the analysis
	 */
	public AnalysisResult(AnalysisOptions options, Collection<Warning> warnings) {
		super();
		this.options = options;
		this.warnings = warnings;
	}

	/**
	 * 
	 * @return the options of the analysis
	 */
	@XmlElement
	public AnalysisOptions getOptions() {
		return options;
	}

	/**
	 * 
	 * @return the warnings produced by the analysis
	 */
	@XmlElementWrapper(name="warnings")
    @XmlElement(name="warning")
	public Collection<Warning> getWarnings() {
		return warnings;
	}
	


}
