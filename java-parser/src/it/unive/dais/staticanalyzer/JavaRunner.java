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
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.IntegerComponentNameProvider;
import org.jgrapht.io.StringComponentNameProvider;

import it.unive.dais.staticanalyzer.abstractdomain.AbstractAnalysisState;
import it.unive.dais.staticanalyzer.abstractdomain.instances.Apron.NumericalDomain;
import it.unive.dais.staticanalyzer.abstractdomain.instances.Apron;
import it.unive.dais.staticanalyzer.abstractdomain.instances.Environment;
import it.unive.dais.staticanalyzer.abstractdomain.instances.IntegerNumericalConstantDomain;
import it.unive.dais.staticanalyzer.api.AnalysisOptions;
import it.unive.dais.staticanalyzer.api.AnalysisResult;
import it.unive.dais.staticanalyzer.api.Warning;
import it.unive.dais.staticanalyzer.api.XmlUtility;
import it.unive.dais.staticanalyzer.cfg.CFG;
import it.unive.dais.staticanalyzer.cfg.CFGAnalysisResults;
import it.unive.dais.staticanalyzer.cfg.ParsingException;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;
import it.unive.dais.staticanalyzer.parser.java.BodyParser;
import it.unive.dais.staticanalyzer.property.AssertChecker;
import it.unive.dais.staticanalyzer.property.Checker;
import it.unive.dais.staticanalyzer.property.GenericSingleStatementChecker;


public class JavaRunner {
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
					XmlUtility.writeAnalysisOptions(cmd.getOptionValue('a'), analysisOptions);
				if(cmd.hasOption('r'))
					XmlUtility.writeAnalysisResult(cmd.getOptionValue('r'), analysisResults);
						
			}
		}
		catch(ParseException e) {
			System.err.println(e.getMessage());
			printHelp();
		}
	}
	
	private static AnalysisResult runAnalysis(it.unive.dais.staticanalyzer.api.AnalysisOptions analysisOptions) throws IOException, ParseException {
		FileInputStream stream = new FileInputStream(analysisOptions.getInput());
		logger.info("Building up the CFG");
		CFG cfg = new BodyParser(stream).parse();
		String cfgOutput = analysisOptions.getCfg();
		if(cfgOutput != null) {
			dumpCFG(cfg, cfgOutput);
			logger.info("CFG dumped to "+cfgOutput);
		}

		

		logger.info("Starting the analysis");
		CFGAnalysisResults analysis =
				CFGAnalysisResults.computeFixpoint(cfg, getAbstractState(analysisOptions.getDomain()));
		logger.info("Analysis ended");		
		
		dumpOutput(analysis, analysisOptions.getOutput());
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

	private static void dumpOutput(
			CFGAnalysisResults analysis,
			String output) throws IOException {

		DOTExporter<Statement, DefaultWeightedEdge> exporter2 = new DOTExporter<Statement, DefaultWeightedEdge>(
				new IntegerComponentNameProvider<Statement>(),

				new ComponentNameProvider<Statement>() {

					@Override
					public String getName(Statement component) {
						return "Entry state:\n"+analysis.getEntryState(component)+"\n"
								+component+
								"\nExit state:\n"+(analysis.getEntryState(component)==null ? "_|_" : analysis.getEntryState(component).smallStepSemantics(component));
					}
					
				},
				new ComponentNameProvider<DefaultWeightedEdge>() {

					@Override
					public String getName(DefaultWeightedEdge component) {
						Boolean b;
						try {
							b = CFG.getBooleanFromWeight(analysis.getCfg().getGraph().getEdgeWeight(component));
						} catch (ParsingException e) {
							return "<error>";
						}
						if(b==null) return "";
						else return String.valueOf(b.booleanValue());
					}
					
				}
		);
		try(FileWriter writer = new FileWriter("analysisresult.dot")) {
			exporter2.exportGraph(analysis.getCfg().getGraph(), writer);
		}
		
	}

	private static Checker getChecker(String checker) throws ParseException {
		switch(checker) {
			case "AssertChecker": return new GenericSingleStatementChecker<>(new AssertChecker());
			default: throw new UnsupportedOperationException("Checker "+checker+" not supported");
		}
	}
	
	private static AbstractAnalysisState getAbstractState(String domain) throws ParseException {
		String[] params = domain.split(":");
		switch(params[0]) {
			case "IntegerNumericalConstantDomain": return new AbstractAnalysisState<>(null, new Environment<IntegerNumericalConstantDomain>(new IntegerNumericalConstantDomain(1).bottom()));
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

	private static void dumpCFG(CFG cfg, String cfgOutput) throws IOException {
		DOTExporter<Statement, DefaultWeightedEdge> exporter = new DOTExporter<Statement, DefaultWeightedEdge>(
				new IntegerComponentNameProvider<Statement>(),
				new StringComponentNameProvider<Statement>(),
				new ComponentNameProvider<DefaultWeightedEdge>() {

					@Override
					public String getName(DefaultWeightedEdge component) {
						Boolean b;
						try {
							b = CFG.getBooleanFromWeight(cfg.getGraph().getEdgeWeight(component));
						} catch (ParsingException e) {
							return "<error>";
						}
						if(b==null) return "";
						else return String.valueOf(b.booleanValue());
					}
					
				}
		);
		try(FileWriter writer = new FileWriter(cfgOutput)) {
			exporter.exportGraph(cfg.getGraph(), writer);
		}
	}

	private static it.unive.dais.staticanalyzer.api.AnalysisOptions CLIoptionsToStructuredOptions(CommandLine cmd) {
		return new it.unive.dais.staticanalyzer.api.AnalysisOptions(cmd.getOptionValue("i"), cmd.getOptionValue("o"), cmd.getOptionValue("cfg"), cmd.getOptionValue("d"), cmd.getOptionValue("c"));
	}

	private static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp("Analyzer", getOptions());
	}


	private static Options getOptions() {
		Option input = Option.builder("i").argName("input file").desc("Input file").longOpt("input").hasArg(true).required(true).build();
		Option cfg = Option.builder("cfg").argName("cfg file").desc("Control flow graph output dot file").longOpt("controlflowgraph").hasArg(true).required(false).build();
		Option output = Option.builder("o").argName("output file").desc("Output dot file containing detailed abstract analysis results").longOpt("output").hasArg(true).required(false).build();
		Option domain = Option.builder("d").argName("abstract domain").desc("Abstract domain for the analysis").longOpt("domain").hasArg(true).required(true).build();
		Option checker = Option.builder("c").argName("checker").desc("Property checked after the analysis on the abstract results").longOpt("checker").hasArg(true).required(true).build();

		Option xmlAnalysisOptions = Option.builder("a").argName("analysis options xml output file").desc("Output xml file of the analysis options").longOpt("xmlanalysisoptions").hasArg(true).required(false).build();
		Option xmlAnalysisResults = Option.builder("r").argName("analysis results xml output file").desc("Output xml file of the analysis results").longOpt("xmlanalysisresults").hasArg(true).required(false).build();
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
