package swarm_sim.foraging;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
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

public class GoalCommunication extends ForagingStrategy {

    public GoalCommunication(IChromosome chrom, Context<AbstractAgent> context,
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
	return super.checkState(prevState, currentState);
    }

    @Override
    protected AgentState processMessage(AgentState prevState,
	    AgentState currentState, Message msg, boolean isLast) {

	if (isLast)
	    return currentState;

	/* We are in state wander here */

	if (msg.getType() == MessageType.ResourceLocation) {
	    ResourceTarget resTarget = (ResourceTarget) msg.getData();

	    if (currentTarget == null) {
		currentTarget = resTarget;
		return AgentState.acquire;
	    } else if (!currentTarget.isValid) {
		if (currentTarget.isSameSector(resTarget.sector)) {
		    /* Target has been visited already */
		    // Do nothing for now
		} else if (isTrustWorthy(msg.getSender(), resTarget)) {
		    currentTarget = resTarget;
		    return AgentState.acquire;
		}
	    } else {
		/* Valid current Target */
		// Do nothing for now
	    }
	}

	return currentState;
    }

    @Override
    protected void sendMessage(AgentState prevState, AgentState currentState,
	    INetworkAgent agentInRange) {
	if (currentTarget != null && currentTarget.isValid == true
		&& (currentState == AgentState.deliver)) {
	    ResourceTarget resTarget = new ResourceTarget(
		    currentTarget.resourceCount, null, currentTarget.sector);
	    agentInRange.pushMessage(new Message(MessageType.ResourceLocation,
		    controllingAgent, resTarget));
	}
    }

    @Override
    public AgentState processPerceivedAgent(AgentState prevState,
	    AgentState currentState, AbstractAgent agent, boolean isLast) {
	return super.processPerceivedAgent(prevState, currentState, agent,
		isLast);
    }

    @Override
    public double makeDirectionDecision(AgentState prevState,
	    AgentState currentState, List<AngleSegment> collisionFreeSegments) {
	return super.makeDirectionDecision(prevState, currentState,
		collisionFreeSegments);
    }

    public boolean isTrustWorthy(INetworkAgent agent, ResourceTarget resTarget) {
	NdPoint resCenter = map.getSectorCenter(resTarget.sector);

	double distanceToTarget = space.getDistance(
		space.getLocation(controllingAgent), resCenter);
	double agentDistToTarget = space.getDistance(space.getLocation(agent),
		resCenter);

	if (agentDistToTarget < distanceToTarget)
	    return true;

	return false;
    }

    @Override
    public void reset() {
	super.reset();
	this.clear();
    }

    @Override
    public void clear() {
	super.clear();
    }

}
