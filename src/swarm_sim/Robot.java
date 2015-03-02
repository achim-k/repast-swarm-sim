package swarm_sim;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;
import repast.simphony.valueLayer.ContinuousValueLayer;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.query.space.gis.GeographyWithin;
import repast.simphony.query.space.gis.IntersectsQuery;

public class Robot implements Agent {
	private Context<Agent> context;
	private ContinuousSpace<Agent> space;
	private ContinuousValueLayer exploredArea;
	private Geography geography;
	private int number;

	private double commScopeRadius = 10000;

	private CommNet<Agent> commNetwork;

	private static int robotNumber = 1;

	public Robot(Context<Agent> context, ContinuousSpace<Agent> space,
			ContinuousValueLayer exploredArea, Geography geography) {
		this.context = context;
		this.space = space;
		this.exploredArea = exploredArea;
		this.geography = geography;
		this.commNetwork = (CommNet<Agent>) context
				.getProjection("comm_network");
		this.number = robotNumber++;
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void run() {
		System.out.println(getName());
		randomWalk();
		/* Create Pheromone */
//		Pheromone p = new Pheromone(this.getGeometry());
//		context.add(p);
//		geography.move(p, p.getGeometry());

		// System.out.println(location);
		double moveX = RandomHelper.nextDoubleFromTo(-1, 1);
		double moveY = RandomHelper.nextDoubleFromTo(-1, 1);
		NdPoint newLocation = this.space.moveByDisplacement(this, moveX, moveY);
		this.exploredArea.set(10, (int) newLocation.getX(),
				(int) newLocation.getY());


	}

	/**
	 * Random walk the agent around.
	 */
	private void randomWalk() {
		Context context = ContextUtils.getContext(this);
		Geography<Robot> geography = (Geography) context
				.getProjection("geography");

		//
		do {
			geography.moveByDisplacement(this,
					RandomHelper.nextDoubleFromTo(-0.005, 0.005),
					RandomHelper.nextDoubleFromTo(-0.005, 0.005));
		} while (ControllerAgent.touchesObjects(this, context.getObjects(ZoneAgent.class)));
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

	@Override
	public Geometry getGeometry() {
		// TODO Auto-generated method stub
		return this.geography.getGeometry(this);
	}
}
