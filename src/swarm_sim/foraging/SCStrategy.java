package swarm_sim.foraging;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.AbstractAgent;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.Strategy;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.communication.Message.MessageType;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.PDDPInput;
import swarm_sim.perception.PDDPInput.AttractionType;
import swarm_sim.perception.PDDPInput.GrowingDirection;
import swarm_sim.perception.PDDP;

public class SCStrategy extends ForagingStrategy {

    PDDPInput scanAgentFollow = new PDDPInput(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 5, config.commScope, 1, 500);

    int perceivedAgentCount = 0;

    public SCStrategy(IChromosome chrom, Context<AbstractAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
    }

    @Override
    protected AgentState processMessage(AgentState prevState,
	    AgentState currentState, Message msg, boolean isLast) {

	if (isLast) {
	    if (currentState == AgentState.wander && scanAgentFollow.isValid()) {
		return AgentState.acquire;
	    }
	    return currentState;
	}

	if (msg.getType() == MessageType.CurrentState) {
	    AgentState netAgentState = (AgentState) msg.getData();

	    if (netAgentState == AgentState.acquire) {
		NdPoint agentLoc = space.getLocation(msg.getSender());
		NdPoint currentLocation = space.getLocation(controllingAgent);
		double distance = space.getDistance(currentLocation, agentLoc);
		double angle = SpatialMath.calcAngleFor2DMovement(space,
			currentLocation, agentLoc);
		scanAgentFollow.addInput(angle, distance);
	    }
	}

	return currentState;
    }

    @Override
    protected void sendMessage(AgentState prevState, AgentState currentState,
	    INetworkAgent agentInRange) {
	if (currentTarget != null && currentTarget.isValid) {
	    // if (config.printConfig) {
	    // // if(currentState == AgentState.acquire) {
	    // agentInRange.pushMessage(new Message(MessageType.CurrentState,
	    // controllingAgent, currentState));
	    // } else
	    // if (perceivedAgentCount < 2) {
	    agentInRange.pushMessage(new Message(MessageType.CurrentState,
		    controllingAgent, currentState));
	    // }
	}
    }

    @Override
    public AgentState processPerceivedAgent(AgentState prevState,
	    AgentState currentState, AbstractAgent agent, boolean isLast) {
	perceivedAgentCount++;

	return super.processPerceivedAgent(prevState, currentState, agent,
		isLast);
    }

    @Override
    public AgentState checkState(AgentState prevState, AgentState currentState) {
	currentState = super.checkState(prevState, currentState);

	if (currentState == AgentState.wander && scanAgentFollow.isValid()) {
	    return AgentState.acquire;
	}

	return currentState;
    }

    @Override
    public double makeDirectionDecision(AgentState prevState,
	    AgentState currentState, PDDP pddp) {

	if (currentState == AgentState.acquire) {
	    if (scanResources.isValid()
		    || (currentTarget != null && currentTarget.isValid)) {
		return super.makeDirectionDecision(prevState, currentState,
			pddp);
	    }

	    pddp.calcProbDist(scanAgentFollow);
	    pddp.normalize();

	    if (config.takeHighestProb)
		directionAngle = pddp.getMovementAngleWithHighestProbability();
	    else
		directionAngle = pddp.getMovementAngle();

	    return directionAngle;
	} else {
	    return super.makeDirectionDecision(prevState, currentState,
		    pddp);
	}
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
