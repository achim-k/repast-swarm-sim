package swarm_sim.foraging;

import java.util.List;

import org.jgap.Chromosome;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.ForagingStrategy;
import swarm_sim.IAgent;
import swarm_sim.IAgent.AgentType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.communication.Message.MessageType;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.CircleScan;

public class StateCommFAStrategy extends ForagingStrategy {

    int segmentCount = 8;
    double directionAngle = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);

    ResourcesTarget currentTarget;

    CircleScan resourceScan = new CircleScan(segmentCount, 1, 1, 10, 1, 0, 1,
	    0, scenario.perceptionScope);
    CircleScan deliverDirection = new CircleScan(segmentCount, 1, 1, 10, 1, 1,
	    1, 0, scenario.perceptionScope);
    CircleScan agentFollow = new CircleScan(segmentCount, 1, 1, 10, 1, 1, 1, 0,
	    scenario.commScope);

    public StateCommFAStrategy(Chromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
    }

    @Override
    protected AgentState processMessage(Message msg, AgentState currentState) {

	if (msg.getType() == MessageType.CurrentState) {
	    AgentState netAgentState = (AgentState) msg.getData();

	    if (currentState == AgentState.wander
		    && netAgentState == AgentState.acquire) {
		NdPoint agentLoc = space.getLocation(msg.getSender());
		NdPoint currentLocation = space.getLocation(controllingAgent);
		double distance = space.getDistance(currentLocation, agentLoc);
		double angle = SpatialMath.calcAngleFor2DMovement(space,
			currentLocation, agentLoc);
		agentFollow.add(angle, distance);
	    }
	}

	return currentState;
    }

    @Override
    protected void sendMessage(INetworkAgent agentInRange,
	    AgentState currentState) {
	if (currentState == AgentState.acquire)
	    agentInRange.pushMessage(new Message(MessageType.CurrentState,
		    controllingAgent, state));
    }

    @Override
    protected AgentState processPerceivedAgent(IAgent agent, boolean isLast) {
	NdPoint currentLocation = space.getLocation(controllingAgent);

	if (state == AgentState.acquire) {
	    if (agent.getAgentType() == AgentType.Resource) {
		perceivedResourceCount++;
		double distance = space.getDistance(currentLocation,
			space.getLocation(agent));

		if (distance <= scenario.maxMoveDistance / 2) {
		    /* pick up that resource */
		    context.remove(agent);
		    state = AgentState.deliver;
		    perceivedResourceCount--;
		    return state;
		}

		double angle = SpatialMath.calcAngleFor2DMovement(space,
			currentLocation, space.getLocation(agent));
		resourceScan.add(angle, distance);
	    }
	}

	if (state == AgentState.deliver) {
	    if (agent.getAgentType() == AgentType.Resource) {
		perceivedResourceCount++;
		if (currentTarget == null)
		    currentTarget = new ResourcesTarget(1, currentLocation);
		else
		    currentTarget.resourceCount++;
	    } else if (agent.getAgentType() == AgentType.Base) {
		double distance = space.getDistance(currentLocation,
			space.getLocation(scenario.baseAgent));

		if (distance <= scenario.maxMoveDistance / 2) {
		    /* Deliver the resource */
		    scenario.deliveredResources++;

		    if (currentTarget != null) {
			state = AgentState.acquire;
		    } else
			state = AgentState.wander;
		}
	    }
	}
	return state;
    }

    @Override
    protected AgentState checkState() {
	if (state == AgentState.acquire) {
	    NdPoint currentLocation = space.getLocation(controllingAgent);

	    /* Unset currentTarget, when it has been reached */
	    if (currentTarget != null
		    && space.getDistance(currentLocation,
			    currentTarget.location) <= scenario.maxMoveDistance / 2) {
		currentTarget = null;
	    }

	    /*
	     * If no resources have been perceived and no targets there → wander
	     */
	    if (perceivedResourceCount == 0 && currentTarget == null) {
		state = AgentState.wander;
	    }
	}
	return state;
    }

    @Override
    protected double makeDirectionDecision(
	    List<AngleSegment> collisionFreeSegments) {

	if (state == AgentState.acquire) {
	    if (currentTarget != null)
		resourceScan.add(SpatialMath.calcAngleFor2DMovement(space,
			space.getLocation(controllingAgent),
			currentTarget.location));
	    CircleScan res = CircleScan.merge(segmentCount, 0.12,
		    collisionFreeSegments, resourceScan, agentFollow);
	    directionAngle = res.getMovementAngle();
	    return directionAngle;
	} else if (state == AgentState.deliver) {
	    deliverDirection.clear();
	    double moveAngleToBase = SpatialMath.calcAngleFor2DMovement(space,
		    space.getLocation(controllingAgent),
		    space.getLocation(scenario.baseAgent));
	    deliverDirection.add(moveAngleToBase);
	    CircleScan resDel = CircleScan.merge(segmentCount, 0.12,
		    collisionFreeSegments, deliverDirection);
	    directionAngle = resDel.getMovementAngle();
	    return directionAngle;
	} else {
	    System.err.println("ERROR: State → " + state);
	}

	return -100;
    }

    @Override
    protected void reset() {
	super.reset();
	this.clear();
	currentTarget = null;
    }

    @Override
    protected void clear() {
	super.clear();
	resourceScan.clear();
	deliverDirection.clear();
	agentFollow.clear();
    }
}
