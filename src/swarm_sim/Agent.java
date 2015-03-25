package swarm_sim;

public interface Agent {
	
	public enum AgentType {
		SwarmAgent,
		Pheromone,
		ControllerAgent,
		
		Scenario,
		Base,
		
		/* Blackbox agents */
		Blackbox,
		BB_RandomExplorerNoComm, BB_RandomExplorerWithComm,
	}
	
	AgentType getAgentType();
}
