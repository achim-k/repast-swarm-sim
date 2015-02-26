package swarm_sim;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.gis.GeographyWithin;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.collections.IndexedIterable;

import com.vividsolutions.jts.geom.Geometry;

public class ControllerAgent implements Agent {

	public static final int COMM_RANGE = 15000; /* in meters */

	private Context<Agent> context;
	private CommNet<Agent> commNet;
	private Geography<Agent> geography;

	/**
	 * Constructor
	 * 
	 * @param contex
	 * @param commNet
	 * @param geography
	 */
	public ControllerAgent(Context<Agent> context, CommNet<Agent> commNet,
			Geography<Agent> geography) {
		this.context = context;
		this.commNet = commNet;
		this.geography = geography;
	}

	/**
	 * Main controller
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public void run() {
		System.out.println(getName());
		/* delete network edges */
		commNet.removeEdges();
		List<Agent> edgesInContext = new ArrayList<>();
		for (Agent a : context.getObjects(CommNetEdge.class)) {
			edgesInContext.add(a);
		}
		for (Agent a : edgesInContext) {
			context.remove(a);
		}
		/* span network */
		for (Agent robot : context.getObjects(Robot.class)) {
			/* Set query for Agents which are in Range */
			GeographyWithin<Agent> inCommRangeQuery = new GeographyWithin<Agent>(
					geography, COMM_RANGE, robot);

			for (Agent agentInRange : inCommRangeQuery.query()) {
				if (agentInRange.getClass() != Robot.class)
					continue;
				if (!commNet.isAdjacent(robot, agentInRange)) {
					CommNetEdge<Agent> e = new CommNetEdge<>(robot,
							agentInRange, false, 0);
					if (!touchesObjects(e, context.getObjects(ZoneAgent.class))) {
						commNet.addEdge(e);
						context.add(e);
						geography.move(e, e.getGeometry());
					}
				}
			}
		}
	}
	
	public static Boolean touchesObjects(Agent agent, IndexedIterable<Agent> objects) {
		for (Agent a : objects) {
			if (!agent.getGeometry().disjoint(a.getGeometry())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "ControllerAgent (THE BOSS)";
	}

	@Override
	public Geometry getGeometry() {
		// TODO Auto-generated method stub
		return null;
	}

}
