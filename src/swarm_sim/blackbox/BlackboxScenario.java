package swarm_sim.blackbox;

import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent.AgentType;
import swarm_sim.BaseAgent;
import swarm_sim.Scenario;

public class BlackboxScenario extends Scenario {
	
	private static BlackboxScenario instance = null;
	
	public NdPoint baseLocation;
	public boolean blackboxFound = false;

	public Blackbox blackboxAgent;
	
	public AgentType agentType;
	
	private BlackboxScenario() {
		super();
	}
	
	public static BlackboxScenario getInstance() {
		if(instance == null) {
			instance = new BlackboxScenario();
		}
		return instance;
	}
	
	
}
