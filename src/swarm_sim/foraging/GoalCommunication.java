package swarm_sim.foraging;

import java.util.ArrayList;
import java.util.List;

import org.jgap.Chromosome;
import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.SectorMap;
import swarm_sim.Strategy;
import swarm_sim.Agent.AgentState;
import swarm_sim.IAgent.AgentType;
import swarm_sim.IAgent;
import swarm_sim.Strategy.MessageTypeRegisterPair;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.communication.Message.MessageType;
import swarm_sim.foraging.ForagingStrategy.ResourceTarget;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.CircleScan;

public class GoalCommunication extends ForagingStrategy {

    int segmentCount = 8;
    double directionAngle = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);

    SectorMap map = new SectorMap(space.getDimensions(), 100, 100, 1);

    ResourceTarget currentTarget;

    CircleScan resourceScan = new CircleScan(segmentCount, 1, 1, 100, 1, 0, 1,
	    0, config.perceptionScope);
    CircleScan deliverDirection = new CircleScan(segmentCount, 1, 1, 100, 1, 1,
	    1, 0, config.perceptionScope);
    CircleScan agentRepulsion = new CircleScan(segmentCount, 2, 4, 100, 1, -1,
	    -2, 0, 0.5 * config.commScope);

    public GoalCommunication(IChromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);

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
	    if(currentTarget == null || (currentTarget.validity == false && !resTarget.sector.equals(currentTarget.sector))) {
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
	if (currentTarget != null && currentTarget.validity == true
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
		resourceScan.add(angle, distance);
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

	if (currentState == AgentState.acquire) {
	    if (currentTarget != null) {
		SectorMap currentSector = map.getCurrentSector(space
			.getLocation(controllingAgent));
		double direction = currentSector
			.getDirectionToSector(currentTarget.sector);
		resourceScan.add(direction);
	    }

	    CircleScan res = CircleScan.merge(segmentCount, 0.12,
		    collisionFreeSegments, resourceScan, agentRepulsion);
	    directionAngle = res.getMovementAngle();
	    return directionAngle;
	} else if (currentState == AgentState.deliver) {
	    deliverDirection.clear();
	    double moveAngleToBase = SpatialMath.calcAngleFor2DMovement(space,
		    space.getLocation(controllingAgent),
		    space.getLocation(config.baseAgent));
	    deliverDirection.add(moveAngleToBase);
	    CircleScan resDel = CircleScan.merge(segmentCount, 0.12,
		    collisionFreeSegments, deliverDirection);
	    directionAngle = resDel.getMovementAngle();
	    return directionAngle;
	} else {
	    System.err.println("ERROR: State → " + currentState);
	}

	return -100;
    }

    @Override
    public void reset() {
	super.reset();
	this.clear();
	currentTarget = null;
    }

    @Override
    public void clear() {
	super.clear();
	agentRepulsion.clear();
	resourceScan.clear();
	deliverDirection.clear();
	// agentFollow.clear();
    }

}
