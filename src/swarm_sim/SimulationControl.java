package swarm_sim;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import swarm_sim.learning.GA;

public class SimulationControl implements IAgent {
    
    public class AgentDistancePairs {
	public IAgent source, target;
	public double distance = 0;
	public int lastTimeChecked = 0;

	public AgentDistancePairs(IAgent source, IAgent target, double distance) {
	    this.source = source;
	    this.target = target;
	    this.distance = distance;
	    this.lastTimeChecked = 0;
	}
    }
    
    Context<IAgent> context;
    Network<IAgent> commNet;

    ContinuousSpace<IAgent> space;
    AdvancedGridValueLayer exploredArea;
    
    Configuration config;
    DataCollection data;

    List<IAgent> networkAgents;
    List<AgentDistancePairs> agentDistancePairs = new ArrayList<>();
    boolean isInitiated = false;
 

    public SimulationControl(Context<IAgent> context, List<IAgent> networkAgents) {
	this.context = context;
	this.commNet = context.getProjection(Network.class, "network_comm");
	this.space = (ContinuousSpace<IAgent>) context.getProjection(
		ContinuousSpace.class, "space_continuous");
	this.config = Configuration.getInstance();
	this.exploredArea = (AdvancedGridValueLayer) context
		.getValueLayer("layer_explored");
	this.data = DataCollection.getInstance();
	this.networkAgents = networkAgents;
    }

    /**
     * initialize agent network by calculating distance pairs
     */
    public void init() {
	data.startTime = System.nanoTime();
	
	agentDistancePairs.clear();

	for (int i = 0; i < networkAgents.size(); i++) {
	    IAgent source = networkAgents.get(i);
	    for (int j = i + 1; j < networkAgents.size(); j++) {
		IAgent target = networkAgents.get(j);
		double distance = space.getDistance(space.getLocation(source),
			space.getLocation(target));
		agentDistancePairs.add(new AgentDistancePairs(source, target,
			distance));
	    }
	}
	isInitiated = true;
    }
    
    public void recalculateNetworkEdges() {
	if(!isInitiated)
	    init();
	
	long start = System.nanoTime();
	int tick = (int) RunEnvironment.getInstance().getCurrentSchedule()
		.getTickCount();

	commNet.removeEdges();
	for (AgentDistancePairs agentPair : agentDistancePairs) {
	    boolean toBeChecked = (tick - agentPair.lastTimeChecked) * 2
		    * config.maxMoveDistance >= Math.abs(agentPair.distance
		    - config.commScope);

	    if (toBeChecked) {
		agentPair.distance = space.getDistance(
			space.getLocation(agentPair.source),
			space.getLocation(agentPair.target));
		agentPair.lastTimeChecked = tick;
	    }

	    if (agentPair.distance <= config.commScope
		    && !exploredArea.isObstacleOnLine(
			    space.getLocation(agentPair.source),
			    space.getLocation(agentPair.target)))
		commNet.addEdge(agentPair.source, agentPair.target);
	}
	
	data.execTimeNetworkCalculation += System.nanoTime() - start;
    }
    
    /**
     * End the simulation when the scenario task has been finished.
     */
    public void checkIfSimFinished() {
	if (config.resourceCount > 0) {
	    /* Finish when all resources are collected */
	    if (data.deliveredResources >= config.resourceCount)
		RunEnvironment.getInstance().endRun();

	} else {
	    /* Exploration only scneario, finish when 99% is explored */
	    if (data.fieldsExplored >= 0.99 * (space.getDimensions()
		    .getHeight() * space.getDimensions().getWidth() - exploredArea
			.getObstacleFieldCount()))
		RunEnvironment.getInstance().endRun();
	}
	
	if (config.useGA) {
	    GA ga = GA.getInstance();
	    ga.currentFitness = 1.0/(int) RunEnvironment.getInstance().getCurrentSchedule()
			    .getTickCount();
	}
    }

    @Override
    public AgentType getAgentType() {
	return IAgent.AgentType.SimulationControl;
    }
}
