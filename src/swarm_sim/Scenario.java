package swarm_sim;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.ContextUtils;


public class Scenario implements Agent {
	
	public int agentCount;
	public int perceptionScope;
	public int commScope;
	public double agentMovementSpeed = 1.0;
	public BaseAgent baseAgent;
	
	/* Data */
	public int exploredAreaCount = 0;
	public int redundantExploredAreaCount = 0;
	
	public int getExploredAreaCount() {
		return exploredAreaCount;
	}
	
	public int getRedundantExploredAreaCount() {
		return redundantExploredAreaCount;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AgentType getAgentType() {
		// TODO Auto-generated method stub
		return AgentType.Scenario;
	}
	
	public void init() {
		/* Move all agents to the base */
		/* Context<Agent> context = ContextUtils.getContext(this);
		ContinuousSpace<Agent> spaceContinuous = (ContinuousSpace<Agent>) context.getProjection(ContinuousSpace.class, "space_continuous");
		NdPoint baseLocation = spaceContinuous.getLocation(baseAgent);
		
		for(Agent agent : context.getAgentLayer(Agent.class)){
			switch (agent.getAgentType()) {
			
			case Base:
			case Blackbox:
			case Pheromone:
				break;
			default:
				
				spaceContinuous.moveTo(agent, baseLocation.getX(), baseLocation.getY());
				break;
			}
		}
*/
	}
}
