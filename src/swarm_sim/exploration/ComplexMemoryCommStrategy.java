package swarm_sim.exploration;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
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
import swarm_sim.learning.GA;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.Scan;
import swarm_sim.perception.Scan.AttractionType;
import swarm_sim.perception.Scan.GrowingDirection;
import swarm_sim.perception.ScanMoveDecision;

public class ComplexMemoryCommStrategy extends ExplorationStrategy {

    int segmentCount = 8;

    double prevDirection = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);

    SectorMap map;

    Scan scanAgentRepell = new Scan(AttractionType.Repelling,
	    GrowingDirection.Inwards, 2, false, 0, 0.8 * config.commScope, 1,
	    1000);
    Scan scanAgentAppeal = new Scan(AttractionType.Attracting,
	    GrowingDirection.Outwards, 0.2, false, 0.8 * config.commScope,
	    config.commScope, 1, 1);
    Scan scanAgentMimic = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, config.commScope, 1, 5);
    Scan scanPrevDirection = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 1000, 1, 1000);
    Scan scanUnknownSectors = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 10000, 1, 1000);

    ScanMoveDecision smd = new ScanMoveDecision(8, 6, 10, 0.05);

    public ComplexMemoryCommStrategy(IChromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);

	int sectorsX = (int) (config.spaceWidth / config.perceptionScope);
	int sectorsY = (int) (config.spaceHeight / config.perceptionScope);

	if (sectorsX > config.spaceWidth)
	    sectorsX = config.spaceWidth;
	if (sectorsY > config.spaceHeight)
	    sectorsY = config.spaceHeight;

	map = new SectorMap(space.getDimensions(), sectorsX, sectorsY, 1);
	
	if(config.useGA) {
	    GA ga = GA.getInstance();
	    
	    scanAgentRepell.setMergeWeight((double)chrom.getGene(ga.RepellIndex).getAllele());
	    scanAgentAppeal.setMergeWeight((double)chrom.getGene(ga.AppealIndex).getAllele());
	    scanAgentMimic.setMergeWeight((double)chrom.getGene(ga.MimicIndex).getAllele());
	    scanUnknownSectors.setMergeWeight((double)chrom.getGene(ga.MemoryIndex).getAllele());
	    scanPrevDirection.setMergeWeight((double)chrom.getGene(ga.PrevDirectionIndex).getAllele());
	    
	    double repellAppealBorder = config.commScope * (double)chrom.getGene(ga.AppealRepellBorderIndex).getAllele();
	    scanAgentRepell.setOuterBorderRadius(repellAppealBorder);
	    scanAgentAppeal.setInnerBorderRadius(repellAppealBorder);
	}
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
		break;
	    case MapOrTargets:
		AgentState states3[] = new AgentState[] { AgentState.wander };
		ret.add(new MessageTypeRegisterPair(MessageType.SectorMap,
			states3));
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

	if (msg.getType() == MessageType.Location) {
	    NdPoint currentLoc = space.getLocation(controllingAgent);
	    NdPoint agentLoc = space.getLocation(msg.getSender());
	    double distance = space.getDistance(currentLoc, agentLoc);
	    double angle = SpatialMath.calcAngleFor2DMovement(space,
		    currentLoc, agentLoc);
	    scanAgentRepell.addInput(angle, distance);
	    scanAgentAppeal.addInput(angle, distance);

	} else if (msg.getType() == MessageType.Direction) {
	    NdPoint currentLoc = space.getLocation(controllingAgent);
	    NdPoint agentLoc = space.getLocation(msg.getSender());
	    double distance = space.getDistance(currentLoc, agentLoc);
	    scanAgentMimic.addInput((double) msg.getData(), distance);

	} else if (msg.getType() == MessageType.SectorMap) {
	    map.merge((SectorMap) msg.getData());
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
	    agentInRange.pushMessage(new Message(MessageType.SectorMap,
		    controllingAgent, map));
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
	NdPoint currentLocation = space.getLocation(controllingAgent);

	map.setPosition(currentLocation);
	List<Integer[]> closeUnfilledSectors = map.getCloseUnfilledSectors(5);
	for (Integer[] d : closeUnfilledSectors) {
	    double angle = SpatialMath.angleFromDisplacement(d[0], d[1]);
	    double distance = Math.sqrt(d[0] * d[0] + d[1] * d[1]);
	    scanUnknownSectors.addInput(angle, distance);
	}

	if (prevDirection >= -Math.PI)
	    scanPrevDirection.addInput(prevDirection);

	smd.setValidSegments(collisionFreeSegments);

	if (scanUnknownSectors.isValid()) {
	    smd.calcProbDist(scanAgentAppeal, scanAgentRepell, scanAgentMimic,
		    scanPrevDirection, scanUnknownSectors);
	} else {
	    smd.calcProbDist(scanAgentRepell, scanPrevDirection);
	}

	smd.normalize();
	prevDirection = smd.getMovementAngle();
	return prevDirection;
    }

    @Override
    protected void clear() {
	scanAgentRepell.clear();
	scanAgentAppeal.clear();
	scanAgentMimic.clear();
	scanPrevDirection.clear();
	scanUnknownSectors.clear();
	smd.clear();
    }

    @Override
    protected void reset() {
	prevDirection = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);
	map.setCurrentSectorUnfilled();
    }

}
