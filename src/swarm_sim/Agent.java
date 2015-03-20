package swarm_sim;

public interface Agent {
	
	public enum AgentType {
		SwarmAgent,
		Pheromone,
		ControllerAgent,
		
		
		
		/* Blackbox agents */
		BB_RandomExplorer,
	}
	
	String getName();
	AgentType getAgentType();
}
