package swarm_sim;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.valueLayer.GridFunction;
import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.VSpatial;
import swarm_sim.communication.DefaultNetworkAgent;

public class DefaultAgent extends DefaultNetworkAgent implements Agent {

	protected Context<Agent> context;
	protected Network<Agent> commNet;
	protected ContinuousSpace<Agent> space;
	protected AdvancedGridValueLayer exploredArea;
	protected Scenario scenario;
	public NdPoint currentLocation;
	
	protected int consecutiveMoveCount = 1;
	protected double directionAngle = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);
	
	@SuppressWarnings("unchecked")
	public DefaultAgent(Context<Agent> context, Context<Agent> rootContext) {
		this.context = context;
		this.space = (ContinuousSpace<Agent>) rootContext.getProjection(ContinuousSpace.class, "space_continuous");
		this.commNet = (Network<Agent>) rootContext.getProjection(Network.class, "network_comm");
		this.exploredArea = (AdvancedGridValueLayer) rootContext.getValueLayer("layer_explored");
		this.scenario = Scenario.getInstance();
	}
	
	protected void updateExploredLayer() {
		NdPoint location = space.getLocation(this);
//		int x = (int) location.getX();
//		int y = (int) location.getY();
//		GridPoint origin = new GridPoint(x, y);
		exploredArea.forEachRadial(new setAreaAsExplored(), location, scenario.perceptionScope);
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
	
	public VSpatial getShape(ShapeFactory2D shapeFactory) {
		return shapeFactory.createCircle(4, 16);
	}
}
