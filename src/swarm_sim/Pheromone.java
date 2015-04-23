package swarm_sim;


public class Pheromone implements IAgent {

    public Pheromone() {
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.Pheromone;
    }
}
