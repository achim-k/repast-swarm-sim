package swarm_sim;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.valueLayer.GridFunction;
import swarm_sim.blackbox.BlackboxScenario;

public class DefaultAgent implements Agent {
	
	protected Context<Agent> context;
	protected Network<Agent> networkComm;
	protected ContinuousSpace<Agent> spaceContinuous;
	protected AdvancedGridValueLayer exploredArea;
	protected BlackboxScenario scenario;
	public NdPoint currentLocation;
	
	@SuppressWarnings("unchecked")
	public DefaultAgent(Context<Agent> context, Context<Agent> rootContext) {
		this.context = context;
		this.spaceContinuous = (ContinuousSpace<Agent>) rootContext.getProjection(ContinuousSpace.class, "space_continuous");
		this.networkComm = (Network<Agent>) rootContext.getProjection(Network.class, "network_comm");
		this.exploredArea = (AdvancedGridValueLayer) rootContext.getValueLayer("layer_explored");
		this.scenario = BlackboxScenario.getInstance();
	}
	
	protected void updateExploredLayer() {
		NdPoint location = spaceContinuous.getLocation(this);
		int x = (int) location.getX();
		int y = (int) location.getY();
		GridPoint origin = new GridPoint(x, y);
		exploredArea.forEachRadial(new setAreaAsExplored(), origin, scenario.perceptionScope);
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
	
	private class setAreaAsExplored implements GridFunction {
		@Override
		public void apply(double gridValue, int... location) {
			if(gridValue == 0)
				scenario.exploredAreaCount++;
			else
				scenario.redundantExploredAreaCount++;
			exploredArea.set(gridValue + 1, location[0], location[1]);
		}
		
	}
}
