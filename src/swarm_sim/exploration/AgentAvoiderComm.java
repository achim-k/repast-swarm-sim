package swarm_sim.exploration;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.DisplayAgent;
import swarm_sim.ScanCircle;
import swarm_sim.ScanCircle.AttractionType;
import swarm_sim.ScanCircle.DistributionType;
import swarm_sim.ScanCircle.GrowingDirection;
import swarm_sim.ScanCircle.InputPair;
import swarm_sim.communication.Message;
import swarm_sim.communication.MsgBlackboxFound;
import swarm_sim.communication.NetworkAgent;

public class AgentAvoiderComm extends DefaultExplorationAgent implements Agent,
	DisplayAgent {
    ScanCircle agentRepellingScan = new ScanCircle(8, 1, 1,
	    AttractionType.Repelling, DistributionType.Linear,
	    GrowingDirection.Inner, 0, scenario.commScope, 0.2, 1);

    public AgentAvoiderComm(Context<Agent> context) {
	super(context);
    }

    public void step() {
	defaultStepStart();
	processMessageQueue();
	move();
	scanEnv();
	prevState = state;
	defaultStepEnd();
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
	double speed = scenario.maxMoveDistance;

	if (state == agentState.exploring) {
	    /* Explore environment randomly */
	    double cameFromAngle = directionAngle + Math.PI;
	    cameFromAngle = cameFromAngle > Math.PI ? cameFromAngle - 2
		    * Math.PI : cameFromAngle;

	    agentRepellingScan.add(agentRepellingScan.new InputPair(
		    cameFromAngle, scenario.commScope / 2.0));
	    ScanCircle resulting = ScanCircle.merge(8, 0, agentRepellingScan);
	    directionAngle = resulting.getMovementAngle();

	    currentLocation = space
		    .moveByVector(this, speed, directionAngle, 0);

	    if (consecutiveMoveCount >= scenario.rndConsecutiveMoves) {
		consecutiveMoveCount = 1;
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
    }

    private void scanEnv() {

	/* Pheromone scan */
	agentRepellingScan.clear();

	for (Agent agent : commNet.getAdjacent(this)) {

	    switch (agent.getAgentType()) {
	    case EXPL_AgentAvoiderComm:
		double angle = SpatialMath.calcAngleFor2DMovement(space,
			currentLocation, space.getLocation(agent));
		double distance = space.getDistance(space.getLocation(this),
			space.getLocation(agent));

		agentRepellingScan.add(agentRepellingScan.new InputPair(angle,
			distance));
		break;
	    default:
		break;
	    }
	}
    }

    @Override
    public String getName() {
	return "BB_AgentAvoiderComm" + agentId;
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.EXPL_AgentAvoiderComm;
    }

}
