package swarm_sim.foraging;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.IAgent;
import swarm_sim.IDisplayAgent;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.communication.Message.MessageType;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.CircleScan;

/**
 * Agent which explores the space randomly
 * 
 * @author achim
 * 
 */
public class Random extends DefaultForagingAgent implements IAgent, IDisplayAgent {

    int segmentCount = 8;
    
    ResourcesTarget currentTarget;

    CircleScan resourceScan = new CircleScan(segmentCount, 1, 1, 10, 1, 0, 1,
	    0, scenario.perceptionScope);
    CircleScan deliverDirection = new CircleScan(segmentCount, 1, 1, 10, 1, 1,
	    1, 0, scenario.perceptionScope);
    CircleScan agentFollow = new CircleScan(segmentCount, 1, 1, 10, 1, 1,
	    1, 0, scenario.commScope);

    public Random(Context<IAgent> context) {
	super(context);
    }

    public void step() {
	List<Resource> perceivedResources = new ArrayList<>();
	List<IAgent> acquiringAgentsInRange = new ArrayList<>();
	
	defaultStepStart();
	processMessageQueue(acquiringAgentsInRange);
	scanEnv(perceivedResources);
	determineState(perceivedResources, acquiringAgentsInRange);
	move();
	sendMessages();
	prevState = state;
	defaultStepEnd();
    }
    
    private void move() {
	AngleSegment moveCircle = new AngleSegment(-Math.PI, Math.PI);
	List<AngleSegment> moveCircleFree = moveCircle
		.filterSegment(collisionAngleFilter.getFilterSegments());

	switch (state) {
	case wander:
	    directionAngle = getWanderDirection(moveCircleFree);
	    break;
	case acquire:
	    directionAngle = getAcquireDirection(moveCircleFree);
	    break;
	case deliver:
	    directionAngle = getDeliverDirection(moveCircleFree);
	    break;
	default:
	    System.err.println("unkown state: " + state);
	    break;
	}

	if (directionAngle > -10) {
	    currentLocation = space.moveByVector(this,
		    scenario.maxMoveDistance, directionAngle, 0);
	}
    }

    private double getWanderDirection(List<AngleSegment> moveCircleFree) {
	if (consecutiveMoveCount < scenario.rndConsecutiveMoves) {
	    boolean moveAllowed = false;
	    for (AngleSegment as : moveCircleFree) {
		if (as.start <= directionAngle && as.end >= directionAngle) {
		    moveAllowed = true;
		    break;
		}
	    }

	    if (moveAllowed) {
		consecutiveMoveCount++;
		return directionAngle;
	    }
	}

	/* Choose random direction */
	consecutiveMoveCount = 0;
	CircleScan res = CircleScan.merge(segmentCount, 0, moveCircleFree);
	return res.getMovementAngle();
    }

    private double getAcquireDirection(List<AngleSegment> moveCircleFree) {
	if(currentTarget != null)
	    resourceScan.add(SpatialMath.calcAngleFor2DMovement(space, currentLocation, currentTarget.location));
	CircleScan res = CircleScan.merge(segmentCount, 0.12, moveCircleFree,
		resourceScan, agentFollow);
	return res.getMovementAngle();
    }

    private double getDeliverDirection(List<AngleSegment> moveCircleFree) {
	deliverDirection.clear();
	double moveAngleToBase = SpatialMath.calcAngleFor2DMovement(space,
		currentLocation, space.getLocation(scenario.baseAgent));
	deliverDirection.add(moveAngleToBase);
	CircleScan res = CircleScan.merge(segmentCount, 0.12, moveCircleFree,
		deliverDirection);
	return res.getMovementAngle();
    }

    private void determineState(List<Resource> perceivedResources, List<IAgent> acquiringAgentsInRange) {
	agentFollow.clear();
	
	switch (state) {
	case wander:
	    for (Resource r : perceivedResources) {
		state = AgentState.acquire;
		
		NdPoint resLoc = space.getLocation(r);
		double distance = space.getDistance(currentLocation, resLoc);
		if(distance <= scenario.maxMoveDistance/2) {
		    /* Pick it up*/
		    context.remove(r);
		    state = AgentState.deliver;
		    return;
		} else if(distance > 0)	{
		    resourceScan.add(SpatialMath.calcAngleFor2DMovement(space, currentLocation, resLoc), distance);
		}
	    }
	    
	    if(perceivedResources.size() == 0 && acquiringAgentsInRange.size() > 0) {
		state = AgentState.acquire;
		for (IAgent agent : acquiringAgentsInRange) {
		    NdPoint agentLoc = space.getLocation(agent);
		    double distance = space.getDistance(currentLocation, agentLoc);
		    double angle = SpatialMath.calcAngleFor2DMovement(space, currentLocation, agentLoc);
		    agentFollow.add(angle, distance);
		}
	    }
	    break;
	case acquire:
	    if(currentTarget != null && space.getDistance(currentLocation, currentTarget.location) < scenario.maxMoveDistance/2) {
		currentTarget = null;
	    }
	    
	    for (Resource r : perceivedResources) {
		NdPoint resLoc = space.getLocation(r);
		double distance = space.getDistance(currentLocation, resLoc);
		if(distance <= scenario.maxMoveDistance/2) {
		    /* Pick it up*/
		    context.remove(r);
		    state = AgentState.deliver;
		    return;
		} else if(distance > 0)	{
		    resourceScan.add(SpatialMath.calcAngleFor2DMovement(space, currentLocation, resLoc), distance);
		}
	    }
	    
	    if(perceivedResources.size() == 0) {
		if(currentTarget == null)
		    state = AgentState.wander;
	    }
	    break;
	case deliver:
	    if(perceivedResources.size() > 0)
		currentTarget = new ResourcesTarget(perceivedResources.size(), currentLocation);
	    
	    double distanceToBase = space.getDistance(currentLocation, space.getLocation(scenario.baseAgent));
	    if(distanceToBase <= scenario.maxMoveDistance/2) {
		/* Deliver the resource */
		scenario.deliveredResources++;
		
		if(currentTarget != null) {
		    state = AgentState.acquire;
		}
		else
		    state = AgentState.wander;
	    }
	    break;
	default:
	    System.err.println("unkown state: " + state);
	    break;
	}
    }

    private void scanEnv(List<Resource> perceivedResources) {
	resourceScan.clear();
	/* scan environment for surrounding agents, pheromones, resources, ... */
	ContinuousWithin<IAgent> withinQuery = new ContinuousWithin<IAgent>(
		space, this, scenario.perceptionScope);
	for (IAgent agent : withinQuery.query()) {
	    switch (agent.getAgentType()) {
	    case EXPL_Random:
		double distance = space.getDistance(space.getLocation(this),
			space.getLocation(agent));
		if (distance > 0 && distance <= scenario.maxMoveDistance + 1) {
		    double angle = SpatialMath.calcAngleFor2DMovement(space,
			    currentLocation, space.getLocation(agent));
		    collisionAngleFilter.add(distance, angle);
		}
		break;
	    case Resource:
		perceivedResources.add((Resource) agent);
		break;
	    default:
		break;
	    }
	}
    }
    
    private void processMessageQueue(List<IAgent> acquiringAgentsInRange) {
	Message msg = popMessage();
	
	
	while (msg != null) {
	    switch (msg.getType()) {
	    case CurrentState:
		AgentState s = (AgentState)msg.getData();
		if(s == AgentState.acquire)
		    acquiringAgentsInRange.add(msg.getSender());
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
	    if(prevState == state)
		netAgent.addToMessageQueue(new Message(MessageType.CurrentState, this, state));
	}
    }


    @Override
    public String getName() {
	return "RandomForager" + agentId;
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.FAGN_Random;
    }
}
