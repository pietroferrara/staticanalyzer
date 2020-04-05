package it.unive.dais.staticanalyzer.decisiontrees;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.opencsv.exceptions.CsvValidationException;

import it.unive.dais.staticanalyzer.AnalysisConstants;
import it.unive.dais.staticanalyzer.JavaCLI;
import it.unive.dais.staticanalyzer.abstractdomain.AbstractAnalysisState;
import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.instances.TracePartitioning;
import it.unive.dais.staticanalyzer.api.Warning;
import it.unive.dais.staticanalyzer.cfg.CFG;
import it.unive.dais.staticanalyzer.cfg.CFGAnalysisResults;
import it.unive.dais.staticanalyzer.cfg.JavaBodyParser;
import it.unive.dais.staticanalyzer.cfg.Type;
import it.unive.dais.staticanalyzer.cfg.expression.FloatConstant;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;
import it.unive.dais.staticanalyzer.cfg.statement.Assignment;
import it.unive.dais.staticanalyzer.cfg.statement.ReturnStatement;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;
import it.unive.dais.staticanalyzer.cfg.statement.VariableDeclaration;

public class AnalysisRunner {

	final static Logger logger = Logger.getLogger(CFGAnalysisResults.class.getName());
	
	
	private static boolean verbose = false;

	public static void main(String[] args) throws IOException, CsvValidationException, org.json.simple.parser.ParseException {
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(getOptions(), args);
			if(cmd.hasOption('h'))
				printHelp();
			else {
				if(cmd.hasOption('v'))
					verbose = true;
				boolean joinPartitioning = cmd.hasOption('j');
				String csv = cmd.getOptionValue('c');
				String domain = cmd.getOptionValue('d');
				String attacker = cmd.getOptionValue('a');
				String dotresults = cmd.getOptionValue('r');
				String summaryresults = cmd.getOptionValue('s');
				String output = cmd.getOptionValue('o');
				String widening = cmd.getOptionValue('w');
				String javaAttacker = cmd.getOptionValue("ai");
				String javaTree = cmd.getOptionValue("ti");
				int numberOfPartitions = cmd.hasOption('n') ? Integer.valueOf(cmd.getOptionValue('n')): Integer.MAX_VALUE;
				
				AnalysisConstants.WIDENING_LIMIT = Integer.parseInt(widening);
				
				CFG cfgAttacker = readCFG(javaAttacker);
				CFG cfgTree = readCFG(javaTree);
				
				List<List<Double>> values = Utility.readCsv(csv, true);
				List<String> header = Utility.readCsvHeader(csv);
				Map<Integer, Attack> attackerModel = Attack.readJSONAttacker(attacker, header);
				long budget = Attack.readJSONAttackerBudget(attacker);
				Set<Integer> successfull = new TreeSet<>(), failed = new TreeSet<>();
				File directory = new File(output);
				long totaltime = 0;
				if(! directory.isDirectory()) 
					throw new ParseException("The output directory is not a directory!");
				for(int i = 0; i < values.size(); i++) {
					File check = new File(directory, i+".txt");
					if(check.exists()) {
						if(verbose) logger.info("Case "+i+" already processed");
						List<String> allLines = Files.readAllLines(check.toPath());
						Boolean result = Boolean.valueOf(allLines.get(0));
						totaltime+=Long.valueOf(allLines.get(1));
						if(result==null)
							throw new ParseException("Previous result stored in file "+check.getAbsolutePath()+" is invalid, it should start with a line containing true or false");
						if(result.booleanValue())
							successfull.add(i);
						else failed.add(i);
					}
					else {
						List<Double> vals = values.get(i);
						if(verbose) logger.info("Beginning the analysis of case "+i);
						long starttime = System.currentTimeMillis();
						boolean result = runSingleAnalysis(vals, domain, numberOfPartitions, attackerModel, budget, joinPartitioning, cfgAttacker, cfgTree, dotresults== null ? null : dotresults+File.separator+i+"_attacker.dot", dotresults== null ? null : dotresults+File.separator+i+"_tree.dot");
						long totallocaltime = System.currentTimeMillis() - starttime;
						String toDump = result+System.lineSeparator()+totallocaltime;
						totaltime += totallocaltime;
						Files.writeString(check.toPath(), toDump);
						if(result) {
							successfull.add(i);
							if(verbose) logger.info("Row "+i+" correctly classified");
						}
						else {
							failed.add(i);
							if(verbose) logger.warning("Row "+i+" wrongly classified");
						}
					}
				}
				logger.info(successfull.size()+" instances correctly classified\n"+failed.size()+" wrongly classified\n");

				String result = Arrays.toString(failed.toArray());
				if(failed.size()>0)
					logger.warning("Instances wrongly classified:" + result);
				result = result.replace(',', '\n');
				result = result.replace(" ", "");
				result = result.substring(1, result.length()-1);
				result += "\nTotal time (msec):"+ totaltime;
				Path target = new File(summaryresults).toPath();
				Files.writeString(target, result);
				logger.info("Textual summary of the results dumped to "+target);
			}
		}
		catch(ParseException e) {
			System.err.println(e.getMessage());
			printHelp();
		}
	}

	private static boolean runSingleAnalysis(List<Double> vals, String domain, int numberOfPartitions, Map<Integer, Attack> attackerModel,
			long budget, boolean joinPartitioning, CFG cfgAttacker, CFG cfgTree, String dotresultsattacker, String dotresultstree) throws ParseException, IOException {

		
		//We first computer the parameters for the trace partitioning
		List<String> tracePartitioningParameters = new ArrayList<>();
		for(int i = 1; i <= vals.size(); i++) {
			if(attackerModel.containsKey(i)) {
				String par = extractTracePartitioningParameter(vals.get(i-1).doubleValue(), attackerModel.get(i), budget, numberOfPartitions);
				if(par!=null)
					tracePartitioningParameters.add(par);
			}
		}
		String elaboratedDomain = tracePartitioningParameters.size()==0 ? domain : "TracePartitioning:"+String.join(";", tracePartitioningParameters)+":"+domain;
		
		logger.info("Domain used for the analysis:"+elaboratedDomain);
		
		//We then initialize the entry state by initializing all the values of the features
		AbstractAnalysisState<?> entryState = JavaCLI.getAbstractState(elaboratedDomain);
		
		for(int i = 1; i <= vals.size(); i++) {
			VariableIdentifier varId;
			if(i!=vals.size()) //declare and assign x<i>
				varId = new VariableIdentifier("x"+i, -1, -1);
			else //declare and assign y
				varId = new VariableIdentifier("y", -1, -1);
			
			entryState = entryState.smallStepSemantics(new VariableDeclaration(new Type.DoubleType(-1, -1), varId, -1, -1));
			FloatConstant constant = new FloatConstant(vals.get(i-1).doubleValue(), -1, -1);
			entryState = entryState.smallStepSemantics(new Assignment(varId, constant, -1, -1));
		}
		
		//We analyze the attacker with the partitionend domain
		CFGAnalysisResults<?> analysis =
				CFGAnalysisResults.computeFixpoint(cfgAttacker, entryState);
		
		if(dotresultsattacker!=null)
			analysis.dumpToDotFile(dotresultsattacker);
		
		//We extract the exit state
		ReturnStatement lastReturn = null;
		for(Statement st: cfgAttacker.statements())
			if(st instanceof ReturnStatement && (lastReturn==null || lastReturn.getLine() < st.getLine()))
				lastReturn = (ReturnStatement) st;
		AbstractAnalysisState<?> attackerState = analysis.getExitState(lastReturn);
		
		//We remove the partitioning collapsing all states into one
		Lattice<?> initialStateForTree = 
				joinPartitioning ? projectTracePartitioningOut(attackerState.getSemanticDomainState()) : attackerState.getSemanticDomainState();
		
		//We analyze the tree with this state
		analysis = CFGAnalysisResults.computeFixpoint(cfgTree, 
						new AbstractAnalysisState(null, initialStateForTree));
		if(dotresultstree!=null)
			analysis.dumpToDotFile(dotresultstree);
		Collection<Warning> result = JavaCLI.getChecker("AssertChecker").check(analysis);
		if(result.size()==0)
			return true;
		else if(result.size()==1)
			return false;
		else throw new UnsupportedOperationException("Impossible case");
	}

	private static <T extends Lattice<T>> Lattice<?> projectTracePartitioningOut(Lattice<?> semanticDomainState) {
		if(semanticDomainState instanceof TracePartitioning) {
			TracePartitioning tpstate = (TracePartitioning) semanticDomainState;
			T result = null;
			for(Object l : tpstate.getKeys())
				if(result == null)
					result = (T) tpstate.getState(l);
				else result = result.lub((T) tpstate.getState(l));
			return result;
		}
		else return semanticDomainState;
	}

	private static String extractTracePartitioningParameter(double featureConcreteValue, Attack attack, long budget, int maxPartitions) {
		if(featureConcreteValue >= attack.getLowerBound() && featureConcreteValue<= attack.getUpperBound())
			return attack.getLine()+","+(Math.min(maxPartitions, (long) (budget/attack.getCost())));
		else return null;
	}

	private static CFG readCFG(String java) throws FileNotFoundException, IOException {
		try(FileInputStream stream = new FileInputStream(java)) {
			logger.info("Building up the CFG");
			CFG cfg = new JavaBodyParser(stream).parse();
			return cfg;
		}
	}

	private static void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp("Decision tree analysis", getOptions());
	}


	private static Options getOptions() {
		Option csv = Option.builder("c").argName("csv file").desc("CSV file with data").longOpt("csv").hasArg(true).required(true).build();
		Option domain = Option.builder("d").argName("abstract domain").desc("Abstract domain for the analysis").longOpt("domain").hasArg(true).required(true).build();
		Option output = Option.builder("o").argName("output directory").desc("Directory where to dump the result of each case").longOpt("output").hasArg(true).required(true).build();
		Option cfgresults = Option.builder("r").argName("dot results").desc("Directory where to dump the dot results of all the analyses").longOpt("dotresults").hasArg(true).build();
		Option verbose = Option.builder("v").desc("Print verbose logging").longOpt("verbose").hasArg(false).build();
		Option summaryresult = Option.builder("s").argName("summary results").desc("File where to dump a text file with all the instances wrongly classified and the total analysis time").longOpt("summaryresults").hasArg(true).required(true).build();
		Option widening = Option.builder("w").argName("threshold").desc("Threshold before applying widening operators").longOpt("widening").hasArg(true).required(true).build();
		Option attackerModel = Option.builder("a").argName("json file").desc("JSON file with the specification of the attacker").longOpt("attacker").hasArg(true).required(true).build();
		Option attackerImplementation = Option.builder("ai").argName("java file").desc("Java file with the body of the attacker").longOpt("attackerimplementation").hasArg(true).required(true).build();
		Option treeImplementation = Option.builder("ti").argName("java file").desc("Java file with the body of the decision tree").longOpt("treeimplementation").hasArg(true).required(true).build();
		Option joinPartitioning = Option.builder("j").desc("Join trace partitioning").longOpt("join").hasArg(false).build();
		Option numberOfPartitions = Option.builder("n").argName("state number").desc("Maximum number of states that a partitioning is allowed to keep").longOpt("numberofstates").hasArg(true).required(false).build();
		
		return new Options()
				.addOption(csv)
				.addOption(domain)
				.addOption(cfgresults)
				.addOption(output)
				.addOption(verbose)
				.addOption(summaryresult)
				.addOption(widening)
				.addOption(attackerModel)
				.addOption(attackerImplementation)
				.addOption(treeImplementation)
				.addOption(joinPartitioning)
				.addOption(numberOfPartitions);
	}
}
