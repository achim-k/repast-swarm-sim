package swarm_sim;

public abstract class AbstractAgent {

    public enum AgentType {
	SwarmAgent, Pheromone, SimulationControl,

	Base, GeneticAlgorithm,

	/* Foraging */
	Resource,

	DataCollection, Configuration,

    }

    public abstract AgentType getAgentType();

    public boolean hasFailed() {
	return false;
    };
}
