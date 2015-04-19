package swarm_sim.foraging;

import swarm_sim.Agent;

public class Resource implements Agent {
    @Override
    public AgentType getAgentType() {
	return AgentType.Resource;
    }
}
