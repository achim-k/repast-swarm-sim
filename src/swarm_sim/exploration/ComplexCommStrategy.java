package swarm_sim.exploration;

import java.util.ArrayList;
import java.util.List;

import org.jgap.Chromosome;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.Strategy;
import swarm_sim.Agent.AgentState;
import swarm_sim.IAgent.AgentType;
import swarm_sim.IAgent;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.communication.Message.MessageType;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.CircleScan;

public class ComplexCommStrategy extends ExplorationStrategy {

    int segmentCount = 8;

    double prevDirection = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);

    CircleScan agentRepell = new CircleScan(segmentCount, 2, 1, 10000, 1, 0,
	    -1, 0, 0.8 * config.commScope);
    CircleScan agentAppeal = new CircleScan(segmentCount, 1, 1, 100, 1, 1, 0,
	    0.8 * config.commScope, 1 * config.commScope);
    CircleScan continuousMove = new CircleScan(segmentCount, 1, 1, 100, 1, 1,
	    1, 1, 1 * config.commScope);
    CircleScan agentMimic = new CircleScan(segmentCount, 1, 1, 100, 1, 1, 0,
	    0.6 * config.commScope, 1 * config.commScope);

    public ComplexCommStrategy(Chromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
    }

    @Override
    protected List<MessageTypeRegisterPair> getMessageTypesToRegister(
	    CommunicationType[] allowedCommTypes) {
	List<MessageTypeRegisterPair> ret = new ArrayList<Strategy.MessageTypeRegisterPair>();
	for (CommunicationType commType : allowedCommTypes) {
	    switch (commType) {
	    case Location:
		AgentState states[] = new AgentState[] { AgentState.wander };
		ret.add(new MessageTypeRegisterPair(MessageType.Location,
			states));
		break;
	    case TargetOrDirection:
		AgentState states2[] = new AgentState[] { AgentState.wander };
		ret.add(new MessageTypeRegisterPair(MessageType.Direction,
			states2));
	    default:
		break;
	    }
	}
	return ret;
    }

    @Override
    protected AgentState processMessage(AgentState prevState,
	    AgentState currentState, Message msg, boolean isLast) {
	if (isLast)
	    return currentState;

	if (msg.getType() == MessageType.Location) {
	    NdPoint currentLoc = space.getLocation(controllingAgent);
	    NdPoint agentLoc = space.getLocation(msg.getSender());
	    double distance = space.getDistance(currentLoc, agentLoc);
	    double angle = SpatialMath.calcAngleFor2DMovement(space,
		    currentLoc, agentLoc);
	    agentRepell.add(angle, distance);
	    agentAppeal.add(angle, distance);
	}
	
	if(msg.getType() == MessageType.Direction) {
	    NdPoint currentLoc = space.getLocation(controllingAgent);
	    NdPoint agentLoc = space.getLocation(msg.getSender());
	    double distance = space.getDistance(currentLoc, agentLoc);
	    agentMimic.add((double)msg.getData(), distance);
	}

	return currentState;
    }

    @Override
    protected void sendMessage(AgentState prevState, AgentState currentState,
	    INetworkAgent agentInRange) {
	if (currentState == AgentState.wander) {
	    agentInRange.pushMessage(new Message(MessageType.Location,
		    controllingAgent, space.getLocation(controllingAgent)));
	    agentInRange.pushMessage(new Message(MessageType.Direction,
		    controllingAgent, prevDirection));
	}
    }

    @Override
    protected AgentState processPerceivedAgent(AgentState prevState,
	    AgentState currentState, IAgent agent, boolean isLast) {
	if (agent.getAgentType() == AgentType.Resource)
	    return AgentState.acquire;
	else if (agent.getAgentType() == controllingAgent.getAgentType()) {
	    NdPoint currentLoc = space.getLocation(controllingAgent);
	    NdPoint agentLoc = space.getLocation(agent);
	    double distance = space.getDistance(currentLoc, agentLoc);
	    double angle = SpatialMath.calcAngleFor2DMovement(space,
		    currentLoc, agentLoc);
	    agentRepell.add(angle, distance);
	}

	return AgentState.wander;
    }

    @Override
    protected double makeDirectionDecision(AgentState prevState,
	    AgentState currentState, List<AngleSegment> collisionFreeSegments) {

	continuousMove.add(prevDirection);

	CircleScan res = CircleScan.merge(segmentCount, 0.12,
		collisionFreeSegments, agentRepell, agentAppeal, agentMimic,
		continuousMove);

	prevDirection = res.getMovementAngle();
	return prevDirection;
    }

    @Override
    protected void clear() {
	agentRepell.clear();
	agentAppeal.clear();
	agentMimic.clear();
	continuousMove.clear();

    }

    @Override
    protected void reset() {
	prevDirection = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);
    }

}
