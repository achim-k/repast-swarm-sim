package swarm_sim;

public interface IAgent {

    public enum AgentType {
	SwarmAgent,
	Pheromone,
	SimulationControl,

	Base,
	GeneticAlgorithm,

	/* Foraging */
	Resource,

	DataCollection, 
	Configuration,

    }

    AgentType getAgentType();
}
