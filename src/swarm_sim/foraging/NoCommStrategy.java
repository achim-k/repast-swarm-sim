package swarm_sim.foraging;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import swarm_sim.AbstractAgent;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.Strategy;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.perception.AngleSegment;

public class NoCommStrategy extends ForagingStrategy {

    public NoCommStrategy(IChromosome chrom, Context<AbstractAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
    }

    @Override
    public AgentState checkState(AgentState prevState, AgentState currentState) {
	return super.checkState(prevState, currentState);
    }

    @Override
    protected List<MessageTypeRegisterPair> getMessageTypesToRegister(
	    CommunicationType[] allowedCommTypes) {
	List<MessageTypeRegisterPair> ret = new ArrayList<Strategy.MessageTypeRegisterPair>();
	/* No communication here */
	return ret;
    }

    @Override
    protected AgentState processMessage(AgentState prevState,
	    AgentState currentState, Message msg, boolean isLast) {
	return currentState;
    }

    @Override
    protected void sendMessage(AgentState prevState, AgentState currentState,
	    INetworkAgent agentInRange) {
	return;
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
