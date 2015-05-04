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
import swarm_sim.perception.Scan;
import swarm_sim.perception.Scan.AttractionType;
import swarm_sim.perception.Scan.GrowingDirection;
import swarm_sim.perception.ScanMoveDecision;

public class ComplexCommStrategy extends ExplorationStrategy {

    int segmentCount = 8;

    double prevDirection = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);

    Scan scanAgentRepell = new Scan(AttractionType.Repelling,
	    GrowingDirection.Inwards, 2, false, 0, 0.7 * config.commScope, 1,
	    1000);
    Scan scanAgentAppeal = new Scan(AttractionType.Attracting,
	    GrowingDirection.Outwards, 0.2, false, 0.8 * config.commScope,
	    config.commScope, 1, 1);
    Scan scanAgentMimic = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0,
	    config.commScope, 1, 1000);
    Scan scanPrevDirection = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0,
	    1000, 1, 1000);
    
    ScanMoveDecision smd = new ScanMoveDecision(8, 6, 10, 0.125);

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
	    scanAgentAppeal.addInput(angle, distance);

	    double delta[] = space.getDisplacement(agentLoc, currentLoc);
	    double p[] = new double[] { currentLoc.getX() + delta[0],
		    currentLoc.getY() + delta[1] };

	    if (p[0] <= config.spaceWidth && p[0] >= 0 && p[1] >= 0
		    && p[1] <= config.spaceHeight)
		scanAgentRepell.addInput(angle, distance);

	}

	if (msg.getType() == MessageType.Direction) {
	    NdPoint currentLoc = space.getLocation(controllingAgent);
	    NdPoint agentLoc = space.getLocation(msg.getSender());
	    double distance = space.getDistance(currentLoc, agentLoc);
	    scanAgentMimic.addInput((double) msg.getData(), distance);
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
	    scanAgentRepell.addInput(angle, distance);
	}

	return AgentState.wander;
    }

    @Override
    protected double makeDirectionDecision(AgentState prevState,
	    AgentState currentState, List<AngleSegment> collisionFreeSegments) {
	
	scanPrevDirection.addInput(prevDirection);

	smd.setValidSegments(collisionFreeSegments);
	smd.calcProbDist(scanAgentAppeal, scanAgentRepell, scanAgentMimic, scanPrevDirection);
	smd.normalize();

	prevDirection = smd.getMovementAngle();
	
	return prevDirection;
    }

    @Override
    protected void clear() {
	scanAgentAppeal.clear();
	scanAgentRepell.clear();
	scanAgentMimic.clear();
	scanPrevDirection.clear();
	smd.clear();
    }

    @Override
    protected void reset() {
	prevDirection = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);
    }

}
