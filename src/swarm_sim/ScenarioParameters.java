package swarm_sim;

public class ScenarioParameters {
	
	private static ScenarioParameters instance = null;
	
	public int agentCount;
	public int perceptionScope;
	public int commScope;
	
	private ScenarioParameters() {
		
	}
	
	public static ScenarioParameters getInstance() {
		if(instance == null) {
			instance = new ScenarioParameters();
		}
		return instance;
	}
}
