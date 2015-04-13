package swarm_sim.blackbox;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
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

/**
 * Agent which explores the space randomly while searching for the blackbox.
 * Doesn't care about other agents. Goes home to base directly when the blackbox
 * was found to communicate the location.
 * 
 * @author achim
 * 
 */
public class BB_RandomObstacleAvoider extends DefaultBlackboxAgent implements
		Agent, DisplayAgent {

	public BB_RandomObstacleAvoider(Context<Agent> context,
			Context<Agent> rootContext) {
		super(context, rootContext);
	}

	ScanCircle obstacles = new ScanCircle(8, 1, 1, AttractionType.Repelling,
			DistributionType.Linear, GrowingDirection.Inner, 0,
			scenario.perceptionScope, 2, 2);

	public void step() {
		defaultStepStart();

		move();
		
		
		if (scanEnv()) {
			bbScenario.blackboxFound();
//			state = agentState.blackbox_found;
		}
		prevState = state;
		defaultStepEnd();
	}

	private void move() {
		obstacles.clear();
		double speed = scenario.maxMoveDistance;

		/* check for obstacles */
		for (FieldDistancePair field : surroundingFields) {
			if (field.fieldType == FieldType.Obstacle) {
				double angle = SpatialMath.calcAngleFor2DMovement(space,
						currentLocation, new NdPoint(field.x, field.y));
				obstacles.add(obstacles.new InputPair(angle, field.distance));
			}
		}

		if (state == agentState.exploring) {
			/* Explore environment randomly */
			if (consecutiveMoveCount >= scenario.randomConsecutiveMoves) {
				ScanCircle resulting = ScanCircle.merge(8, 0.12, obstacles);
				directionAngle = resulting.getMovementAngle();
//				System.out.println(resulting.getPrintable(null));
				
				currentLocation = space.moveByVector(this, speed,
						directionAngle, 0);
				if(exploredArea.getFieldType(currentLocation.getX(), currentLocation.getY()) == FieldType.Obstacle) {
					System.out.println(resulting.getPrintable(null));
					System.err.println("bad!!!");
					
				}
				
				
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
		NdPoint baseLocation = space.getLocation(bbScenario.blackboxAgent);
		if (space.getDistance(currentLocation, baseLocation) <= scenario.perceptionScope) {
			System.out.println("bb found");
			return true; /* Blackbox found */
		}
		return false;
	}

	@Override
	public String getName() {
		return "BB_RandomObstacleAvoider" + agentId;
	}

	@Override
	public AgentType getAgentType() {
		return AgentType.BB_RandomObstacleAvoider;
	}
}
