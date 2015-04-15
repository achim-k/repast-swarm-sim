package swarm_sim.exploration;

import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.SpatialMath;
import swarm_sim.Agent;
import swarm_sim.DisplayAgent;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.CircleScan;

/**
 * Agent which explores the space randomly
 * 
 * @author achim
 * 
 */
public class Random extends DefaultExplorationAgent implements Agent,
	DisplayAgent {

    int binCount = 8;

    public Random(Context<Agent> context) {
	super(context);
    }

    public void step() {
	defaultStepStart();
	scanEnv();
	move();
	prevState = state;
	defaultStepEnd();
    }

    private void move() {
	AngleSegment moveCircle = new AngleSegment(-Math.PI, Math.PI);
	List<AngleSegment> moveCircleFree = moveCircle
		.filterSegment(collisionAngleFilter.getFilterSegments());

	if (consecutiveMoveCount < scenario.rndConsecutiveMoves) {
	    boolean moveAllowed = false;
	    for (AngleSegment as : moveCircleFree) {
		if (as.start <= directionAngle && as.end >= directionAngle) {
		    moveAllowed = true;
		    break;
		}
	    }

	    if (moveAllowed) {
		currentLocation = space.moveByVector(this,
			scenario.maxMoveDistance, directionAngle, 0);
		consecutiveMoveCount++;
		return;
	    }
	}

	/* Choose random direction */
	CircleScan res = CircleScan.merge(8, 0, moveCircleFree);
	directionAngle = res.getMovementAngle();
	if (directionAngle > -10) {
	    currentLocation = space.moveByVector(this,
		    scenario.maxMoveDistance, directionAngle, 0);
	}
	consecutiveMoveCount = 0;
    }

    private void scanEnv() {
	/* scan environment for surrounding agents, pheromones, resources, ... */
	ContinuousWithin<Agent> withinQuery = new ContinuousWithin<Agent>(
		space, this, scenario.perceptionScope);
	for (Agent agent : withinQuery.query()) {
	    switch (agent.getAgentType()) {
	    case EXPL_Random:
		double distance = space.getDistance(space.getLocation(this),
			space.getLocation(agent));
		if (distance > 0 && distance <= scenario.maxMoveDistance + 1) {
		    double angle = SpatialMath.calcAngleFor2DMovement(space,
			    currentLocation, space.getLocation(agent));
		    collisionAngleFilter.add(distance, angle);
		}
		break;
	    default:
		break;
	    }
	}
    }

    @Override
    public String getName() {
	return "RandomExplorerNoComm" + agentId;
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.EXPL_Random;
    }
}
