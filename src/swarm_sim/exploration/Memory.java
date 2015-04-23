package swarm_sim.exploration;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.IAgent;
import swarm_sim.IDisplayAgent;
import swarm_sim.SectorMap;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.communication.Message.MessageType;

public class Memory extends DefaultExplorationAgent implements IAgent,
	IDisplayAgent {

    SectorMap map = new SectorMap(space.getDimensions(), 60, 60, 1);

    public Memory(Context<IAgent> context) {
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
	    map.setPosition(currentLocation);
	    directionAngle = map.getNewMoveAngle();

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
		map.merge(s);
		SectorMap targetSector = (SectorMap) data[2];
		if (targetSector.equals(map.getTargetSector())) {
		    NdPoint targetSectorCenter = s
			    .getSectorCenter(targetSector);
		    if (space.getDistance(currentLocation, targetSectorCenter) > space
			    .getDistance((NdPoint) data[1], targetSectorCenter))
			map.chooseNewTargetSector();
		}
		break;
	    case Current_Direction:
		break;
	    case Location:
		break;
	    default:
		break;
	    }
	    msg = popMessage();
	}
    }

    private void sendMessages() {
	for (IAgent agent : commNet.getAdjacent(this)) {
	    INetworkAgent netAgent = (INetworkAgent) agent;

	    Object data[] = new Object[3];
	    data[0] = map;
	    data[1] = currentLocation;
	    data[2] = map.getTargetSector();
	    netAgent.addToMessageQueue(new Message(MessageType.SectorMap, this, data));

	}
    }

    @Override
    public String getName() {
	return "MemoryExplorer" + agentId;
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.EXPL_Memory;
    }
}
