package swarm_sim.blackbox;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.DisplayAgent;
import swarm_sim.communication.Message;
import swarm_sim.communication.MsgBlackboxFound;
import swarm_sim.communication.NetworkAgent;

/**
 * Agent which explores the space randomly while searching for the blackbox.
 * Goes home to base directly when the blackbox was found to communicate the
 * location.
 * 
 * @author achim
 * 
 */
public class BB_RandomComm extends DefaultBlackboxAgent implements
		Agent, DisplayAgent {

	private static int agentNo = 1;

	public BB_RandomComm(Context<Agent> context,
			Context<Agent> rootContext) {
		super(context, rootContext);
		agentNo++;
	}

	public void step() {
		processMessageQueue();
		move();
		if (scanEnv()) {
			bbScenario.blackboxFound();
			state = agentState.blackbox_found;
		}
		if (state == agentState.blackbox_found) {
			/* tell others */
			for (Agent agent : commNet.getAdjacent(this)) {
				NetworkAgent netAgent = (NetworkAgent) agent;
				netAgent.addToMessageQueue(new MsgBlackboxFound(this, agent,
						null));
			}
		}
		prevState = state;
	}

	private void processMessageQueue() {
		Message msg = popMessage();
		while (msg != null) {
			switch (msg.getType()) {
			case Location:
				break;
			case Blackbox_found:
				/* This agent also knows where the blackbox is */
				this.state = agentState.blackbox_found;
			default:
				break;
			}
			msg = popMessage();
		}
	}

	private void move() {
		double speed = scenario.agentMovementSpeed;

		if (state == agentState.exploring) {
			/* Explore environment randomly */
			if(consecutiveMoveCount >= scenario.randomConsecutiveMoves) {
				directionAngle = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);
				currentLocation = space.moveByVector(this, speed, directionAngle, 0);
				consecutiveMoveCount = 1;
			} else {
				currentLocation = space.moveByVector(this, speed, directionAngle, 0);
				consecutiveMoveCount++;
			}
		} else if (state == agentState.blackbox_found) {
			/* Go back to base */
			NdPoint baseLocation = space
					.getLocation(scenario.baseAgent);

			double moveDistance = space.getDistance(currentLocation,
					baseLocation);
			if (moveDistance > speed) {
				moveDistance = speed;
			}
			double movementAngle = SpatialMath.calcAngleFor2DMovement(
					space, currentLocation, baseLocation);
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
		NdPoint baseLocation = space
				.getLocation(bbScenario.blackboxAgent);
		if (space.getDistance(currentLocation, baseLocation) <= scenario.perceptionScope) {
			System.out.println("bb found");
			return true; /* Blackbox found */
		}
		return false;
	}


	@Override
	public String getName() {
		return "RandomExplorerWithComm" + agentNo;
	}

	@Override
	public AgentType getAgentType() {
		return AgentType.BB_RandomComm;
	}

}
