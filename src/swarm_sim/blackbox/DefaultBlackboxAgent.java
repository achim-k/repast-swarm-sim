package swarm_sim.blackbox;

import repast.simphony.context.Context;
import swarm_sim.Agent;
import swarm_sim.DefaultAgent;

public class DefaultBlackboxAgent extends DefaultAgent {

	public enum agentState {
		exploring,
		blackbox_found,
	}
	
	protected agentState state = agentState.exploring;
	protected agentState prevState = agentState.exploring;
	
	public DefaultBlackboxAgent(Context<Agent> context,
			Context<Agent> rootContext) {
		super(context, rootContext);
	}

}
