package swarm_sim.foraging;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.IAgent;
import swarm_sim.Strategy;
import swarm_sim.Agent.AgentState;
import swarm_sim.IAgent.AgentType;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.communication.Message.MessageType;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.Scan;
import swarm_sim.perception.Scan.AttractionType;
import swarm_sim.perception.Scan.GrowingDirection;
import swarm_sim.perception.ScanMoveDecision;

public class StateCommStrategy extends ForagingStrategy {

    int segmentCount = 8;
    double directionAngle = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);

    ResourceTarget currentTarget;

    Scan scanResources = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, config.perceptionScope, 1,
	    100);
    Scan scanDeliverDirection = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 1E8, 1, 10);
    Scan scanCurrentTarget = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 1E8, 1, 10);
    Scan scanAgentFollow = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, config.commScope, 1, 1);
    Scan scanAgentRepell = new Scan(AttractionType.Repelling,
	    GrowingDirection.Inwards, 1.5, true, 0, 0.6 * config.commScope, 1,
	    10000);
    
    int perceivedAgentCount = 0;

    ScanMoveDecision smd = new ScanMoveDecision(8, 6, 10, 0.05);

    public StateCommStrategy(IChromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
    }

    @Override
    protected AgentState processMessage(AgentState prevState,
	    AgentState currentState, Message msg, boolean isLast) {

	if (isLast) {
	    /* */
	    if (currentState == AgentState.wander && scanAgentFollow.isValid()) {
		return AgentState.acquire;
	    }
	    return currentState;
	}

	if (msg.getType() == MessageType.CurrentState) {
	    AgentState netAgentState = (AgentState) msg.getData();

	    if (netAgentState == AgentState.acquire) {

		if (currentState == AgentState.wander
			|| (currentState == AgentState.acquire && currentTarget == null)) {
		    NdPoint agentLoc = space.getLocation(msg.getSender());
		    NdPoint currentLocation = space
			    .getLocation(controllingAgent);
		    double distance = space.getDistance(currentLocation,
			    agentLoc);
		    double angle = SpatialMath.calcAngleFor2DMovement(space,
			    currentLocation, agentLoc);
		    scanAgentFollow.addInput(angle, distance);

		}

	    }
	}

	if (currentState == AgentState.acquire) {
	    NdPoint agentLoc = space.getLocation(msg.getSender());
	    NdPoint currentLocation = space.getLocation(controllingAgent);
	    double distance = space.getDistance(currentLocation, agentLoc);
	    double angle = SpatialMath.calcAngleFor2DMovement(space,
		    currentLocation, agentLoc);
	    scanAgentRepell.addInput(angle, distance);
	    perceivedAgentCount++;
	}
	return currentState;
    }

    @Override
    protected void sendMessage(AgentState prevState, AgentState currentState,
	    INetworkAgent agentInRange) {
	// return;
	if (currentState == AgentState.acquire && currentTarget != null)
	    agentInRange.pushMessage(new Message(MessageType.CurrentState,
		    controllingAgent, currentState));
	// else if (currentState == AgentState.wander
	// || currentState == AgentState.deliver)
	// ;
	// agentInRange.pushMessage(new Message(MessageType.CurrentState,
	// controllingAgent, currentState));
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
	    } else if(agent.getAgentType() == controllingAgent.getAgentType()) {
		perceivedAgentCount++;
		double distance = space.getDistance(currentLocation,
			space.getLocation(agent));
		double angle = SpatialMath.calcAngleFor2DMovement(space,
			currentLocation, space.getLocation(agent));
		scanAgentRepell.addInput(angle, distance);
	    }
	}

	if (currentState == AgentState.deliver) {
	    if (agent.getAgentType() == AgentType.Resource) {
		perceivedResourceCount++;
		if (currentTarget == null)
		    currentTarget = new ResourceTarget(1, currentLocation, null);
		else
		    currentTarget.resourceCount++;
	    } else if (agent.getAgentType() == AgentType.Base) {
		double distance = space.getDistance(currentLocation,
			space.getLocation(config.baseAgent));

		if (distance <= config.maxMoveDistance / 2) {
		    /* Deliver the resource */
		    data.deliveredResources++;

		    if (currentTarget != null) {
			currentState = AgentState.acquire;
		    } else
			currentState = AgentState.wander;
		}
	    }
	}
	return currentState;
    }

    @Override
    public AgentState checkState(AgentState prevState, AgentState currentState) {
	if (currentState == AgentState.acquire) {
	    NdPoint currentLocation = space.getLocation(controllingAgent);

	    /* Unset currentTarget, when it has been reached */
	    if (currentTarget != null
		    && space.getDistance(currentLocation,
			    currentTarget.location) <= config.maxMoveDistance / 2) {
		currentTarget = null;
	    }

	    /*
	     * If no resources have been perceived and no targets there and no
	     * agent to follow → wander
	     */
	    if (perceivedResourceCount == 0 && currentTarget == null) {
		if(scanAgentFollow.isValid()) {
//		    double wanderProbability = 1/(perceivedAgentCount + 1);
//		    if(Math.random() >= wanderProbability)
//			currentState = AgentState.wander;
		} else
		    currentState = AgentState.wander;
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
		scanCurrentTarget.addInput(SpatialMath.calcAngleFor2DMovement(
			space, space.getLocation(controllingAgent),
			currentTarget.location));
	    }
	    smd.calcProbDist(scanResources, scanCurrentTarget, scanAgentFollow, scanAgentRepell);

	} else if (currentState == AgentState.deliver) {
	    double moveAngleToBase = SpatialMath.calcAngleFor2DMovement(space,
		    space.getLocation(controllingAgent),
		    space.getLocation(config.baseAgent));
	    scanDeliverDirection.addInput(moveAngleToBase);
	    smd.calcProbDist(scanDeliverDirection, scanAgentRepell);
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
	currentTarget = null;
    }

    @Override
    public void clear() {
	super.clear();
	scanAgentFollow.clear();
	scanResources.clear();
	scanCurrentTarget.clear();
	scanDeliverDirection.clear();
	scanAgentRepell.clear();
	smd.clear();
	perceivedAgentCount = 0;
    }

    @Override
    protected List<MessageTypeRegisterPair> getMessageTypesToRegister(
	    CommunicationType allowedCommTypes[]) {
	List<MessageTypeRegisterPair> ret = new ArrayList<Strategy.MessageTypeRegisterPair>();
	for (CommunicationType commType : allowedCommTypes) {
	    switch (commType) {
	    case State:
		AgentState states[] = new AgentState[] { AgentState.wander,
			AgentState.acquire };
		ret.add(new MessageTypeRegisterPair(MessageType.CurrentState,
			states));
		break;
	    default:
		break;
	    }
	}
	return ret;
    }
}
