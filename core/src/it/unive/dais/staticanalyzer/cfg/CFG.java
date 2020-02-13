package it.unive.dais.staticanalyzer.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.unive.dais.staticanalyzer.cfg.statement.Statement;

public class CFG extends DefaultDirectedWeightedGraph<Statement, DefaultWeightedEdge> implements ParsedBlock {
	private static final long serialVersionUID = -5787684749175918173L;
	
	
	//The first statement added to the CFG
	private Statement entryPoint;
	//The last statement added to the CFG
	private Statement lastAdded;
	
	public CFG() {
		super(DefaultWeightedEdge.class);
	}
	public CFG(Statement st) throws ParsingException {
		super(DefaultWeightedEdge.class);
		addAndCheckVertex(st);
		this.entryPoint = st;
		this.lastAdded = st;
	}
	
	ArrayList<Statement> getOrderedStatements() {
		ArrayList<Statement> prev = new ArrayList<>();
		ArrayList<Statement> result = new ArrayList<>();
		result.add(this.entryPoint);
		Set<Statement> lastAdded = new HashSet<>();
		lastAdded.add(this.entryPoint);
		while(result.size()>prev.size()) {
			prev = result;
			result = new ArrayList<>(prev);
			Set<Statement> added = new HashSet<>();
			for(Statement st : lastAdded) {
				for(DefaultWeightedEdge edge : this.outgoingEdgesOf(st)) {
					Statement target = this.getEdgeTarget(edge);
					if(! prev.contains(target)) {
						result.add(target);
						added.add(target);
					}
				}
			}
			lastAdded = added;
		}
		return result;
	}

	
	public void append(Statement statement) throws ParsingException {
		//empty statement
		if(statement == null)
			return;
		this.addVertex(statement);
		if(getEntryPoint() == null)
			this.entryPoint = statement;
		if(getLastAdded() != null)
			addAndCheckEdge(getLastAdded(), statement, null);
		this.lastAdded = statement;
	}
	

	public void append(CFG visitBlockStatement, Statement from, Boolean condition) throws ParsingException {
		this.lastAdded = from;
		this.append(visitBlockStatement, condition);
	}

	public void append(CFG visitBlockStatement, Boolean condition) throws ParsingException {
		//empty CFG
		if(visitBlockStatement == null || visitBlockStatement.isEmpty())
			return;
		
		for(Statement v : visitBlockStatement.vertexSet())
			addAndCheckVertex(v);
		for(DefaultWeightedEdge e : visitBlockStatement.edgeSet()) {
			Statement source = visitBlockStatement.getEdgeSource(e);
			Statement target = visitBlockStatement.getEdgeTarget(e);
			double weight = visitBlockStatement.getEdgeWeight(e);
			addAndCheckEdge(source, target, getBooleanFromWeight(weight));
		}
		
		if(getLastAdded() != null)
			addAndCheckEdge(getLastAdded(), visitBlockStatement.getEntryPoint(), condition);
		this.lastAdded = visitBlockStatement.getLastAdded();
		if(getEntryPoint() == null)
			this.entryPoint = visitBlockStatement.getEntryPoint();
	}

	private boolean isEmpty() {
		return getEntryPoint() == null && getLastAdded() == null && this.isEmpty();
	}

	public void addAndCheckEdge(Statement from, Statement to, Boolean weight) throws ParsingException {
		if(from.isTerminatingStatement())
			throw new UnsupportedOperationException("It is not allowed to add outcoming edges from terminating statements");
		DefaultWeightedEdge edge = this.addEdge(from, to); 
		if(edge==null)
			throw new ParsingException("Edge from "+from+" to "+to+" already contained in the current CFG, or the vertexes of the edge were not contained");
		this.setEdgeWeight(edge, getWeightFromBoolean(weight));
	}
	
	public static double getWeightFromBoolean(Boolean b) {
		if(b==null)
			return 0.0;
		else if(b.booleanValue())
			return 1.0;
		else return -1.0;
	}
	
	public static Boolean getBooleanFromWeight(double d) throws ParsingException {
		if(d==0.0)
			return null;
		else if(d==1.0)
			return true;
		else if(d==-1.0)
			return false;
		else throw new ParsingException("Unkown edge weight: "+d);
	}
	
	private void addAndCheckVertex(Statement v) throws ParsingException {
		if(! this.addVertex(v))
			throw new ParsingException("Vertex "+v.toString()+" already contained in the current CFG");
	}
	public Statement getLastAdded() {
		return lastAdded;
	}
	public Statement getEntryPoint() {
		return entryPoint;
	}
	
}
