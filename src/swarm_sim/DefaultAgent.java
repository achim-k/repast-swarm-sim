package swarm_sim;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.valueLayer.GridValueLayer;

public class DefaultAgent implements Agent {
	
	protected Context<Agent> context;
	protected Network<Agent> networkComm;
	protected ContinuousSpace<Agent> spaceContinuous;
	protected GridValueLayer exploredArea;
	
	@SuppressWarnings("unchecked")
	public DefaultAgent(Context<Agent> context, Context<Agent> rootContext) {
		this.context = context;
		this.spaceContinuous = (ContinuousSpace<Agent>) rootContext.getProjection(ContinuousSpace.class, "space_continuous");
		this.networkComm = (Network<Agent>) rootContext.getProjection(Network.class, "network_comm");
		this.exploredArea = (GridValueLayer) rootContext.getValueLayer("layer_explored");
	}
	
	protected void updateExploredLayer() {
		NdPoint location = spaceContinuous.getLocation(this);
		int x = (int) location.getX();
		int y = (int) location.getY();
		double value = exploredArea.get(x,y);
		exploredArea.set(value + 1, x, y);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AgentType getAgentType() {
		// TODO Auto-generated method stub
		return null;
	}
}
