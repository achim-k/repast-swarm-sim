package swarm_sim.exploration;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.IAgent;
import swarm_sim.SectorMap;
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

public class MemoryCommStrategy extends ExplorationStrategy {

    int segmentCount = 8;

    
    SectorMap map;
    
    Scan scanUnknownSectors = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0,
	    10000, 1, 1000);
    
    ScanMoveDecision smd = new ScanMoveDecision(8, 6, 10, 0.05);

    public MemoryCommStrategy(IChromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
	
	int sectorsX = (int) (config.spaceWidth / config.perceptionScope);
	int sectorsY = (int) (config.spaceHeight / config.perceptionScope);
	
	if(sectorsX > config.spaceWidth)
	    sectorsX = config.spaceWidth;
	if(sectorsY > config.spaceHeight)
	    sectorsY = config.spaceHeight;
	
	map = new SectorMap(space.getDimensions(), sectorsX, sectorsY, 1);
    }

    @Override
    protected AgentState processMessage(AgentState prevState,
	    AgentState currentState, Message msg, boolean isLast) {
	if (isLast)
	    return currentState;

	if (msg.getType() == MessageType.SectorMap)
	    map.merge((SectorMap) msg.getData());

	return currentState;
    }

    @Override
    protected void sendMessage(AgentState prevState, AgentState currentState,
	    INetworkAgent agentInRange) {
	if (currentState == AgentState.wander) {
	    agentInRange.pushMessage(new Message(MessageType.SectorMap,
		    controllingAgent, map));
	}
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

	/* Look for close unexplored sectors */
	map.setPosition(currentLocation);
	List<Integer[]> closeUnfilledSectors = map.getCloseUnfilledSectors(3);
	for (Integer[] d : closeUnfilledSectors) {
	    double angle = SpatialMath.angleFromDisplacement(d[0], d[1]);
	    double distance = Math.sqrt(d[0] * d[0] + d[1] * d[1]);
	    scanUnknownSectors.addInput(angle, distance);
	}

	smd.setValidSegments(collisionFreeSegments);
	smd.calcProbDist(scanUnknownSectors);
	smd.normalize();
	
	return smd.getMovementAngle();
    }

    @Override
    protected void clear() {
	scanUnknownSectors.clear();
	smd.clear();
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
	for (CommunicationType commType : allowedCommTypes) {
	    switch (commType) {
	    case MapOrTargets:
		AgentState states[] = new AgentState[] { AgentState.wander };
		ret.add(new MessageTypeRegisterPair(MessageType.SectorMap,
			states));
		break;
	    default:
		break;
	    }
	}
	return ret;
    }

}
