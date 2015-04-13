package swarm_sim.blackbox;

import java.awt.Color;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.DisplayAgent;

/**
 * Agent which explores the space randomly while searching for the blackbox.
 * Doesn't care about other agents. Goes home to base directly when the blackbox
 * was found to communicate the location.
 * 
 * @author achim
 * 
 */
public class BB_RandomPoint extends DefaultBlackboxAgent implements
		Agent, DisplayAgent {

	private static int agentNo = 1;
	
	private boolean reachedTargetPoint = false;
	private NdPoint targetPoint = null;

	public BB_RandomPoint(Context<Agent> context,
			Context<Agent> rootContext) {
		super(context, rootContext);
		agentNo++;
		reachedTargetPoint = true;
	}

	public void step() {
		defaultStepStart();
		move();
		if (scanEnv()) {
			bbScenario.blackboxFound();
			state = agentState.blackbox_found;
		}
		prevState = state;
		defaultStepEnd();
	}

	private void move() {
		double speed = scenario.maxMoveDistance;

		if (state == agentState.exploring) {
			/* Explore environment randomly */
			if(reachedTargetPoint) {
				determineRandomPointAndDirection();
			}
			currentLocation = space.moveByVector(this, speed, directionAngle, 0);
			if(space.getDistance(currentLocation, targetPoint) < speed) {
				reachedTargetPoint = true;
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
	
	private void determineRandomPointAndDirection() {
		double randX = RandomHelper.nextDoubleFromTo(0, space.getDimensions().getWidth());
		double randY = RandomHelper.nextDoubleFromTo(0, space.getDimensions().getHeight());
		targetPoint = new NdPoint(randX, randY);
		directionAngle = SpatialMath.calcAngleFor2DMovement(space, space.getLocation(this), targetPoint);
		reachedTargetPoint = false;
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
		return "RandomPointExplorerNoComm" + agentNo;
	}

	@Override
	public AgentType getAgentType() {
		return AgentType.BB_RandomPoint;
	}
}
