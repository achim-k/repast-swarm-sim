package swarm_sim.foraging;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.AbstractAgent;
import swarm_sim.AbstractAgent.AgentType;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.Pheromone;
import swarm_sim.Strategy;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.perception.AngleFilter;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.PDDP;
import swarm_sim.perception.PDDPInput;
import swarm_sim.perception.PDDPInput.AttractionType;
import swarm_sim.perception.PDDPInput.GrowingDirection;

public class PCStrategy extends ForagingStrategy {

    int pheromoneDropMoveCount = 0;
    int perceivedPheromones = 0;

    PDDPInput scanPheromones = new PDDPInput(AttractionType.Attracting,
	    GrowingDirection.Outwards, 1, true, 0, config.perceptionScope, 1,
	    20);

    public PCStrategy(IChromosome chrom, Context<AbstractAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
    }

    @Override
    protected List<MessageTypeRegisterPair> getMessageTypesToRegister(
	    CommunicationType[] allowedCommTypes) {
	// No direct communication here
	return new ArrayList<Strategy.MessageTypeRegisterPair>();
    }

    @Override
    protected AgentState processMessage(AgentState prevState,
	    AgentState currentState, Message msg, boolean isLast) {
	// No direct communication here
	return currentState;
    }

    @Override
    public AgentState processPerceivedAgent(AgentState prevState,
	    AgentState currentState, AbstractAgent agent, boolean isLast) {
	NdPoint currentLocation = space.getLocation(controllingAgent);
	double baseAngle = SpatialMath.calcAngleFor2DMovement(space,
		currentLocation, space.getLocation(config.baseAgent));

	if (agent.getAgentType() == AgentType.Pheromone) {
	    perceivedPheromones++;
	    if (currentState == AgentState.acquire
		    && (currentTarget == null || !currentTarget.isValid)) {
		NdPoint pLoc = space.getLocation(agent);
		double pAngle = SpatialMath.calcAngleFor2DMovement(space,
			currentLocation, pLoc);

		if (prevState == AgentState.wander
			|| AngleFilter.angleDistance(pAngle, baseAngle) > Math.PI) {
		    scanPheromones.addInput(pAngle,
			    space.getDistance(currentLocation, pLoc));
		}
		return AgentState.acquire;
	    }
	    return currentState;
	} else
	    return super.processPerceivedAgent(prevState, currentState, agent,
		    isLast);
    }

    @Override
    public AgentState checkState(AgentState prevState, AgentState currentState) {
	currentState = super.checkState(prevState, currentState);

	if (currentState == AgentState.deliver) {
	    if (++pheromoneDropMoveCount >= config.maxMoveDistance) {
		NdPoint currLoc = space.getLocation(controllingAgent);
		pheromoneDropMoveCount = 0;
		Pheromone p = new Pheromone();
		context.add(p);
		space.moveTo(p, currLoc.getX(), currLoc.getY());
	    }
	} else {
	    if (scanPheromones.isValid())
		return AgentState.acquire;
	}
	return currentState;
    }

    @Override
    public double makeDirectionDecision(AgentState prevState,
	    AgentState currentState, PDDP pddp) {
	if (currentState == AgentState.acquire) {
	    if (scanResources.isValid()
		    || (currentTarget != null && currentTarget.isValid)) {
		return super.makeDirectionDecision(prevState, currentState,
			pddp);
	    }

	    pddp.calcProbDist(scanPheromones);
	    pddp.normalize();
	    directionAngle = pddp.getMovementAngle();
	    return directionAngle;
	} else {
	    return super.makeDirectionDecision(prevState, currentState,
		    pddp);
	}
    }

    @Override
    protected void sendMessage(AgentState prevState, AgentState currentState,
	    INetworkAgent agentInRange) {
    }

    @Override
    public void reset() {
	super.reset();
	pheromoneDropMoveCount = 0;
    }

    @Override
    public void clear() {
	super.clear();
	perceivedPheromones = 0;
	scanPheromones.clear();
    }

}
