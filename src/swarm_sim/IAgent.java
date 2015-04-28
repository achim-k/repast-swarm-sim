package swarm_sim;

public interface IAgent {

    public enum AgentType {
	SwarmAgent,
	Pheromone,
	SimulationControl,

	Scenario,
	Base,
	GeneticAlgorithm,

	/* Foraging */
	Resource,
	FAGN_Random,

	/* Exploration agents */
	EXPL_Random,
	EXPL_PheromoneAvoider,
	EXPL_AgentRepell,
	EXPL_AgentAvoiderMimic,
	EXPL_Memory,
	EXPL_AvoidAppealMimicMemory,
	DataCollection,

    }

    AgentType getAgentType();
}
