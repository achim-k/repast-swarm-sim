package swarm_sim.exploration;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.IAgent;
import swarm_sim.IAgent.AgentType;
import swarm_sim.QuadTree;
import swarm_sim.QuadTree.Node;
import swarm_sim.Strategy;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.communication.Message.MessageType;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.Scan;
import swarm_sim.perception.Scan.AttractionType;
import swarm_sim.perception.Scan.GrowingDirection;
import swarm_sim.perception.ScanMoveDecision;

public class QTMemoryCommStrategy extends ExplorationStrategy {

    QuadTree quadTree;

    Scan scanUnknownSectors = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 10000, 1, 1000);

    ScanMoveDecision smd = new ScanMoveDecision(8, 6, 10, 0.05);

    public QTMemoryCommStrategy(IChromosome chrom,
	    Context<IAgent> context, Agent controllingAgent) {
	super(chrom, context, controllingAgent);

	this.quadTree = new QuadTree(config.spaceWidth, config.spaceHeight,
		config.perceptionScope);
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

    @Override
    protected AgentState processMessage(AgentState prevState,
	    AgentState currentState, Message msg, boolean isLast) {
	if (isLast)
	    return currentState;

	if (msg.getType() == MessageType.SectorMap)
	    quadTree.merge((QuadTree) msg.getData());

	return currentState;
    }

    @Override
    protected void sendMessage(AgentState prevState, AgentState currentState,
	    INetworkAgent agentInRange) {
	if (currentState == AgentState.wander) {
	    agentInRange.pushMessage(new Message(MessageType.SectorMap,
		    controllingAgent, quadTree));
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
	quadTree.setLocation(currentLocation.getX(), currentLocation.getY());

	Node n = quadTree.getSmallestUnfilledNode(currentLocation.getX(),
		currentLocation.getY());

	if (n != null) {
	    NdPoint quadCenter = quadTree.getUnfilledNodeCenter(n,
		    currentLocation);
	    double angle = SpatialMath.calcAngleFor2DMovement(space,
		    currentLocation, quadCenter);
	    scanUnknownSectors.addInput(angle);
	}

	smd.setValidSegments(collisionFreeSegments);
	smd.calcProbDist(scanUnknownSectors);
	smd.normalize();

	double direction = smd.getMovementAngle();

	return direction;
    }

    @Override
    protected void clear() {
	scanUnknownSectors.clear();
	smd.clear();

    }

    @Override
    protected void reset() {
	// TODO Auto-generated method stub
//	map.setCurrentSectorUnfilled(); TODO
    }

}
