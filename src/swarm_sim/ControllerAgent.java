package swarm_sim;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.continuous.ContinuousSpace;
import swarm_sim.Scenario.AgentDistancePairs;

public class ControllerAgent implements Agent {
	private Context<Agent> context;
	private CommNet<Agent> commNet;
	private ContinuousSpace<Agent> spaceContinuous;

	private Scenario scenario;

	public ControllerAgent(Context<Agent> context, Scenario scenario) {
		this.context = context;
		this.commNet = context.getProjection(CommNet.class, "network_comm");
		this.spaceContinuous = (ContinuousSpace<Agent>) context.getProjection(
				ContinuousSpace.class, "space_continuous");
		this.scenario = scenario;
	}

	public void step() {
		
		if (scenario.agentDistancePairs.size() == 0)
			scenario.init();

		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule()
				.getTickCount();
		
		commNet.removeEdges();
		for (AgentDistancePairs agentPair : scenario.agentDistancePairs) {
			boolean toBeChecked = (tick - agentPair.lastTimeChecked) * 2
					* scenario.agentMovementSpeed >= Math
					.abs(agentPair.distance - scenario.commScope);
//			toBeChecked = true;
			if (toBeChecked) {
				agentPair.distance = spaceContinuous.getDistance(
						spaceContinuous.getLocation(agentPair.source),
						spaceContinuous.getLocation(agentPair.target));
				agentPair.lastTimeChecked = tick;
			}

			if (agentPair.distance <= scenario.commScope)
				commNet.addEdge(agentPair.source, agentPair.target);
		}

//		commNet.removeEdges();
//		for (int i = 0; i < scenario.networkAgents.size(); i++) {
//			Agent source = scenario.networkAgents.get(i);
//			for (int j = i + 1; j < scenario.networkAgents.size(); j++) {
//				Agent target = scenario.networkAgents.get(j);
//
//				double dist = spaceContinuous.getDistance(
//						spaceContinuous.getLocation(source),
//						spaceContinuous.getLocation(target));
//				if (dist <= scenario.commScope) {
//					commNet.addEdge(source, target);
//				}
//			}
//		}

		/* Update communication network */
		// System.out.println(this.getName());
		// List<RepastEdge> edges =
		// scenario.agentNet.getActiveEdges(spaceContinuous,
		// scenario.commScope, scenario.agentMovementSpeed);
		// commNet.removeEdges();
		// System.out.println("Total edges: " + commNet.getDegree());
		// int i =0;
		// for (RepastEdge<Agent> edge : edges) {
		// CommNetEdge<Agent> e = (CommNetEdge<Agent>)edge;
		// commNet.addEdge(edge);
		// i++;
		// }
		// System.out.println(i +" edges added");
		// System.out.println("Total edges: " + commNet.getDegree());
	}

	@Override
	public AgentType getAgentType() {
		// TODO Auto-generated method stub
		return Agent.AgentType.ControllerAgent;
	}
}
