package swarm_sim;

public interface Agent {

	public enum AgentType {
		SwarmAgent, Pheromone, ControllerAgent,

		Scenario, Base,

		/* Blackbox agents */
		Blackbox, BB_Random, BB_RandomComm, BB_PheromoneAvoider, BB_RandomPoint, BB_AgentAvoiderComm, BB_AgentAvoiderMimicDirectionComm, BB_RandomObstacleAvoider,
	}

	AgentType getAgentType();
}
