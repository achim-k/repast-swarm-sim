package swarm_sim.exploration;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.AbstractAgent;
import swarm_sim.AdvancedGridValueLayer.FieldDistancePair;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.QuadTree;
import swarm_sim.QuadTree.Node;
import swarm_sim.Strategy;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.communication.Message.MessageType;
import swarm_sim.perception.PDDP;
import swarm_sim.perception.PDDPInput;
import swarm_sim.perception.PDDPInput.AttractionType;
import swarm_sim.perception.PDDPInput.GrowingDirection;

public class MCStrategy extends ExplorationStrategy {

    QuadTree quadTree;
    private Node nodeTarget;

    PDDPInput scanUnknownSectors = new PDDPInput(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 10000, 1, 1000);

    public MCStrategy(IChromosome chrom,
	    Context<AbstractAgent> context, Agent controllingAgent) {
	super(chrom, context, controllingAgent);

	this.quadTree = new QuadTree(config.spaceWidth, config.spaceHeight,
		config.perceptionScope);
	
	/* Choose a random target quadrant */
	double rndX = RandomHelper.nextDoubleFromTo(0, config.spaceWidth);
	double rndY = RandomHelper.nextDoubleFromTo(0, config.spaceHeight);

	quadTree.setLocation(rndX, rndY);
	nodeTarget = quadTree.getSmallestUnfilledNode(rndX, rndY);
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
    protected double makeDirectionDecision(AgentState prevState,
	    AgentState currentState, PDDP pddp) {
	NdPoint currentLocation = space.getLocation(controllingAgent);

	/* Look for close unexplored sectors */
	quadTree.setLocation(currentLocation.getX(), currentLocation.getY());


	Node n = quadTree.getSmallestUnfilledNode(
		    currentLocation.getX(), currentLocation.getY());
	
	if (nodeTarget == null || nodeTarget.contains(n) || nodeTarget.isFilled() || quadTree.nodeFilled(nodeTarget)) {
	    nodeTarget = n;
	}

	if (nodeTarget != null) {
	    NdPoint quadCenter = quadTree.getUnfilledNodeCenter(nodeTarget);
	    scanUnknownSectors.addInput(motionToGoal(quadCenter, pddp));

	    //
	    // double angle = SpatialMath.calcAngleFor2DMovement(space,
	    // currentLocation, quadCenter);
	    // scanUnknownSectors.addInput(angle);
	}
	
	pddp.calcProbDist(scanUnknownSectors);
	pddp.normalize();

	double direction;
	if (config.takeHighestProb)
	    direction = pddp.getMovementAngleWithHighestProbability();
	else
	    direction = pddp.getMovementAngle();

	return direction;
    }
    
    @Override
    public void handleObstacle(AgentState prevState, AgentState currentState,
            FieldDistancePair obs) {
	quadTree.setLocation(obs.loc.getX(), obs.loc.getY());
    }

    @Override
    protected void clear() {
	scanUnknownSectors.clear();

    }

    @Override
    protected void reset() {
	// TODO Auto-generated method stub
	// map.setCurrentSectorUnfilled(); TODO
    }

}
