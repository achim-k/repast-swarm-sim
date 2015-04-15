package swarm_sim;

public interface Agent {

    public enum AgentType {
	SwarmAgent,
	Pheromone,
	ControllerAgent,

	Scenario,
	Base,

	/* Foraging */
	Resource,

	/* Exploration agents */
	EXPL_Random,
	EXPL_PheromoneAvoider,
	EXPL_AgentAvoiderComm,
	EXPL_AgentAvoiderMimicDirectionComm,
	EXPL_MemoryComm,
    }

    AgentType getAgentType();
}
