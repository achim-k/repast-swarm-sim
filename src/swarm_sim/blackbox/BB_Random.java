package swarm_sim.blackbox;

import java.awt.Color;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.DisplayAgent;
import swarm_sim.ScanCircle;
import swarm_sim.AdvancedGridValueLayer.FieldDistancePair;
import swarm_sim.AdvancedGridValueLayer.FieldType;
import swarm_sim.ScanCircle.AttractionType;
import swarm_sim.ScanCircle.DistributionType;
import swarm_sim.ScanCircle.GrowingDirection;
import swarm_sim.ScanCircle.InputPair;
import swarm_sim.perception.AngleFilter;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.CircleScan;

/**
 * Agent which explores the space randomly while searching for the blackbox.
 * Doesn't care about other agents. Goes home to base directly when the blackbox
 * was found to communicate the location.
 * 
 * @author achim
 * 
 */
public class BB_Random extends DefaultBlackboxAgent implements Agent,
		DisplayAgent {

	CircleScan perceivedAgents = new CircleScan(8, 1, 1, 100, 1, 1, 2, 0,
			scenario.perceptionScope);
	

	public BB_Random(Context<Agent> context, Context<Agent> rootContext) {
		super(context, rootContext);
	}

	public void step() {
		defaultStepStart();

		if (scanEnv()) {
			bbScenario.blackboxFound();
			// state = agentState.blackbox_found;
		}

		move();
		prevState = state;
		defaultStepEnd();
	}

	private void move() {
		double speed = scenario.maxMoveDistance;

		if (state == agentState.exploring) {
			/* Explore environment randomly */
			if (consecutiveMoveCount >= scenario.randomConsecutiveMoves) {

				List<AngleSegment> filterSegments = collisionAngleFilter
						.getFilterSegments();
				AngleSegment r = new AngleSegment(-Math.PI, Math.PI);

				List<AngleSegment> freeToGoSegments = r
						.filterSegment(filterSegments);

				double sum = 0;
				for (AngleSegment s : freeToGoSegments) {
					if (s.start > s.end)
						System.err.println("This is fatal!!!!");
					sum += s.end - s.start;
					// System.out.println(s.start + "\t" + s.end + "\t"
					// + (s.end - s.start));
				}

				double rndS = Math.random();
				double rndSum = 0;
				for (AngleSegment s : freeToGoSegments) {
					rndSum += (s.end - s.start) / sum;
					if (rndSum > rndS) {
						directionAngle = RandomHelper.nextDoubleFromTo(s.start,
								s.end);
						break;
					}
				}

				scenario.movebins[CircleScan.movementAngleToSegmentIndex(
						directionAngle, 8)]++;

				if (freeToGoSegments.size() > 0)
					currentLocation = space.moveByVector(this, speed,
							directionAngle, 0);

				consecutiveMoveCount = 1;
			} else {
				currentLocation = space.moveByVector(this, speed,
						directionAngle, 0);
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
	}

	private boolean scanEnv() {
		boolean bbFound = false;

		perceivedAgents.clear();


		/* scan environment for surrounding agents, pheromones, resources, ... */
		ContinuousWithin<Agent> withinQuery = new ContinuousWithin<Agent>(
				space, this, scenario.perceptionScope);
		for (Agent agent : withinQuery.query()) {
			switch (agent.getAgentType()) {
			case BB_Random:
				double distance = space.getDistance(space.getLocation(this),
						space.getLocation(agent));
				if (distance > 0 && distance <= scenario.maxMoveDistance + 1) {
					double angle = SpatialMath.calcAngleFor2DMovement(space,
							currentLocation, space.getLocation(agent));
					perceivedAgents.add(angle, distance);
					collisionAngleFilter.add(distance, angle);
				}

				break;
			case Blackbox:
				bbFound = true;
				break;
			default:
				break;
			}
		}

		return bbFound;
	}

	@Override
	public String getName() {
		return "RandomExplorerNoComm" + agentId;
	}

	@Override
	public AgentType getAgentType() {
		return AgentType.BB_Random;
	}
}
