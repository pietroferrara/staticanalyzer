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

public class AttackerInferrer {

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
				String csv = cmd.getOptionValue('c');
				String attacker = cmd.getOptionValue('a');
				String dotresults = cmd.getOptionValue('r');
				String summaryresults = cmd.getOptionValue('s');
				String widening = cmd.getOptionValue('w');
				String javaAttacker = cmd.getOptionValue("ai");
				String jsonOutputAttacker = cmd.getOptionValue("j");
				int numberOfPartitions = cmd.hasOption('n') ? Integer.valueOf(cmd.getOptionValue('n')): Integer.MAX_VALUE;
				
				AnalysisConstants.WIDENING_LIMIT = Integer.parseInt(widening);
				
				CFG cfgAttacker = readCFG(javaAttacker);
				
				List<String> header = Utility.readCsvHeader(csv);
				Map<Integer, Attack> attackerModel = Attack.readJSONAttacker(attacker, header);
				long budget = Attack.readJSONAttackerBudget(attacker);
				String resultstring = runSingleAnalysis("Apron:Polka", numberOfPartitions, attackerModel, budget, cfgAttacker, dotresults== null ? null : dotresults+File.separator+"attacker.dot", jsonOutputAttacker);

				Path target = new File(summaryresults).toPath();
				Files.writeString(target, resultstring);
			}
		}
		catch(ParseException e) {
			System.err.println(e.getMessage());
			printHelp();
		}
	}

	private static String runSingleAnalysis(String domain, int numberOfPartitions, Map<Integer, Attack> attackerModel,
			long budget, CFG cfgAttacker, String dotresultsattacker, String jsonresultattacker) throws ParseException, IOException {

		
		//We first computer the parameters for the trace partitioning
		List<String> tracePartitioningParameters = new ArrayList<>();
		for(Integer i : attackerModel.keySet()) {
				String par = extractTracePartitioningParameter(attackerModel.get(i), budget, numberOfPartitions);
				if(par!=null)
					tracePartitioningParameters.add(par);
		}
		String elaboratedDomain = tracePartitioningParameters.size()==0 ? domain : "TracePartitioning:"+String.join(";", tracePartitioningParameters)+":"+domain;
		
		if(verbose) logger.info("Domain used for the analysis:"+elaboratedDomain);
		
		long starttime = System.currentTimeMillis();
		
		//We then initialize the entry state by initializing all the values of the features
		AbstractAnalysisState<?> entryState = JavaCLI.getAbstractState(elaboratedDomain);

		/*VariableIdentifier random = new VariableIdentifier("random", -1, -1);
		for(Integer i : attackerModel.keySet()) {
			VariableIdentifier varId = new VariableIdentifier("x"+i, -1, -1);
			VariableIdentifier initVarId = new VariableIdentifier("x"+i+"_init", -1, -1);
			entryState = entryState.smallStepSemantics(new VariableDeclaration(new Type.DoubleType(-1, -1), initVarId, -1, -1));
			entryState = entryState.smallStepSemantics(new Assignment(initVarId, random, -1, -1));
			entryState = entryState.smallStepSemantics(new VariableDeclaration(new Type.DoubleType(-1, -1), varId, -1, -1));
			entryState = entryState.smallStepSemantics(new Assignment(varId, initVarId, -1, -1));
		}*/
		
		//We analyze the attacker with the partitionend domain
		CFGAnalysisResults<?> analysis =
				CFGAnalysisResults.computeFixpoint(cfgAttacker, entryState);
		
		if(dotresultsattacker!=null)
			analysis.dumpToDotFile(dotresultsattacker);
		long endattackertime = System.currentTimeMillis();
		//We extract the exit state
		ReturnStatement lastReturn = null;
		for(Statement st: cfgAttacker.statements())
			if(st instanceof ReturnStatement && (lastReturn==null || lastReturn.getLine() < st.getLine()))
				lastReturn = (ReturnStatement) st;
		TracePartitioning attackerState = (TracePartitioning) analysis.getExitState(lastReturn).getSemanticDomainState();
		
		String[] vars = {"budget", "continua", "brandom"};
		attackerState.forgetVariables(vars);
		
		attackerState.dumpToJSON(jsonresultattacker);
		
		
		String stringresult ="Abstract domain:"+elaboratedDomain+"\n";
		stringresult+="Attacker analysis time:"+(endattackertime-starttime)+" msec\n";
		return stringresult;
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

	private static String extractTracePartitioningParameter(Attack attack, long budget, int maxPartitions) {
		//return attack.getLine()+","+(Math.min(maxPartitions, (long) (budget/attack.getCost())));
		return attack.getLine()+",1";//With the new model we need just to distinguish when we can enter the attack and when not
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
		Option cfgresults = Option.builder("r").argName("dot results").desc("Directory where to dump the dot results of all the analyses").longOpt("dotresults").hasArg(true).build();
		Option verbose = Option.builder("v").desc("Print verbose logging").longOpt("verbose").hasArg(false).build();
		Option summaryresult = Option.builder("s").argName("summary results").desc("File where to dump a text file with all the instances wrongly classified and the total analysis time").longOpt("summaryresults").hasArg(true).required(true).build();
		Option widening = Option.builder("w").argName("threshold").desc("Threshold before applying widening operators").longOpt("widening").hasArg(true).required(true).build();
		Option attackerModel = Option.builder("a").argName("json file").desc("JSON file with the specification of the attacker").longOpt("attacker").hasArg(true).required(true).build();
		Option attackerImplementation = Option.builder("ai").argName("java file").desc("Java file with the body of the attacker").longOpt("attackerimplementation").hasArg(true).required(true).build();
		Option numberOfPartitions = Option.builder("n").argName("state number").desc("Maximum number of states that a partitioning is allowed to keep").longOpt("numberofstates").hasArg(true).required(false).build();
		Option jsonattacker = Option.builder("j").argName("json output file").desc("JSON file with the dump of the attacker status after the attacks").longOpt("jsonoutput").hasArg(true).required(true).build();
		
		return new Options()
				.addOption(csv)
				.addOption(cfgresults)
				.addOption(verbose)
				.addOption(summaryresult)
				.addOption(widening)
				.addOption(attackerModel)
				.addOption(attackerImplementation)
				.addOption(numberOfPartitions)
				.addOption(jsonattacker);
	}
}
