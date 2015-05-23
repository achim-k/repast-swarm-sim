package swarm_sim.exploration;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.IAgent;
import swarm_sim.SectorMap;
import swarm_sim.Strategy;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.Scan;
import swarm_sim.perception.Scan.AttractionType;
import swarm_sim.perception.Scan.GrowingDirection;
import swarm_sim.perception.ScanMoveDecision;

public class MemoryComplexStrategy extends ExplorationStrategy {

    private double prevDirection = RandomHelper.nextDoubleFromTo(-Math.PI,
	    Math.PI);
    private Scan scanLine = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 1000, 1, 1000);
    SectorMap map = new SectorMap(space.getDimensions(), 60, 60, 1);

    private ScanMoveDecision smd;

    public MemoryComplexStrategy(IChromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);

	smd = new ScanMoveDecision(config.segmentCount, config.k,
		config.distanceFactor, config.initProb);
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
    protected double makeDirectionDecision(AgentState prevState,
	    AgentState currentState, List<AngleSegment> collisionFreeSegments) {
	NdPoint currentLocation = space.getLocation(controllingAgent);
	map.setPosition(currentLocation);

	scanLine.addInput(map.getNewMoveAngle());

	smd.setValidSegments(collisionFreeSegments);
	smd.calcProbDist(scanLine);
	smd.normalize();

	prevDirection = smd.getMovementAngle();

	return prevDirection;
    }

    @Override
    protected void clear() {
	smd.clear();
	scanLine.clear();
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
    }

}
