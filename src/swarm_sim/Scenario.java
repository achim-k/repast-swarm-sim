package swarm_sim;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.ContextUtils;

public class Scenario implements Agent {

	public int agentCount;
	public double perceptionScope;
	public double commScope;
	public double maxMoveDistance = 1.0;
	public Base baseAgent;
	public List<Agent> networkAgents = new ArrayList<>();
	public List<AgentDistancePairs> agentDistancePairs = new ArrayList<>();
	
	public int randomConsecutiveMoves = 1;
	
	/* Data */
	public int exploredAreaCount = 0;
	public int redundantExploredAreaCount = 0;
	public int messagesSent = 0;
	
	public int[] movebins = new int[8];

	
	private static Scenario instance = null;
	
	protected Scenario() {
	}

	public static Scenario getInstance() {
		if(instance == null) {
			instance = new Scenario();
		}
		return instance;
	}
	
	
	public int getExploredAreaCount() {
		return exploredAreaCount;
	}

	public int getRedundantExploredAreaCount() {
		return redundantExploredAreaCount;
	}

	@Override
	public AgentType getAgentType() {
		// TODO Auto-generated method stub
		return AgentType.Scenario;
	}
	
	private void reset() {
		agentDistancePairs.clear();
		exploredAreaCount = 0;
		redundantExploredAreaCount = 0;
		messagesSent = 0;
	}

	public void init() {
		reset();
		
		Context<Agent> context = ContextUtils.getContext(this);
		ContinuousSpace<Agent> space = (ContinuousSpace<Agent>) context
				.getProjection(ContinuousSpace.class, "space_continuous");
		/* initialize agent network by calculating distance pairs */
		
		for (int i = 0; i < networkAgents.size(); i++) {
			Agent source = networkAgents.get(i);
			for (int j = i + 1; j < networkAgents.size(); j++) {
				Agent target = networkAgents.get(j);
				double distance = space.getDistance(space.getLocation(source),
						space.getLocation(target));
				agentDistancePairs.add(new AgentDistancePairs(source, target, distance));
			}
		}
	}

	public class AgentDistancePairs {
		public Agent source, target;
		public double distance = 0;
		public int lastTimeChecked = 0;
		
		public AgentDistancePairs(Agent source, Agent target, double distance) {
			this.source = source;
			this.target = target;
			this.distance = distance;
			this.lastTimeChecked = 0;
		}
	}

}
