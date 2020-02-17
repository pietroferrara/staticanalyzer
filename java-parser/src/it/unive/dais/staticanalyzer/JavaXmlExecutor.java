package it.unive.dais.staticanalyzer;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import it.unive.dais.staticanalyzer.api.XmlUtility;
import it.unive.dais.staticanalyzer.cfg.CFGAnalysisResults;


public class JavaXmlExecutor {
	final static Logger logger = Logger.getLogger(CFGAnalysisResults.class.getName());

	public static void main(String[] args) throws IOException, JAXBException {
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(getOptions(), args);
			if(cmd.hasOption('h'))
				printHelp();
			else
				JavaCLI.runAnalysis(XmlUtility.readAnalysisOptions(cmd.getOptionValue('s')));
		}
		catch(ParseException e) {
			System.err.println(e.getMessage());
			printHelp();
		}
	}
	
	private static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp("Analyzer", getOptions());
	}


	private static Options getOptions() {
		Option spec = Option.builder("s").argName("xml specification file").desc("XML file containing the specification of the analysis").longOpt("spec").hasArg(true).required(true).build();
		Option help = Option.builder("h").desc("Print this help").longOpt("help").hasArg(false).required(false).build();
		return new Options()
				.addOption(spec)
				.addOption(help);
	}
	
}
