package swarm_sim.blackbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.DisplayAgent;
import swarm_sim.Quadrant;
import swarm_sim.QuadrantMap;
import swarm_sim.blackbox.DefaultBlackboxAgent.agentState;
import swarm_sim.communication.Message;
import swarm_sim.communication.MsgBlackboxFound;
import swarm_sim.communication.MsgCurrentDirection;
import swarm_sim.communication.MsgQuadrantValues;
import swarm_sim.communication.NetworkAgent;

public class BB_MemoryComm extends DefaultBlackboxAgent implements Agent,
		DisplayAgent {

	QuadrantMap map = new QuadrantMap(space.getDimensions(), 15, 15, 200);
	QuadrantMap currentQuadrantMap = null;
	
	Quadrant targetQuadrant = null;

	int lineX = -1;
	int lineY = -1;
	int direction = 1;
	int verticalSteps = 0;

	public BB_MemoryComm(Context<Agent> context, Context<Agent> rootContext) {
		super(context, rootContext);
	}

	public void step() {
		defaultStepStart();
		processMessageQueue();

		if (targetQuadrant == null) {
			targetQuadrant = map.getRandomQuadrant();
		}

		move();
		if (scanEnv()) {
			bbScenario.blackboxFound();
			 state = agentState.blackbox_found;
		}
		prevState = state;
		sendMessages();
		defaultStepEnd();
	}

	private void move() {
		double speed = scenario.maxMoveDistance;

		if (state == agentState.exploring) {
			// NdPoint quadrantLowerLeft = targetQuadrant.getLowerLeftCorner(0);
			// NdPoint quadrantLowerRight = targetQuadrant
			// .getLowerRightCorner(scenario.perceptionScope);
			//
			// double x = targetQuadrant.getLocationX(currentLocation);
			// double y = targetQuadrant.getLocationY(currentLocation);
			//
			// if (x < 0.1 && x > -0.1 && y < 0.1 && y > -0.1) {
			// lineX = 0;
			// lineY = 0;
			// directionAngle = SpatialMath.calcAngleFor2DMovement(space,
			// currentLocation, quadrantLowerRight);
			// }
			// if(lineX >= 0) {
			// if(x >= 1 && verticalSteps < 3) {
			// direction *= -1;
			// directionAngle = Math.PI/2;
			// verticalSteps++;
			// } else {
			// directionAngle = direction < 0 ? 0 : Math.PI;
			// verticalSteps = 0;
			// }
			// }
			// else
			// directionAngle = SpatialMath.calcAngleFor2DMovement(space,
			// currentLocation, quadrantLowerLeft);
			//
			// currentLocation = space
			// .moveByVector(this, speed, directionAngle, 0);

			Quadrant currentQuadrant = map.locationToQuadrant(currentLocation);
			if (!currentQuadrant.equals(targetQuadrant)) {
				directionAngle = SpatialMath.calcAngleFor2DMovement(space,
						currentLocation, targetQuadrant.getCenter());
			} else if (map.getData(targetQuadrant) / map.getBinArea() < 2) {
				directionAngle = RandomHelper.nextDoubleFromTo(-Math.PI,
						Math.PI);
			} else {
				/* choose new quadrant */
//				RunEnvironment.getInstance().pauseRun();
				boolean newQuadrant = false;
				int degree = 1;
				System.out.println(targetQuadrant);

				List<Quadrant> neighborQuadrants = new ArrayList<>();
				do {
					neighborQuadrants = map.getNeighboringQuadrants(
							currentQuadrant, degree++);
					Collections.shuffle(neighborQuadrants);

					for (Quadrant q : neighborQuadrants) {
						if (map.getData(q) / map.getBinArea() < 1) {
							targetQuadrant = q;
							
							newQuadrant = true;
							System.out.println(q + "\t → " + (degree-1));
							System.out.println("---");
							break;
						}
					}
				} while (newQuadrant == false && neighborQuadrants.size() > 0);

				if (!newQuadrant) {
					targetQuadrant = map.getRandomQuadrant();
					targetQuadrant.data = 0;
					targetQuadrant.doUpdate = false;
				}

				directionAngle = SpatialMath.calcAngleFor2DMovement(space,
						currentLocation, targetQuadrant.getCenter());
			}

			currentLocation = space
					.moveByVector(this, speed, directionAngle, 0);
			map.incrementData(currentLocation, 2 * Math.PI
					* scenario.perceptionScope * 0.1);

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

	private void processMessageQueue() {
		Message msg = popMessage();
		while (msg != null) {
			switch (msg.getType()) {
			case QuadrantMap:
				map.merge((QuadrantMap) msg.getData());
				break;
			case Current_Direction:
				break;
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

	private void sendMessages() {
		for (Agent agent : commNet.getAdjacent(this)) {
			NetworkAgent netAgent = (NetworkAgent) agent;

			if (state == agentState.blackbox_found)
				netAgent.addToMessageQueue(new MsgBlackboxFound(this, agent,
						null));
			else
				netAgent.addToMessageQueue(new MsgQuadrantValues(this, agent,
						map));
		}
	}

	@Override
	public String getName() {
		return "MemoryExplorer" + agentId;
	}

	@Override
	public AgentType getAgentType() {
		return AgentType.BB_MemoryComm;
	}
}
