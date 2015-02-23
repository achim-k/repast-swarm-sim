package swarm_sim;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.valueLayer.ContinuousValueLayer;

public class Robot {
	private Context<Object> context;
	private ContinuousSpace<Object> space;
	private ContinuousValueLayer exploredArea;
	private int number;
	
	
	private static double commScopeRadius = 10;
	
	private Network<Object> commNetwork;
	
	private static int robotNumber = 1;

	
	@SuppressWarnings("unchecked")
	public Robot(Context<Object> context, ContinuousSpace<Object> space, ContinuousValueLayer exploredArea) {
		this.context = context;
		this.space = space;
		this.exploredArea = exploredArea;
		this.commNetwork = (Network<Object>)context.getProjection("comm_network");
		this.number = robotNumber++; 
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void run() {
		// System.out.println(location);
		double moveX = RandomHelper.nextDoubleFromTo(-1, 1);
		double moveY = RandomHelper.nextDoubleFromTo(-1, 1);
		NdPoint newLocation = this.space.moveByDisplacement(this, moveX, moveY);
		this.exploredArea.set(10, (int) newLocation.getX(),
				(int) newLocation.getY());

		/* look for Robots in comm scope */
		for (Object obj : this.space.getObjects()) {
			Robot r = (Robot) obj;
			RepastEdge<Object> edge;
			if(space.getDistance(newLocation, space.getLocation(r)) <= commScopeRadius) {
				this.commNetwork.addEdge(this, obj);
			} else if ((edge = this.commNetwork.getEdge(this, r)) != null) {
				this.commNetwork.removeEdge(edge);
			}
		}
	}
	
	public String getName() {
		return "Robot " + this.number;
	}
	
	public int getEdgeCount() {
		int count = 0;
		for (Object o : this.commNetwork.getEdges(this)) {
			count++;
		} 
		return count;
	}

}
