package it.unive.dais.staticanalyzer;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.IntegerComponentNameProvider;
import org.jgrapht.io.StringComponentNameProvider;

import it.unive.dais.staticanalyzer.abstractdomain.AbstractAnalysisState;
import it.unive.dais.staticanalyzer.abstractdomain.instances.Environment;
import it.unive.dais.staticanalyzer.abstractdomain.instances.IntegerNumericalConstantDomain;
import it.unive.dais.staticanalyzer.cfg.CFG;
import it.unive.dais.staticanalyzer.cfg.CFGAnalysisResults;
import it.unive.dais.staticanalyzer.cfg.ParsingException;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;
import it.unive.dais.staticanalyzer.parser.java.BodyParser;

public class Tester {

	public static void main(String[] args) throws IOException {
		FileInputStream stream = new FileInputStream(args[0]);
		CFG cfg = new BodyParser(stream).parse();
		DOTExporter<Statement, DefaultWeightedEdge> exporter = new DOTExporter<Statement, DefaultWeightedEdge>(
				new IntegerComponentNameProvider<Statement>(),
				new StringComponentNameProvider<Statement>(),
				new ComponentNameProvider<DefaultWeightedEdge>() {

					@Override
					public String getName(DefaultWeightedEdge component) {
						Boolean b;
						try {
							b = CFG.getBooleanFromWeight(cfg.getEdgeWeight(component));
						} catch (ParsingException e) {
							// TODO Auto-generated catch block
							return "<error>";
						}
						if(b==null) return "";
						else return String.valueOf(b.booleanValue());
					}
					
				}
		);
		try(FileWriter writer = new FileWriter("cfg.dot")) {
			exporter.exportGraph(cfg, writer);
		}
		
		CFGAnalysisResults<AbstractAnalysisState<Environment<IntegerNumericalConstantDomain>>> analysis =
				CFGAnalysisResults.computeFixpoint(cfg, new AbstractAnalysisState(null, new Environment<IntegerNumericalConstantDomain>(new IntegerNumericalConstantDomain(1).bottom())));
				
				
		System.out.println(analysis);

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
							b = CFG.getBooleanFromWeight(cfg.getEdgeWeight(component));
						} catch (ParsingException e) {
							// TODO Auto-generated catch block
							return "<error>";
						}
						if(b==null) return "";
						else return String.valueOf(b.booleanValue());
					}
					
				}
		);
		try(FileWriter writer = new FileWriter("analysisresult.dot")) {
			exporter2.exportGraph(cfg, writer);
		}
		
	}

}
