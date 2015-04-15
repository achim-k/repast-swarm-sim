package swarm_sim;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.ContinuousSpace;
import swarm_sim.Scenario.AgentDistancePairs;

public class ControllerAgent implements Agent {
    private Context<Agent> context;
    private CommNet<Agent> commNet;
    private ContinuousSpace<Agent> space;
    protected AdvancedGridValueLayer exploredArea;

    private Scenario scenario;

    public ControllerAgent(Context<Agent> context, Scenario scenario) {
	this.context = context;
	this.commNet = context.getProjection(CommNet.class, "network_comm");
	this.space = (ContinuousSpace<Agent>) context.getProjection(
		ContinuousSpace.class, "space_continuous");
	this.scenario = scenario;
	this.exploredArea = (AdvancedGridValueLayer) context
		.getValueLayer("layer_explored");
    }

    public void step() {
	// for (int i = 0; i < scenario.movebins.length; i++) {
	// System.out.print(scenario.movebins[i] + ", ");
	// }
	// System.out.println();

	if (scenario.agentDistancePairs.size() == 0)
	    scenario.init();

	int tick = (int) RunEnvironment.getInstance().getCurrentSchedule()
		.getTickCount();

	commNet.removeEdges();
	for (AgentDistancePairs agentPair : scenario.agentDistancePairs) {
	    boolean toBeChecked = (tick - agentPair.lastTimeChecked) * 2
		    * scenario.maxMoveDistance >= Math.abs(agentPair.distance
		    - scenario.commScope);

	    if (toBeChecked) {
		agentPair.distance = space.getDistance(
			space.getLocation(agentPair.source),
			space.getLocation(agentPair.target));
		agentPair.lastTimeChecked = tick;
	    }

	    if (agentPair.distance <= scenario.commScope
		    && !exploredArea.isObstacleOnLine(
			    space.getLocation(agentPair.source),
			    space.getLocation(agentPair.target)))
		commNet.addEdge(agentPair.source, agentPair.target);
	}

    }

    @Override
    public AgentType getAgentType() {
	// TODO Auto-generated method stub
	return Agent.AgentType.ControllerAgent;
    }
}
