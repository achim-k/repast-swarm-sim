package swarm_sim.exploration;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.IAgent;
import swarm_sim.QuadTree;
import swarm_sim.QuadTree.Node;
import swarm_sim.Strategy;
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

public class ComplexQTMemoryCommStrategy extends ExplorationStrategy {

    private double prevDirection = RandomHelper.nextDoubleFromTo(-Math.PI,
	    Math.PI);

    QuadTree quadTree;

    private Scan scanAgentRepell = new Scan(AttractionType.Repelling,
	    GrowingDirection.Inwards, 2, false, 0, 0.8 * config.commScope, 1,
	    1000);
    private Scan scanAgentAppeal = new Scan(AttractionType.Attracting,
	    GrowingDirection.Outwards, 0.2, false, 0.8 * config.commScope,
	    config.commScope, 1, 1000);
    private Scan scanAgentMimic = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, config.commScope, 1, 1000);
    private Scan scanPrevDirection = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 1000, 1, 1000);
    private Scan scanUnknownSectors = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 10000, 1, 1000);

    private ScanMoveDecision smd;

    public ComplexQTMemoryCommStrategy(IChromosome chrom,
	    Context<IAgent> context, Agent controllingAgent) {
	super(chrom, context, controllingAgent);

	smd = new ScanMoveDecision(config.segmentCount, config.k,
		config.distanceFactor, config.initProb);

	int sectorsX = (int) (config.spaceWidth / config.perceptionScope);
	int sectorsY = (int) (config.spaceHeight / config.perceptionScope);

	if (sectorsX > config.spaceWidth)
	    sectorsX = config.spaceWidth;
	if (sectorsY > config.spaceHeight)
	    sectorsY = config.spaceHeight;

	this.quadTree = new QuadTree(config.spaceWidth, config.spaceHeight,
		config.perceptionScope);

	if (config.useGA) {
	    GA ga = GA.getInstance();

	    scanAgentRepell.setMergeWeight((double) chrom.getGene(
		    ga.RepellIndex).getAllele());
	    scanAgentAppeal.setMergeWeight((double) chrom.getGene(
		    ga.AppealIndex).getAllele());
	    scanAgentMimic.setMergeWeight((double) chrom.getGene(ga.MimicIndex)
		    .getAllele());
	    scanUnknownSectors.setMergeWeight((double) chrom.getGene(
		    ga.MemoryIndex).getAllele());
	    scanPrevDirection.setMergeWeight((double) chrom.getGene(
		    ga.PrevDirectionIndex).getAllele());

	    double repellAppealBorder = config.commScope
		    * (double) chrom.getGene(ga.AppealRepellBorderIndex)
			    .getAllele();
	    scanAgentRepell.setOuterBorderRadius(repellAppealBorder);
	    scanAgentAppeal.setInnerBorderRadius(repellAppealBorder);
	} else {
	    double winningOutput[] = new double[] { 1,
		    0.1, 0.9,
		    0.1, 0.23248373248966592,
		    1 };

	    scanAgentRepell.setMergeWeight(winningOutput[0]);
	    scanAgentAppeal.setMergeWeight(winningOutput[1]);
	    scanAgentMimic.setMergeWeight(winningOutput[3]);
	    scanPrevDirection.setMergeWeight(winningOutput[4]);
	    scanUnknownSectors.setMergeWeight(winningOutput[5]);

	    double repellAppealBorder = config.commScope * winningOutput[2];
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
	    scanAgentAppeal.addInput(angle, distance);
	    
	    /* Do not add to repell, if the border of the search area is closer */
	    double delta[] = space.getDisplacement(agentLoc, currentLoc);
	    double p[] = new double[] { currentLoc.getX() + delta[0],
		    currentLoc.getY() + delta[1] };

	    if (p[0] <= config.spaceWidth && p[0] >= 0 && p[1] >= 0
		    && p[1] <= config.spaceHeight) {
		scanAgentRepell.addInput(angle, distance);
	    }

	} else if (msg.getType() == MessageType.Direction) {
	    NdPoint currentLoc = space.getLocation(controllingAgent);
	    NdPoint agentLoc = space.getLocation(msg.getSender());
	    double distance = space.getDistance(currentLoc, agentLoc);
	    scanAgentMimic.addInput((double) msg.getData(), distance);

	} else if (msg.getType() == MessageType.SectorMap) {
	    quadTree.merge((QuadTree) msg.getData());
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
		    controllingAgent, quadTree));
	}
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

	if (prevDirection >= -Math.PI)
	    scanPrevDirection.addInput(prevDirection);

	smd.setValidSegments(collisionFreeSegments);

	smd.calcProbDist(scanAgentAppeal, scanAgentRepell, scanAgentMimic,
		scanPrevDirection, scanUnknownSectors);

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
	// map.setCurrentSectorUnfilled(); TODO
	smd.clear();
    }

}
