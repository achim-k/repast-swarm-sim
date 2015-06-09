package swarm_sim.exploration;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.AbstractAgent;
import swarm_sim.Agent;
import swarm_sim.AdvancedGridValueLayer.FieldDistancePair;
import swarm_sim.Agent.AgentState;
import swarm_sim.SectorMap;
import swarm_sim.Strategy;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.perception.PDDP;
import swarm_sim.perception.PDDPInput;
import swarm_sim.perception.PDDPInput.AttractionType;
import swarm_sim.perception.PDDPInput.GrowingDirection;

public class CMStrategy extends ExplorationStrategy {

    private double prevDirection = RandomHelper.nextDoubleFromTo(-Math.PI,
	    Math.PI);
    private PDDPInput scanLine = new PDDPInput(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 1000, 1, 1000);
    SectorMap map = new SectorMap(space.getDimensions(), 60, 60, 1);

    public CMStrategy(IChromosome chrom,
	    Context<AbstractAgent> context, Agent controllingAgent) {
	super(chrom, context, controllingAgent);

	int sectorsX = (int) (0.625 * config.spaceWidth / config.perceptionScope);
	int sectorsY = (int) (0.625 * config.spaceHeight / config.perceptionScope);

	if (sectorsX > config.spaceWidth)
	    sectorsX = config.spaceWidth;
	if (sectorsY > config.spaceHeight)
	    sectorsY = config.spaceHeight;

	map = new SectorMap(space.getDimensions(), sectorsX, sectorsY, 1);
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
	    AgentState currentState, PDDP pddp) {
	NdPoint currentLocation = space.getLocation(controllingAgent);
	map.setPosition(currentLocation);
	map.getNewMoveAngle();
	scanLine.addInput(motionToGoal(map.getSectorCenter(map.getTargetSector()), pddp));

	pddp.calcProbDist(scanLine);
	pddp.normalize();

	if (config.takeHighestProb)
	    prevDirection = pddp.getMovementAngleWithHighestProbability();
	else
	    prevDirection = pddp.getMovementAngle();

	return prevDirection;
    }

    @Override
    protected void clear() {
	scanLine.clear();
    }

    @Override
    public void handleObstacle(AgentState prevState, AgentState currentState,
            FieldDistancePair obs) {
	map.setPosition(obs.loc);
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
