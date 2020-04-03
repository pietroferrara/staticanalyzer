package it.unive.dais.staticanalyzer;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import it.unive.dais.staticanalyzer.abstractdomain.AbstractAnalysisState;
import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.SemanticDomain;
import it.unive.dais.staticanalyzer.abstractdomain.instances.Apron.NumericalDomain;
import it.unive.dais.staticanalyzer.abstractdomain.instances.CharIsIncludedDomain;
import it.unive.dais.staticanalyzer.abstractdomain.instances.Apron;
import it.unive.dais.staticanalyzer.abstractdomain.instances.Environment;
import it.unive.dais.staticanalyzer.abstractdomain.instances.IntegerNumericalConstantDomain;
import it.unive.dais.staticanalyzer.abstractdomain.instances.StringPrefixDomain;
import it.unive.dais.staticanalyzer.api.AnalysisOptions;
import it.unive.dais.staticanalyzer.api.AnalysisResult;
import it.unive.dais.staticanalyzer.api.Warning;
import it.unive.dais.staticanalyzer.api.XmlUtility;
import it.unive.dais.staticanalyzer.cfg.JavaBodyParser;
import it.unive.dais.staticanalyzer.cfg.CFG;
import it.unive.dais.staticanalyzer.cfg.CFGAnalysisResults;
import it.unive.dais.staticanalyzer.cfg.ParsingException;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;
import it.unive.dais.staticanalyzer.property.AssertChecker;
import it.unive.dais.staticanalyzer.property.Checker;
import it.unive.dais.staticanalyzer.property.GenericSingleStatementChecker;


public class JavaCLI {
	final static Logger logger = Logger.getLogger(CFGAnalysisResults.class.getName());

	public static void main(String[] args) throws IOException, JAXBException {
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(getOptions(), args);
			if(cmd.hasOption('h'))
				printHelp();
			else {
				AnalysisOptions analysisOptions = CLIoptionsToStructuredOptions(cmd);
				AnalysisResult analysisResults = runAnalysis(analysisOptions);
				if(cmd.hasOption('a'))
					XmlUtility.writeAnalysisOptions(analysisOptions.getXmlanalysisoptionsfile(), analysisOptions);
				if(cmd.hasOption('r'))
					XmlUtility.writeAnalysisResult(analysisOptions.getXmlanalysisresultfile(), analysisResults);
						
			}
		}
		catch(ParseException e) {
			System.err.println(e.getMessage());
			printHelp();
		}
	}
	
	public static AnalysisResult runAnalysis(it.unive.dais.staticanalyzer.api.AnalysisOptions analysisOptions) throws IOException, ParseException {
		CFG cfg = null;
		try(FileInputStream stream = new FileInputStream(analysisOptions.getInput())) {
			logger.info("Building up the CFG");
			cfg = new JavaBodyParser(stream).parse();
		}
		
		String cfgOutput = analysisOptions.getCfg();
		if(cfgOutput != null) {
			cfg.dumpToDotFile(cfgOutput);
			logger.info("CFG dumped to "+cfgOutput);
		}

		

		logger.info("Starting the analysis");
		CFGAnalysisResults<?> analysis =
				CFGAnalysisResults.computeFixpoint(cfg, getAbstractState(analysisOptions.getDomain()));
		logger.info("Analysis ended");		
		
		if(analysisOptions.getOutput()!=null && ! analysisOptions.getOutput().isEmpty())
			analysis.dumpToDotFile(analysisOptions.getOutput());
		logger.info("Abstract results dumped to "+analysisOptions.getOutput());
		
		Checker c = getChecker(analysisOptions.getChecker());
		logger.info("Applying checker "+analysisOptions.getChecker());
		Collection<Warning> warns = c.check(analysis);
		if(warns.size()==0) {
			logger.info("No warning produced by the analysis");
		}
		else {
			logger.info(warns.size()+" warnings produced by the analysis");
			for(Warning w : warns)
				logger.info(w.toString());
		}
		return new AnalysisResult(analysisOptions, warns);
	}

	public static Checker getChecker(String checker) throws ParseException {
		switch(checker) {
			case "AssertChecker": return new GenericSingleStatementChecker<>(new AssertChecker());
			default: throw new UnsupportedOperationException("Checker "+checker+" not supported");
		}
	}
	
	public static AbstractAnalysisState<?> getAbstractState(String domain) throws ParseException {
		String[] params = domain.split(":");
		switch(params[0]) {
			case "IntegerNumericalConstantDomain": return new AbstractAnalysisState<>(null, new Environment<IntegerNumericalConstantDomain>(new IntegerNumericalConstantDomain(1).bottom()));
			case "CharIsIncludedDomain": return new AbstractAnalysisState<>(null, new CharIsIncludedDomain());
			case "StringPrefixDomain": return new AbstractAnalysisState<>(null, new StringPrefixDomain());
			case "Apron": 
				if(params.length==1)
					throw new ParseException("Domain Apron needs the numerical domain, syntax -d Apron:<numerical_domain>");
				try {
					Apron.setManager(NumericalDomain.valueOf(params[1]));
					return new AbstractAnalysisState<Apron>(null, new Apron());
				}
				catch(IllegalArgumentException e) {
					throw new ParseException("Numerical domain "+params[1]+" not supported by Apron. Apron supports the following domains: "+NumericalDomain.values());
				}
			default: throw new UnsupportedOperationException("Domain "+domain+" not supported");
		}
	}

	private static it.unive.dais.staticanalyzer.api.AnalysisOptions CLIoptionsToStructuredOptions(CommandLine cmd) {
		return new it.unive.dais.staticanalyzer.api.AnalysisOptions(cmd.getOptionValue("i"), cmd.getOptionValue("o"), cmd.getOptionValue("cfg"), cmd.getOptionValue("d"), cmd.getOptionValue("c"), cmd.getOptionValue("a"), cmd.getOptionValue("r"));
	}

	private static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp("JavaCLI", getOptions());
	}


	private static Options getOptions() {
		Option input = Option.builder("i").argName("input file").desc("Input file").longOpt("input").hasArg(true).required(true).build();
		Option cfg = Option.builder("cfg").argName("cfg file").desc("Control flow graph output dot file").longOpt("controlflowgraph").hasArg(true).required(false).build();
		Option output = Option.builder("o").argName("output file").desc("Output dot file containing detailed abstract analysis results").longOpt("output").hasArg(true).required(false).build();
		Option domain = Option.builder("d").argName("abstract domain").desc("Abstract domain for the analysis").longOpt("domain").hasArg(true).required(true).build();
		Option checker = Option.builder("c").argName("checker").desc("Property checked after the analysis on the abstract results").longOpt("checker").hasArg(true).required(true).build();

		Option xmlAnalysisOptions = Option.builder("a").argName("analysis options").desc("Output xml file of the analysis options").longOpt("xmloptions").hasArg(true).required(false).build();
		Option xmlAnalysisResults = Option.builder("r").argName("analysis results").desc("Output xml file of the analysis results").longOpt("xmlresults").hasArg(true).required(false).build();
		Option help = Option.builder("h").desc("Print this help").longOpt("help").hasArg(false).required(false).build();
		return new Options()
				.addOption(xmlAnalysisOptions)
				.addOption(xmlAnalysisResults)
				.addOption(checker)
				.addOption(input)
				.addOption(cfg)
				.addOption(output)
				.addOption(domain)
				.addOption(help);
	}
	
}
