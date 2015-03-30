package swarm_sim.blackbox;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.DisplayAgent;
import swarm_sim.Pheromone;
import swarm_sim.ScanData;
import swarm_sim.communication.MsgBlackboxFound;
import swarm_sim.communication.NetworkAgent;

public class BB_PheromoneAvoider extends DefaultBlackboxAgent implements
		Agent, DisplayAgent {

	static int agentNo;
	List<Agent> pheromonesInRange = new ArrayList<Agent>();

	ScanData pheromoneScan = new ScanData(8, scenario.perceptionScope, 1);

	public void step() {
		move();
		if (scanEnv()) {
			bbScenario.blackboxFound();
			state = agentState.blackbox_found;
		}
		prevState = state;
	}

	private void move() {
		double speed = scenario.agentMovementSpeed;

		if (state == agentState.exploring) {
			/* Explore environment randomly */
			double cameFromAngle = directionAngle + Math.PI;
			cameFromAngle = cameFromAngle > Math.PI ? cameFromAngle - 2
					* Math.PI : cameFromAngle;
			
			pheromoneScan.addData(cameFromAngle, 0.2);
			directionAngle = pheromoneScan.getMovementAngle();
//			System.out.println(pheromoneScan.getPrintable("a"));
//			System.out.println("→ " + directionAngle);

			// directionAngle += Math.PI;
			directionAngle = directionAngle > 2 * Math.PI ? directionAngle - 2
					* Math.PI : directionAngle;

			currentLocation = space
					.moveByVector(this, speed, directionAngle, 0);

			if (consecutiveMoveCount >= scenario.randomConsecutiveMoves) {
				consecutiveMoveCount = 1;
				/* lay Pheromone */
				if (pheromonesInRange.size() < 1) {
					Pheromone p = new Pheromone();
					context.add(p);
					space.moveTo(p, currentLocation.getX(),
							currentLocation.getY());
				}
			} else {
				consecutiveMoveCount++;
			}

		} else if (state == agentState.blackbox_found) {
			/* Go back to base */
			NdPoint baseLocation = space.getLocation(scenario.baseAgent);

			double moveDistance = space.getDistance(currentLocation,
					baseLocation);
			if (moveDistance > speed) {
				moveDistance = speed;
			}
			double movementAngle = SpatialMath.calcAngleFor2DMovement(space,
					currentLocation, baseLocation);
			currentLocation = space.moveByVector(this, moveDistance,
					movementAngle, 0);

			if (moveDistance <= 0.2) {
				System.out
						.println("Agent which found BB has arrived at Base, end of Simulation");
				RunEnvironment.getInstance().endRun();
			}
		}
		updateExploredLayer();
	}

	private boolean scanEnv() {

		/* Pheromone scan */
		pheromonesInRange.clear();
		pheromoneScan.clear();

		ContinuousWithin<Agent> withinQuery = new ContinuousWithin<Agent>(
				space, this, scenario.perceptionScope);
		for (Agent agent : withinQuery.query()) {
			switch (agent.getAgentType()) {
			case Pheromone:
				pheromonesInRange.add(agent);
				double angle = SpatialMath.calcAngleFor2DMovement(space,
						currentLocation, space.getLocation(agent));
				double distance = space.getDistance(space.getLocation(this),
						space.getLocation(agent));
				pheromoneScan.addData(angle, distance);

				// System.out.print("Angle: " + String.format("%.2f", angle) +
				// "Dist: " + String.format("%.1f", distance));
				break;
			default:
				break;
			}
		}
		// System.out.print("\n");
		// System.out.println("r: " + pheromoneScan.getPrintable("a"));
		// s.normalize();
		// System.out.println("n: " + s.getPrintable("a"));

		/* CHeck if bb in perception scope */
		NdPoint baseLocation = space.getLocation(bbScenario.blackboxAgent);
		if (space.getDistance(currentLocation, baseLocation) <= scenario.perceptionScope) {
			System.out.println("bb found");
			return true; /* Blackbox found */
		}
		return false;
	}

	public BB_PheromoneAvoider(Context<Agent> context,
			Context<Agent> rootContext) {
		super(context, rootContext);
		agentNo++;
	}

	public double getResultingAngle(List<Agent> agents) {
		double dx = 0, dy = 0;

		for (Agent agent : agents) {
			double displacement[] = space.getDisplacement(
					space.getLocation(this), space.getLocation(agent));
			double distance = space.getDistance(space.getLocation(this),
					space.getLocation(agent));

			if (distance > 0) {
				dx += (scenario.perceptionScope / distance) * displacement[0];
				dy += (scenario.perceptionScope / distance) * displacement[1];
			}
		}

		return SpatialMath.calcAngleFor2DMovement(space, new NdPoint(0, 0),
				new NdPoint(dx, dy));
	}

	@Override
	public String getName() {
		return "BB_PheromoneAvoider" + agentNo;
	}

	@Override
	public AgentType getAgentType() {
		return AgentType.BB_PheromoneAvoider;
	}

}
