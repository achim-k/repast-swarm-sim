package swarm_sim;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.ContextJungNetwork;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.graph.UndirectedJungNetwork;

public class AgentNet {

	private List<CommNetEdge<Agent>> agentsEdges = new ArrayList<CommNetEdge<Agent>>();
	
	public AgentNet() {
	}
	
	public void init(List<Agent> agents, ContinuousSpace<Agent> space, double commScope, double agentSpeed) {
		for(int i = 0; i < agents.size(); i++) {
			Agent source = agents.get(i);
			for (int j = 0; j < agents.size(); j++) {
				Agent target = agents.get(j);
				CommNetEdge<Agent> edge= new CommNetEdge<>(source, target, space, commScope, agentSpeed);
				agentsEdges.add(edge);
			}
		}
	}
	
	public List<RepastEdge> getActiveEdges(ContinuousSpace<Agent> space, double commScope, double agentSpeed) {
		List<RepastEdge> ret = new ArrayList<>();
		for (CommNetEdge edge : agentsEdges) {
			if(edge.isActive(space, commScope, agentSpeed))
				ret.add(edge);
		}
		return ret;
	}
}
