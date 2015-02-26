package swarm_sim;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.ContextJungNetwork;
import repast.simphony.space.graph.UndirectedJungNetwork;

public class CommNet<Agent> extends ContextJungNetwork<Agent> {

	public CommNet(String name, Context<Agent> context) {
		super(new UndirectedJungNetwork<Agent>(name), context);
	}
	
	/**
	 * Checks if a and b are connected with an edge
	 * @param a
	 * @param b
	 * @return
	 */
	public Boolean areConnected(Agent a, Agent b) { 
		for (Agent agent : this.getAdjacent(a)) {
			if(agent.equals(b))
				return true;
		}
		return false;
	}
}
