package swarm_sim.blackbox;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;

/**
 * Agent which explores the space randomly while searching for the blackbox.
 * Doesn't care about other agents. Goes home to base directly when the blackbox
 * was found to communicate the location.
 * 
 * @author achim
 * 
 */
public class BB_RandomExplorerNoComm extends DefaultBlackboxAgent implements Agent {

	private static int agentNo = 1;

	public BB_RandomExplorerNoComm(Context<Agent> context, Context<Agent> rootContext) {
		super(context, rootContext);
		agentNo++;
	}

	public void step() {
		
		move();
		if (scanEnv()) {
			if(prevState != agentState.blackbox_found)
				RunEnvironment.getInstance().pauseRun();
			
			state = agentState.blackbox_found; 
		}
		prevState = state;
	}

	private void move() {
		double speed = scenario.agentMovementSpeed;

		if (state == agentState.exploring) {
			/* Explore environment randomly */
			double moveX = RandomHelper.nextDoubleFromTo(-speed, speed);
			double moveY = RandomHelper.nextDoubleFromTo(-speed, speed);
			currentLocation = spaceContinuous.moveByDisplacement(this, moveX,
					moveY);
		} else if (state == agentState.blackbox_found) {
			/* Go back to base */
			NdPoint baseLocation = spaceContinuous
					.getLocation(scenario.baseAgent);
			
			double moveDistance = spaceContinuous.getDistance(currentLocation, baseLocation);
			if(moveDistance > speed) {
				moveDistance = speed;
			}
			double movementAngle = SpatialMath.calcAngleFor2DMovement(
					spaceContinuous, currentLocation, baseLocation);
			currentLocation = spaceContinuous.moveByVector(this, moveDistance, movementAngle, 0);
			
			if(moveDistance <= 0.2) {
				System.out.println("Agent which found BB has arrived at Base, end of Simulation");
				RunEnvironment.getInstance().endRun();
			}
		}
		updateExploredLayer();
	}

	private boolean scanEnv() {
		NdPoint baseLocation = spaceContinuous
				.getLocation(scenario.blackboxAgent);
		if (spaceContinuous.getDistance(currentLocation, baseLocation) <= scenario.perceptionScope) {
			scenario.blackboxFound = true;
			System.out.println("bb found");
			return true; /* Blackbox found */
		}
		return false;
	}

	@Override
	public String getName() {
		return "RandomExplorer" + agentNo;
	}

	@Override
	public AgentType getAgentType() {
		return AgentType.BB_RandomExplorerNoComm;
	}

}
