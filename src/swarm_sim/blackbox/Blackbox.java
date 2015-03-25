package swarm_sim.blackbox;

import swarm_sim.Agent;

public class Blackbox implements Agent {

	@Override
	public AgentType getAgentType() {
		return AgentType.Blackbox;
	}
}
