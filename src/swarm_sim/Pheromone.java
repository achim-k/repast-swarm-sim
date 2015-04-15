package swarm_sim;


public class Pheromone implements Agent {

    public Pheromone() {
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.Pheromone;
    }
}
