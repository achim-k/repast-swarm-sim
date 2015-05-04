package swarm_sim.exploration;

import java.util.ArrayList;
import java.util.List;

import org.jgap.Chromosome;
import org.jgap.IChromosome;

import repast.simphony.context.Context;
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

public class MemoryComplexStrategy extends ExplorationStrategy {

    SectorMap map = new SectorMap(space.getDimensions(), 60, 60, 1);

    public MemoryComplexStrategy(IChromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
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
    protected AgentState processPerceivedAgent(AgentState prevState,
	    AgentState currentState, IAgent agent, boolean isLast) {
	if (agent.getAgentType() == AgentType.Resource)
	    return AgentState.acquire;

	return AgentState.wander;
    }

    @Override
    protected double makeDirectionDecision(AgentState prevState,
	    AgentState currentState, List<AngleSegment> collisionFreeSegments) {
	NdPoint currentLocation = space.getLocation(controllingAgent);
	map.setPosition(currentLocation);
	return map.getNewMoveAngle();
    }

    @Override
    protected void clear() {
	// N/A here
    }

    @Override
    protected void reset() {
	// set current sector unfilled, so agent will return here some time
	map.setCurrentSectorUnfilled();
    }

    @Override
    protected List<MessageTypeRegisterPair> getMessageTypesToRegister(
	    CommunicationType[] allowedCommTypes) {
	List<MessageTypeRegisterPair> ret = new ArrayList<Strategy.MessageTypeRegisterPair>();
	/* No communication here */
	return ret;
	
//	List<MessageTypeRegisterPair> ret = new ArrayList<Strategy.MessageTypeRegisterPair>();
//	for (CommunicationType commType : allowedCommTypes) {
//	    switch (commType) {
//	    case MapOrTargets:
//		AgentState states[] = new AgentState[] { AgentState.wander };
//		ret.add(new MessageTypeRegisterPair(MessageType.SectorMap,
//			states));
//		break;
//	    default:
//		break;
//	    }
//	}
//	return ret;
    }

}
