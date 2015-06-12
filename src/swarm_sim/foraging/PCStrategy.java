package swarm_sim.foraging;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.AbstractAgent;
import swarm_sim.AdvancedGridValueLayer.FieldDistancePair;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.AdvancedGridValueLayer;
import swarm_sim.Strategy;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.perception.CollisionAvoidance;
import swarm_sim.perception.PDDP;
import swarm_sim.perception.PDDPInput;
import swarm_sim.perception.PDDPInput.AttractionType;
import swarm_sim.perception.PDDPInput.GrowingDirection;

public class PCStrategy extends ForagingStrategy {

    double pheromoneValue = 100;
    NdPoint lastDroppedPoint;

    AdvancedGridValueLayer pheromoneLayer;

    PDDPInput scanPheromones = new PDDPInput(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 1E8, 1,
	    1000);

    public PCStrategy(IChromosome chrom, Context<AbstractAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);

	pheromoneLayer = (AdvancedGridValueLayer) context
		.getValueLayer("layer_pheromones");
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

    public AgentState scanForPheromones(AgentState prevState,
	    AgentState currentState) {
	NdPoint currentLocation = space.getLocation(controllingAgent);

	List<FieldDistancePair> fields = pheromoneLayer.getFieldsRadial(
		currentLocation, config.perceptionScope);
	
//	double angleToBase = SpatialMath.calcAngleFor2DMovement(space,
//		    currentLocation, space.getLocation(config.baseAgent));

	for (FieldDistancePair field : fields) {
	    if (field.value < 1)
		continue;

	    double angle = SpatialMath.calcAngleFor2DMovement(space,
		    currentLocation, field.loc);
	    
	    if(Math.abs(CollisionAvoidance.angleDistance(angle, directionAngle)) < Math.PI/2)
		scanPheromones.addInput(angle, 1/field.value);
	}

	if (scanPheromones.isValid())
	    return AgentState.acquire;
	else
	    return currentState;
    }

    @Override
    public AgentState checkState(AgentState prevState, AgentState currentState) {
	currentState = super.checkState(prevState, currentState);

	if (currentState == AgentState.deliver) {
	    dropPheromone();
	    pheromoneValue *= 0.97;
	} else if (currentState == AgentState.wander) {
	    scanForPheromones(prevState, currentState);
	    if (scanPheromones.isValid())
		return AgentState.acquire;
	}

	if (prevState == AgentState.deliver
		&& currentState == AgentState.acquire)
	    pheromoneValue = 100;

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
	    
	    if(config.takeHighestProb)
		directionAngle = pddp.getMovementAngleWithHighestProbability();
	    else
		directionAngle = pddp.getMovementAngle();
	    return directionAngle;
	} else {
	    return super.makeDirectionDecision(prevState, currentState, pddp);
	}
    }

    @Override
    protected void sendMessage(AgentState prevState, AgentState currentState,
	    INetworkAgent agentInRange) {
    }

    @Override
    public void reset() {
	super.reset();
	pheromoneValue = 100;
    }

    @Override
    public void clear() {
	super.clear();
	scanPheromones.clear();
    }

    public void dropPheromone() {
	NdPoint currLoc = space.getLocation(controllingAgent);
	
	if(lastDroppedPoint == null)
	    lastDroppedPoint = currLoc;

	if (!(currLoc.getCoordInt(0) == lastDroppedPoint.getCoordInt(0) && currLoc
		.getCoordInt(1) == lastDroppedPoint.getCoordInt(1))) {
	    double fieldVal = pheromoneLayer
		    .get(currLoc.getX(), currLoc.getY());
	    pheromoneLayer.set(fieldVal + pheromoneValue, (int) currLoc.getX(),
		    (int) currLoc.getY());
	}

	lastDroppedPoint = currLoc;
    }
}
