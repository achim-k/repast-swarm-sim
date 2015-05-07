package swarm_sim.foraging;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.IAgent;
import swarm_sim.IAgent.AgentType;
import swarm_sim.SectorMap;
import swarm_sim.Strategy;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.communication.Message.MessageType;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.Scan;
import swarm_sim.perception.Scan.AttractionType;
import swarm_sim.perception.Scan.GrowingDirection;
import swarm_sim.perception.ScanMoveDecision;

public class GoalCommunication extends ForagingStrategy {

    int segmentCount = 8;
    double directionAngle = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);

    SectorMap map;

    ResourceTarget currentTarget;

    Scan scanResources = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, config.perceptionScope, 1,
	    100);
    Scan scanDeliverDirection = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 1E8, 1, 10);
    Scan scanCurrentTarget = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 1E8, 1, 10);

    ScanMoveDecision smd = new ScanMoveDecision(8, 6, 10, 0.05);

    public GoalCommunication(IChromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);

	int sectorsX = (int) (config.spaceWidth / config.perceptionScope);
	int sectorsY = (int) (config.spaceHeight / config.perceptionScope);

	if (sectorsX > config.spaceWidth)
	    sectorsX = config.spaceWidth;
	if (sectorsY > config.spaceHeight)
	    sectorsY = config.spaceHeight;

	map = new SectorMap(space.getDimensions(), sectorsX, sectorsY, 1);
    }

    @Override
    protected List<MessageTypeRegisterPair> getMessageTypesToRegister(
	    CommunicationType[] allowedCommTypes) {
	List<MessageTypeRegisterPair> ret = new ArrayList<Strategy.MessageTypeRegisterPair>();
	for (CommunicationType commType : allowedCommTypes) {
	    switch (commType) {
	    case TargetOrDirection:
		AgentState states[] = new AgentState[] { AgentState.wander };
		ret.add(new MessageTypeRegisterPair(
			MessageType.ResourceLocation, states));
		break;
	    default:
		break;
	    }
	}
	return ret;
    }

    @Override
    public AgentState checkState(AgentState prevState, AgentState currentState) {
	NdPoint currentLocation = space.getLocation(controllingAgent);
	if (currentState == AgentState.acquire) {
	    /* Unset currentTarget, when it has been reached */
	    if (currentTarget != null
		    && currentTarget.validity == true
		    && map.getCurrentSector(currentLocation).equals(
			    currentTarget.sector)) {
		currentTarget.validity = false;
	    }

	    /*
	     * If no resources have been perceived and no targets there and no
	     * agent to follow → wander
	     */
	    if (perceivedResourceCount > 0)
		return currentState;
	    if (currentTarget != null && currentTarget.validity == true)
		return currentState;

	    return AgentState.wander;
	}
	return currentState;
    }

    @Override
    protected AgentState processMessage(AgentState prevState,
	    AgentState currentState, Message msg, boolean isLast) {

	if (isLast)
	    return currentState;

	/* We are in state wander here */

	if (msg.getType() == MessageType.ResourceLocation) {
	    ResourceTarget resTarget = (ResourceTarget) msg.getData();
	    if (currentTarget == null
		    || (currentTarget.validity == false && !resTarget.sector
			    .equals(currentTarget.sector))) {
		currentTarget = resTarget;
		return AgentState.acquire;
	    }
	}

	// if (currentState == AgentState.acquire) {
	// NdPoint agentLoc = space.getLocation(msg.getSender());
	// NdPoint currentLocation = space.getLocation(controllingAgent);
	// double distance = space.getDistance(currentLocation, agentLoc);
	// double angle = SpatialMath.calcAngleFor2DMovement(space,
	// currentLocation, agentLoc);
	// agentRepulsion.add(angle, distance);
	// }
	return currentState;
    }

    @Override
    protected void sendMessage(AgentState prevState, AgentState currentState,
	    INetworkAgent agentInRange) {
	if (currentTarget != null
		&& currentTarget.validity == true
		&& (currentState == AgentState.acquire || currentState == AgentState.deliver)) {
	    agentInRange.pushMessage(new Message(MessageType.ResourceLocation,
		    controllingAgent, currentTarget));
	}
    }

    @Override
    protected AgentState processPerceivedAgent(AgentState prevState,
	    AgentState currentState, IAgent agent, boolean isLast) {
	NdPoint currentLocation = space.getLocation(controllingAgent);

	if (currentState == AgentState.acquire) {
	    if (agent.getAgentType() == AgentType.Resource) {
		perceivedResourceCount++;
		double distance = space.getDistance(currentLocation,
			space.getLocation(agent));

		if (distance <= config.maxMoveDistance / 2) {
		    /* pick up that resource */
		    context.remove(agent);
		    currentState = AgentState.deliver;
		    perceivedResourceCount--;
		    return currentState;
		}

		double angle = SpatialMath.calcAngleFor2DMovement(space,
			currentLocation, space.getLocation(agent));
		scanResources.addInput(angle, distance);
	    }
	}

	if (currentState == AgentState.deliver) {
	    if (agent.getAgentType() == AgentType.Resource) {
		perceivedResourceCount++;
		if (currentTarget == null)
		    currentTarget = new ResourceTarget(1, currentLocation,
			    map.getCurrentSector(currentLocation));
		else
		    currentTarget.resourceCount++;
	    } else if (agent.getAgentType() == AgentType.Base) {
		double distance = space.getDistance(currentLocation,
			space.getLocation(config.baseAgent));

		if (distance <= config.maxMoveDistance / 2) {
		    /* Deliver the resource */
		    data.deliveredResources++;

		    if (currentTarget != null && currentTarget.validity == true) {
			currentState = AgentState.acquire;
		    } else
			currentState = AgentState.wander;
		}
	    }
	}
	return currentState;
    }

    @Override
    protected double makeDirectionDecision(AgentState prevState,
	    AgentState currentState, List<AngleSegment> collisionFreeSegments) {

	smd.setValidSegments(collisionFreeSegments);

	if (currentState == AgentState.acquire) {
	    if (currentTarget != null) {
		SectorMap currentSector = map.getCurrentSector(space
			.getLocation(controllingAgent));
		double direction = currentSector
			.getDirectionToSector(currentTarget.sector);
		scanCurrentTarget.addInput(direction);
	    }
	    smd.calcProbDist(scanResources, scanCurrentTarget);

	} else if (currentState == AgentState.deliver) {
	    double moveAngleToBase = SpatialMath.calcAngleFor2DMovement(space,
		    space.getLocation(controllingAgent),
		    space.getLocation(config.baseAgent));
	    scanDeliverDirection.addInput(moveAngleToBase);
	    smd.calcProbDist(scanDeliverDirection);
	} else {
	    System.err.println("state not existing: " + currentState);
	}

	smd.normalize();
	directionAngle = smd.getMovementAngle();

	return directionAngle;
    }

    @Override
    public void reset() {
	super.reset();
	this.clear();
	if (currentTarget != null)
	    currentTarget.validity = false;
    }

    @Override
    public void clear() {
	super.clear();
	scanCurrentTarget.clear();
	scanDeliverDirection.clear();
	scanResources.clear();
	smd.clear();
    }

}
