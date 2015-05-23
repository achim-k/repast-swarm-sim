package swarm_sim.foraging;

import swarm_sim.AbstractAgent;

public class Resource extends AbstractAgent {
    @Override
    public AgentType getAgentType() {
	return AgentType.Resource;
    }
}
