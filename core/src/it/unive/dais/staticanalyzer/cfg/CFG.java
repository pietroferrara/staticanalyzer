package it.unive.dais.staticanalyzer.cfg;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.IntegerComponentNameProvider;
import org.jgrapht.io.StringComponentNameProvider;

import it.unive.dais.staticanalyzer.AnalysisConstants;
import it.unive.dais.staticanalyzer.cfg.statement.Statement;

/**
 * The class representing the control flow graph of a program
 * @author Pietro Ferrara
 *
 */
public class CFG extends ParsedBlock {
	private final DefaultDirectedWeightedGraph<Statement, DefaultWeightedEdge> graph; 
	
	//The first statement added to the CFG
	private Statement entryPoint;
	//The last statement added to the CFG
	private Statement lastAdded;
	
	CFG(int line, int column) {
		super(line, column);
		this.graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
	}
	CFG(Statement st) throws ParsingException {
		super(st.getLine(), st.getColumn());
		this.graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		addAndCheckVertex(st);
		this.entryPoint = st;
		this.lastAdded = st;
	}
	
	private Statement getNextBreakPoint(Statement initialStatement) {
		try {
			Statement nextOne = initialStatement;
			while(nextOne != null) {
				if(Math.abs(nextOne.getLine()-initialStatement.getLine())>AnalysisConstants.CFG_LIMIT)
					return nextOne;
				Set<DefaultWeightedEdge> outgoingEdges = this.graph.outgoingEdgesOf(nextOne);
				switch(outgoingEdges.size()) {
					case 1:
						DefaultWeightedEdge edge = outgoingEdges.iterator().next();
						Boolean weight = getBooleanFromWeight(this.graph.getEdgeWeight(edge));
						if(weight!=null)
							return null;
						nextOne = this.graph.getEdgeTarget(edge);
						break;
					case 2:
						Iterator<DefaultWeightedEdge> it = outgoingEdges.iterator();
						DefaultWeightedEdge left = it.next();
						DefaultWeightedEdge right = it.next();
						Boolean weightleft = getBooleanFromWeight(this.graph.getEdgeWeight(left));
						Boolean weightright = getBooleanFromWeight(this.graph.getEdgeWeight(right));
						if(weightleft == null || weightright == null || weightleft == weightright)
							return null;
						Statement jointPointLeft = getFirstJointPointStartingFrom(this.graph.getEdgeTarget(left));
						Statement jointPointRight = getFirstJointPointStartingFrom(this.graph.getEdgeTarget(right));
						if(jointPointLeft!=jointPointRight)
							return null;
						nextOne = jointPointLeft;
						break;
					default: return null;
				}
			}
			return null;
		}
		catch(ParsingException e) {return null;}
	}
	
	private Statement getFirstJointPointStartingFrom(Statement initialStatement) {
		int tobeDiscarded = 1;
		Statement next = initialStatement;
		while(next!=null) {
			Set<DefaultWeightedEdge> incomingEdges = this.graph.incomingEdgesOf(next);
			if(incomingEdges.size() == 2) {
				tobeDiscarded--;
				if(tobeDiscarded==0)
					return next;
			}
			Set<DefaultWeightedEdge> outgoingEdges = this.graph.outgoingEdgesOf(next);
			switch(outgoingEdges.size()) {
				case 1: next = this.graph.getEdgeTarget(outgoingEdges.iterator().next()); break;
				case 2: tobeDiscarded++;
					next = this.graph.getEdgeTarget(outgoingEdges.iterator().next()); break;
				default: return null;
			}
		}
		return null;
	}
	private List<Statement> getBreakPoints() {
		List<Statement> breakPoints = new ArrayList<>();
		Statement current = this.entryPoint;
		while(current!=null) {
			current = this.getNextBreakPoint(current);
			if(current!=null)
				breakPoints.add(current);
		}
		return breakPoints;
	}
	
	private List<Collection<Statement>> orderedStatements = null;
	
	List<Collection<Statement>> getOrderedStatements(Comparator<Statement> comparator) {
		if(orderedStatements!=null)
			return orderedStatements;
		List<Statement> breakPoints = this.getBreakPoints();
		Statement initial = this.entryPoint;
		List<Collection<Statement>> result = new ArrayList<>();
		for(Statement st : breakPoints) {
			result.add(this.getOrderedStatementsFromAndUpTo(comparator, initial, st));
			initial = st;
		}
		result.add(this.getOrderedStatementsFromAndUpTo(comparator, initial, null));
		orderedStatements = result;
		return result;	
	}
	
	/*Collection<Statement> getOrderedStatements(Comparator<Statement> comparator) {
		return this.getOrderedStatementsFromAndUpTo(comparator, this.entryPoint, null);
	}*/
	
	private Collection<Statement> getOrderedStatementsFromAndUpTo(Comparator<Statement> comparator, Statement from, Statement to) {
		ArrayList<Statement> prev = new ArrayList<>();
		ArrayList<Statement> result = new ArrayList<>();
		result.add(from);
		Set<Statement> lastAdded = new HashSet<>();
		lastAdded.add(from);
		while(result.size()>prev.size()) {
			prev = result;
			result = new ArrayList<>(prev);
			Set<Statement> added = new HashSet<>();
			for(Statement st : lastAdded) {
				for(DefaultWeightedEdge edge : graph.outgoingEdgesOf(st)) {
					Statement target = graph.getEdgeTarget(edge);
					if(! prev.contains(target) && !target.equals(to)) {
						result.add(target);
						added.add(target);
					}
				}
			}
			lastAdded = added;
		}
		if(comparator!=null) {
			TreeSet<Statement> ts = new TreeSet<Statement>(comparator);
			ts.addAll(result);
			return ts;
		}
		else return result;
	}

	
	void append(Statement statement) throws ParsingException {
		//empty statement
		if(statement == null)
			return;
		graph.addVertex(statement);
		if(getEntryPoint() == null)
			this.entryPoint = statement;
		if(getLastAdded() != null)
			addAndCheckEdge(getLastAdded(), statement, null);
		this.lastAdded = statement;
	}
	

	void append(CFG visitBlockStatement, Statement from, Boolean condition) throws ParsingException {
		this.lastAdded = from;
		this.append(visitBlockStatement, condition);
	}
	
	DefaultDirectedWeightedGraph<Statement, DefaultWeightedEdge> getGraph() {
		return graph;
	}

	void append(CFG visitBlockStatement, Boolean condition) throws ParsingException {
		//empty CFG
		if(visitBlockStatement == null || visitBlockStatement.isEmpty())
			return;
		
		for(Statement v : visitBlockStatement.getGraph().vertexSet())
			addAndCheckVertex(v);
		for(DefaultWeightedEdge e : visitBlockStatement.getGraph().edgeSet()) {
			Statement source = visitBlockStatement.getGraph().getEdgeSource(e);
			Statement target = visitBlockStatement.getGraph().getEdgeTarget(e);
			double weight = visitBlockStatement.getGraph().getEdgeWeight(e);
			addAndCheckEdge(source, target, getBooleanFromWeight(weight));
		}
		
		if(getLastAdded() != null)
			addAndCheckEdge(getLastAdded(), visitBlockStatement.getEntryPoint(), condition);
		this.lastAdded = visitBlockStatement.getLastAdded();
		if(getEntryPoint() == null)
			this.entryPoint = visitBlockStatement.getEntryPoint();
	}

	private boolean isEmpty() {
		return getEntryPoint() == null && getLastAdded() == null && this.graph.vertexSet().isEmpty() && this.graph.edgeSet().isEmpty();
	}

	void addAndCheckEdge(Statement from, Statement to, Boolean weight) throws ParsingException {
		if(from.isTerminatingStatement())
			throw new UnsupportedOperationException("It is not allowed to add outcoming edges from terminating statements");
		DefaultWeightedEdge edge = graph.addEdge(from, to); 
		if(edge==null)
			throw new ParsingException("Edge from "+from+" to "+to+" already contained in the current CFG, or the vertexes of the edge were not contained");
		graph.setEdgeWeight(edge, getWeightFromBoolean(weight));
	}
	
	static double getWeightFromBoolean(Boolean b) {
		if(b==null)
			return 0.0;
		else if(b.booleanValue())
			return 1.0;
		else return -1.0;
	}
	
	static Boolean getBooleanFromWeight(double d) throws ParsingException {
		if(d==0.0)
			return null;
		else if(d==1.0)
			return true;
		else if(d==-1.0)
			return false;
		else throw new ParsingException("Unkown edge weight: "+d);
	}
	
	void addAndCheckVertex(Statement v) throws ParsingException {
		if(! graph.addVertex(v))
			throw new ParsingException("Vertex "+v.toString()+" already contained in the current CFG");
	}
	Statement getLastAdded() {
		return lastAdded;
	}
	Statement getEntryPoint() {
		return entryPoint;
	}
	

	/**
	 * Dump the control flow graph to a dot file
	 * @param cfgOutput the path where the dot file is written
	 * @throws IOException
	 */
	public void dumpToDotFile(String cfgOutput) throws IOException {
		DOTExporter<Statement, DefaultWeightedEdge> exporter = new DOTExporter<Statement, DefaultWeightedEdge>(
				new IntegerComponentNameProvider<Statement>(),
				new StringComponentNameProvider<Statement>(),
				new ComponentNameProvider<DefaultWeightedEdge>() {

					@Override
					public String getName(DefaultWeightedEdge component) {
						Boolean b;
						try {
							b = CFG.getBooleanFromWeight(getGraph().getEdgeWeight(component));
						} catch (ParsingException e) {
							return "<error>";
						}
						if(b==null) return "";
						else return String.valueOf(b.booleanValue());
					}
					
				}
		);
		try(FileWriter writer = new FileWriter(cfgOutput)) {
			exporter.exportGraph(getGraph(), writer);
		}
	}
	
	/**
	 * 
	 * @return the statements contained in the control flow graph
	 */
	public Set<Statement> statements() {
		return this.graph.vertexSet();
	}
	
}
