package swarm_sim.foraging;

import swarm_sim.IAgent;

public class Resource implements IAgent {
    @Override
    public AgentType getAgentType() {
	return AgentType.Resource;
    }
}
