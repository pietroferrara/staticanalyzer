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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.opencsv.exceptions.CsvValidationException;

import apron.Coeff;
import apron.DoubleScalar;
import apron.Environment;
import apron.Lincons1;
import apron.Linexpr1;
import apron.Linterm1;
import apron.MpqScalar;
import apron.Var;
import it.unive.dais.staticanalyzer.AnalysisConstants;
import it.unive.dais.staticanalyzer.JavaCLI;
import it.unive.dais.staticanalyzer.abstractdomain.AbstractAnalysisState;
import it.unive.dais.staticanalyzer.abstractdomain.Lattice;
import it.unive.dais.staticanalyzer.abstractdomain.instances.Apron;
import it.unive.dais.staticanalyzer.abstractdomain.instances.Apron.NumericalDomain;
import it.unive.dais.staticanalyzer.abstractdomain.instances.TracePartitioning;
import it.unive.dais.staticanalyzer.api.Warning;
import it.unive.dais.staticanalyzer.cfg.CFG;
import it.unive.dais.staticanalyzer.cfg.CFGAnalysisResults;
import it.unive.dais.staticanalyzer.cfg.JavaBodyParser;
import it.unive.dais.staticanalyzer.cfg.Type;
import it.unive.dais.staticanalyzer.cfg.expression.BinaryArithmeticExpression;
import it.unive.dais.staticanalyzer.cfg.expression.Constant;
import it.unive.dais.staticanalyzer.cfg.expression.Expression;
import it.unive.dais.staticanalyzer.cfg.expression.FloatConstant;
import it.unive.dais.staticanalyzer.cfg.expression.IntegerConstant;
import it.unive.dais.staticanalyzer.cfg.expression.VariableIdentifier;
import it.unive.dais.staticanalyzer.cfg.statement.Assignment;
import it.unive.dais.staticanalyzer.cfg.statement.ReturnStatement;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;
import it.unive.dais.staticanalyzer.cfg.statement.VariableDeclaration;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class TestCasesAnalysis {

	final static Logger logger = Logger.getLogger(CFGAnalysisResults.class.getName());
	
	
	private static boolean verbose = false;

	public static void main(String[] args) throws IOException, CsvValidationException, org.json.simple.parser.ParseException, InterruptedException, ExecutionException, TimeoutException {
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(getOptions(), args);
			if(cmd.hasOption('h'))
				printHelp();
			else {
				if(cmd.hasOption('v'))
					verbose = true;
				boolean joinPartitioning = cmd.hasOption('j');
				boolean inferBudget = cmd.hasOption('b');
				String csv = cmd.getOptionValue('c');
				String attacker = cmd.getOptionValue('a');
				String dotresults = cmd.getOptionValue('r');
				String summaryresults = cmd.getOptionValue('s');
				String output = cmd.getOptionValue('o');
				String widening = cmd.getOptionValue('w');
				String cfg_splitting= cmd.getOptionValue("cs");
				String javaTree = cmd.getOptionValue("ti");
				String jsonAttackerState = cmd.getOptionValue("as");
				String domain = cmd.getOptionValue("d");
				int numberOfPartitions = cmd.hasOption('n') ? Integer.valueOf(cmd.getOptionValue('n')): Integer.MAX_VALUE;
				int timeout = cmd.hasOption('t') ? Integer.valueOf(cmd.getOptionValue('t')): Integer.MAX_VALUE;
				if(domain!=null) {
					switch(domain) {
					case "Polka": Apron.setManager(NumericalDomain.Polka);break;
					case "Octagon": Apron.setManager(NumericalDomain.Octagon);break;
					case "Box": Apron.setManager(NumericalDomain.Box);break;
					case "PolkaEq": Apron.setManager(NumericalDomain.PolkaEq);break;
					default: throw new UnsupportedOperationException("Domain "+domain+" not known");
					}
					
				}
				else Apron.setManager(NumericalDomain.Polka);
				
				AnalysisConstants.WIDENING_LIMIT = Integer.parseInt(widening);
				
				if(cfg_splitting!=null)
					AnalysisConstants.CFG_LIMIT = Integer.parseInt(cfg_splitting);
				
				CFG cfgTree = readCFG(javaTree);

				List<List<Double>> values = Utility.readCsv(csv, true);
				List<String> header = Utility.readCsvHeader(csv);
				Map<Integer, Attack> attackerModel = Attack.readJSONAttacker(attacker, header);
				apron.Environment env = createInitialEnvironment(attackerModel, header);

				Map<Map<Integer, Integer>, Lincons1[]> attackerState = TracePartitioning.loadConstraintsFromJSON(env, jsonAttackerState);		
				
				Set<Integer> successfull = new TreeSet<>(), failed = new TreeSet<>();
				File directory = new File(output);
				long totaltime = 0;
				if(! directory.isDirectory()) 
					throw new ParseException("The output directory is not a directory!");
				for(int i = 0; i < values.size(); i++) {
					File check = new File(directory, i+".txt");
					if(check.exists()) {
						if(verbose) logger.info("Case "+i+" already processed");
						if(! inferBudget) {
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
							List<String> allLines = Files.readAllLines(check.toPath());
							String budget = allLines.get(0);
							totaltime+=Long.valueOf(allLines.get(1));
							if(budget==null || "null".equals(budget))
								failed.add(i);
							else
								successfull.add(i);
						}
					}
					else {
						List<Double> vals = values.get(i);
						if(verbose) logger.info("Beginning the analysis of case "+i);
						long starttime = System.currentTimeMillis();
						if(! inferBudget) {
							Future<String> resultstringfuture = runSingleAnalysisTestCase(vals, numberOfPartitions, attackerModel, cfgTree, dotresults== null ? null : dotresults+File.separator+i+"_tree.dot", attackerState, env, joinPartitioning);
							try {
								String resultstring = resultstringfuture.get(timeout, TimeUnit.SECONDS);
								boolean result = Boolean.parseBoolean(resultstring.substring(0, resultstring.indexOf('\n')));
								long totallocaltime = System.currentTimeMillis() - starttime;
								String toDump = result+System.lineSeparator()+totallocaltime+"\n"+resultstring.substring(resultstring.indexOf('\n'));
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
							catch(TimeoutException e) {
								Files.writeString(check.toPath(), "false\n"+(timeout*1000));
								failed.add(i);
								totaltime += timeout*1000;
								if(verbose) logger.warning("Row "+i+" timeouted, and therefore conservatively considered wrongly classified");
								System.exit(1);
							}
						}
						else {
							Future<String> resultstringfuture = runSingleAnalysisBudget(vals, numberOfPartitions, attackerModel, cfgTree, dotresults== null ? null : dotresults+File.separator+i+"_tree.dot", attackerState, env, joinPartitioning);
							try {
								String resultstring = resultstringfuture.get(timeout, TimeUnit.SECONDS);
								Files.writeString(check.toPath(), resultstring);
								successfull.add(i);
								if(verbose) logger.info("Minimal budget of row "+i+":"+resultstring.substring(0, resultstring.indexOf('\n')));
							}
							catch(TimeoutException e) {
								Files.writeString(check.toPath(), "null\n"+(timeout*1000));
								failed.add(i);
								totaltime += timeout*1000;
								if(verbose) logger.warning("Row "+i+" timeouted");
								System.exit(1);
							}
							
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
				System.exit(0);
			}
		}
		catch(ParseException e) {
			System.err.println(e.getMessage());
			printHelp();
		}
	}

	private static Environment createInitialEnvironment(Map<Integer, Attack> attackerModel, List<String> header) {
		List<String> realVars = new ArrayList<>();
		for(Integer key : attackerModel.keySet()) {
			realVars.add("x"+key);
			realVars.add("x"+key+"_init");
			realVars.add("x"+key+"_counter");
			realVars.add("x"+key+"_min");
			realVars.add("x"+key+"_max");
		}
		for(int i = 0; i < header.size(); i++) {
			int index = i+1;
			if(! attackerModel.containsKey(Integer.valueOf(index))) {
				if(index==header.size())
					realVars.add("y");
				else realVars.add("x"+index);
			}
		}
		realVars.add("budget_init");
		String[] par = realVars.toArray(new String[realVars.size()]);
		return new apron.Environment(new String[0], par);
		//FIXME add other variables, or forget them
	}
	
	private static ExecutorService executor = Executors.newSingleThreadExecutor();

	private static Future<String> runSingleAnalysisTestCase(List<Double> vals, int numberOfPartitions, Map<Integer, Attack> attackerModel,
			CFG cfgTree, String dotresultstree, Map<Map<Integer, Integer>, Lincons1[]>  attackerConstraint, apron.Environment env, boolean join) throws ParseException, IOException {

		return executor.submit(() -> {
			long starttime = System.currentTimeMillis();
			
			CFGAnalysisResults<?> analysis = computeCFGAnalysisResults(vals, attackerModel, cfgTree, attackerConstraint,
					env, join);
			if(dotresultstree!=null)
				analysis.dumpToDotFile(dotresultstree);
			Collection<Warning> result = JavaCLI.getChecker("AssertChecker").check(analysis);
			long endtime = System.currentTimeMillis();
			String stringresult;
			if(result.size()==0)
				stringresult="true\n";
			else if(result.size()==1)
				stringresult="false\n";
			else throw new UnsupportedOperationException("Impossible case");
			stringresult+="Tree analysis time:"+(endtime-starttime)+" msec\n";
			return stringresult;
		});
	}


	private static Future<String> runSingleAnalysisBudget(List<Double> vals, int numberOfPartitions, Map<Integer, Attack> attackerModel,
			CFG cfgTree, String dotresultstree, Map<Map<Integer, Integer>, Lincons1[]>  attackerConstraint, apron.Environment env, boolean join) throws ParseException, IOException {

		return executor.submit(() -> {
			Apron.setManager(NumericalDomain.Octagon);
			long starttime = System.currentTimeMillis();
			int expectedClass = vals.get(vals.size()-1).intValue();
			
			CFGAnalysisResults<?> analysis = computeCFGAnalysisResults(vals, attackerModel, cfgTree, attackerConstraint,
					env, join);
			if(dotresultstree!=null)
				analysis.dumpToDotFile(dotresultstree);
			long endtime = System.currentTimeMillis();
			
			Collection<AbstractAnalysisState<?>> wrongClassificationStates = extractWrongClassificationStates(analysis, expectedClass);
			Integer minBudget = null;
			for(AbstractAnalysisState<?> a : wrongClassificationStates)
				if(! a.bottom().equals(a)) {
					Apron state = (Apron) a.getSemanticDomainState();
					if(! state.bottom().equals(state)) {
						Lincons1[] constraints = state.getConstraints();
						Integer budget = computeMinimalBudget(constraints);
						if(budget!=null) {
							if(minBudget==null)
								minBudget = budget;
							else minBudget = Integer.min(minBudget, budget);
						}
					}
				}

			String stringresult = minBudget+"\n";
			stringresult+="Tree analysis time:"+(endtime-starttime)+" msec\n";
			return stringresult;
		});
	}
	
	private static Integer computeMinimalBudget(Lincons1[] constraints) throws LpSolveException {
		ArrayList<String> vars = new ArrayList<>();
		vars.addAll(extractVariables(constraints));
		LpSolve solver = LpSolve.makeLp(0, vars.size());
		
		for(Lincons1 cons : constraints)
			addConstraints(cons, solver, vars);
		
		double[] coefficients = new double[vars.size()];
		coefficients[vars.indexOf("budget_init")] = 1;
		solver.setObjFn(coefficients);
		int result = solver.solve();
		solver.printLp();
		int min_budget = (int) solver.getPtrVariables()[vars.indexOf("budget_init")];
		switch(result) {
			case LpSolve.INFEASIBLE : return Integer.MAX_VALUE;
			case LpSolve.OPTIMAL: return Math.max(0, min_budget);
			default : throw new UnsupportedOperationException("unknown result from the solver");
		}
		
	}

	private static void addConstraints(Lincons1 cons, LpSolve solver, ArrayList<String> vars) throws LpSolveException {
		double[] coefficients = new double[vars.size()];
		
		for(Linterm1 term : cons.getLinterms()) {
			String variable = term.var.toString();
			int index = vars.indexOf(variable);
			if(index < 0)
				throw new UnsupportedOperationException("Variable in the constraint that is not part of the variables of the state");
			if(coefficients[index]!=0)
				throw new UnsupportedOperationException("The same variable is present many times in the same constraint");
			MpqScalar coefficient = (MpqScalar) term.getCoefficient();
			coefficients[index] = coefficient.val.doubleValue();
		}
		
		MpqScalar leftPart = (MpqScalar) cons.getCst();
		double constant = - leftPart.val.doubleValue();
		int operator = -1;
		switch(cons.getKind()) {
			case Lincons1.EQ : operator = LpSolve.EQ; break;
			case Lincons1.DISEQ : throw new UnsupportedOperationException("Operator not supported");
			case Lincons1.SUP : operator = LpSolve.GE; break;
			case Lincons1.SUPEQ : operator = LpSolve.GE; break;
			case Lincons1.EQMOD : throw new UnsupportedOperationException("Operator not supported");
			default : throw new UnsupportedOperationException("Operator not supported");
		}
		
		solver.addConstraint(coefficients, operator, constant);
			
	}

	private static Collection<String> extractVariables(Lincons1[] constraints) {
		Set<String> result = new HashSet<>();
		for(Lincons1 cons : constraints) 
			for(Linterm1 term : cons.getLinterms()) {
				Var variable = term.var;
				if(variable!=null)
					result.add(variable.toString());
			}
		return result;
	}

	private static Collection<AbstractAnalysisState<?>> extractWrongClassificationStates(CFGAnalysisResults<?> analysis,
			int expectedClass) {
		Set<AbstractAnalysisState<?>> result = new HashSet<>();
		for(Statement st : analysis.getKeys())
			if(st instanceof Assignment) {
				Assignment assignment = (Assignment) st;
				VariableIdentifier var = (VariableIdentifier) assignment.getAssignedVariable();
				if(var.getName().equals("w")) {
					Expression assignedExpr = assignment.getExpression();
					if(assignedExpr instanceof IntegerConstant) {
						IntegerConstant assignedValue = (IntegerConstant) assignment.getExpression();
						if(assignedValue.getValue()!=expectedClass)
							result.add(analysis.getEntryState(st));
					}
					else {
						BinaryArithmeticExpression binaryExpr = (BinaryArithmeticExpression) assignment.getExpression();
						IntegerConstant leftValue = (IntegerConstant) binaryExpr.getLeft();
						IntegerConstant rightValue = (IntegerConstant) binaryExpr.getRight();
						int value;
						switch(binaryExpr.getOperator()) {
							case "+" : value = (int) (leftValue.getValue()+rightValue.getValue()); break;
							case "-" : value = (int) (leftValue.getValue()-rightValue.getValue()); break;
							default : throw new UnsupportedOperationException("Binary arithmentic operator not yet supported in assignments to classification variable");
						}
						if(value!=expectedClass)
							result.add(analysis.getEntryState(st));
					}
				}
			}
		return result;
	}

	private static CFGAnalysisResults<?> computeCFGAnalysisResults(List<Double> vals,
			Map<Integer, Attack> attackerModel, CFG cfgTree, Map<Map<Integer, Integer>, Lincons1[]> attackerConstraint,
			apron.Environment env, boolean join) {
		List<Lincons1> constraintsTestCase = new ArrayList<>();
		
		for(int i = 1; i <= vals.size(); i++) {
			String variable;
			if(i!=vals.size()) {
				
				if(attackerModel.containsKey(Integer.valueOf(i)))
					variable = "x"+i+"_init";
				else 
					variable = "x"+i;
			}
			else //declare and assign y
				variable = "y";
			DoubleScalar[] scalar = new DoubleScalar[env.getSize()];
			for(int j = 0; j < env.getSize(); j++)
				scalar[j] = new DoubleScalar(0);
			scalar[env.dimOfVar(variable)] = new DoubleScalar(1);
			Lincons1 constraint = new Lincons1(Lincons1.EQ, new Linexpr1(env, scalar, new DoubleScalar(0-vals.get(i-1).doubleValue())), new DoubleScalar(0));
			constraintsTestCase.add(constraint);
			if(attackerModel.containsKey(Integer.valueOf(i))) {
				DoubleScalar[] scalarmin = new DoubleScalar[env.getSize()];
				DoubleScalar[] scalarmax = new DoubleScalar[env.getSize()];
				for(int j = 0; j < env.getSize(); j++) {
					scalarmin[j] = new DoubleScalar(0);
					scalarmax[j] = new DoubleScalar(0);
				}
				scalarmin[env.dimOfVar("x"+i)] = new DoubleScalar(1);
				scalarmax[env.dimOfVar("x"+i)] = new DoubleScalar(-1);
				scalarmin[env.dimOfVar("x"+i+"_min")] = new DoubleScalar(-1);
				scalarmax[env.dimOfVar("x"+i+"_max")] = new DoubleScalar(1);
				constraint = new Lincons1(Lincons1.SUPEQ, new Linexpr1(env, scalarmin, new DoubleScalar(0)), new DoubleScalar(0));
				constraintsTestCase.add(constraint);
				constraint = new Lincons1(Lincons1.SUPEQ, new Linexpr1(env, scalarmax, new DoubleScalar(0)), new DoubleScalar(0));
				constraintsTestCase.add(constraint);
			}
		}
		
		TracePartitioning initialStateForTree = TracePartitioning.createFromLincons1(env, constraintsTestCase, attackerConstraint);
		
		AbstractAnalysisState<?> entryState = new AbstractAnalysisState(null,
				join ? projectTracePartitioningOut(initialStateForTree): initialStateForTree);
		//We analyze the tree with this state
		CFGAnalysisResults<?> analysis = 
				CFGAnalysisResults.computeFixpoint(cfgTree, entryState);
		return analysis;
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
		Option output = Option.builder("o").argName("output directory").desc("Directory where to dump the result of each case").longOpt("output").hasArg(true).required(true).build();
		Option cfgresults = Option.builder("r").argName("dot results").desc("Directory where to dump the dot results of all the analyses").longOpt("dotresults").hasArg(true).build();
		Option verbose = Option.builder("v").desc("Print verbose logging").longOpt("verbose").hasArg(false).build();
		Option summaryresult = Option.builder("s").argName("summary results").desc("File where to dump a text file with all the instances wrongly classified and the total analysis time").longOpt("summaryresults").hasArg(true).required(true).build();
		Option widening = Option.builder("w").argName("threshold").desc("Threshold before applying widening operators").longOpt("widening").hasArg(true).required(true).build();
		Option cfg_splitting = Option.builder("cs").argName("LOCs").desc("Number of LOCs after that the fixpoint computation tries to split the CFG into many sequential pieces of code (default: 100)").longOpt("cfgsplitting").hasArg(true).required(false).build();
		Option attackerModel = Option.builder("a").argName("json file").desc("JSON file with the specification of the attacker").longOpt("attacker").hasArg(true).required(true).build();
		Option treeImplementation = Option.builder("ti").argName("java file").desc("Java file with the body of the decision tree").longOpt("treeimplementation").hasArg(true).required(true).build();
		Option joinPartitioning = Option.builder("j").desc("Join trace partitioning").longOpt("join").hasArg(false).build();
		Option numberOfPartitions = Option.builder("n").argName("state number").desc("Maximum number of states that a partitioning is allowed to keep").longOpt("numberofstates").hasArg(true).required(false).build();
		Option attackerstate = Option.builder("as").desc("JSON attacker state").longOpt("attackerstate").hasArg(true).required(true).build();
		Option timeout = Option.builder("t").argName("timeout (sec)").desc("Timeout in seconds per test case after which the analysis is killed").longOpt("timeout").hasArg(true).required(false).build();
		Option budget = Option.builder("b").argName("budget").desc("Tries to find the minimal budget that is enough to missclassify the instances").longOpt("budget").required(false).build();
		Option domain = Option.builder("d").argName("numerical abstract domain").desc("Numerical abstract domain to be applied during the analysis").longOpt("domain").hasArg(true).required(false).build();
		
		return new Options()
				.addOption(csv)
				.addOption(cfgresults)
				.addOption(output)
				.addOption(verbose)
				.addOption(summaryresult)
				.addOption(widening)
				.addOption(cfg_splitting)
				.addOption(attackerModel)
				.addOption(treeImplementation)
				.addOption(joinPartitioning)
				.addOption(numberOfPartitions)
				.addOption(attackerstate)
				.addOption(timeout)
				.addOption(budget)
				.addOption(domain);
	}
}
