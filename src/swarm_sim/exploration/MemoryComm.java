package swarm_sim.exploration;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.DisplayAgent;
import swarm_sim.SectorMap;
import swarm_sim.communication.Message;
import swarm_sim.communication.MsgBlackboxFound;
import swarm_sim.communication.MsgSectorValues;
import swarm_sim.communication.NetworkAgent;
import swarm_sim.exploration.DefaultExplorationAgent.agentState;

public class MemoryComm extends DefaultExplorationAgent implements Agent,
		DisplayAgent {

	SectorMap sectors = new SectorMap(space.getDimensions(), 40, 40, 1);

	public MemoryComm(Context<Agent> context) {
		super(context);
	}

	public void step() {
		defaultStepStart();
		processMessageQueue();
		move();
		scanEnv();
		prevState = state;
		sendMessages();
		defaultStepEnd();
	}

	private void move() {
		double speed = scenario.maxMoveDistance;

		if (state == agentState.exploring) {
			sectors.setPosition(currentLocation);
			directionAngle = sectors.getNewMoveAngle();

			currentLocation = space
					.moveByVector(this, speed, directionAngle, 0);
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

	private void scanEnv() {

	}

	private void processMessageQueue() {
		Message msg = popMessage();
		while (msg != null) {
			switch (msg.getType()) {
			case SectorMap:
				Object data[] = (Object[]) msg.getData();
				SectorMap s = (SectorMap) data[0];
				sectors.merge(s);
				SectorMap targetSector = (SectorMap) data[2];
				if (targetSector.equals(sectors.getTargetSector())) {
					NdPoint targetSectorCenter = s
							.getSectorCenter(targetSector);
					if (space.getDistance(currentLocation, targetSectorCenter) > space
							.getDistance((NdPoint) data[1], targetSectorCenter))
						sectors.chooseNewTargetSector();
				}
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
			else {
				Object data[] = new Object[3];
				data[0] = sectors;
				data[1] = currentLocation;
				data[2] = sectors.getTargetSector();
				netAgent.addToMessageQueue(new MsgSectorValues(this, agent,
						data));
			}

		}
	}

	@Override
	public String getName() {
		return "MemoryExplorer" + agentId;
	}

	@Override
	public AgentType getAgentType() {
		return AgentType.EXPL_MemoryComm;
	}
}
