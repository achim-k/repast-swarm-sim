package swarm_sim.foraging;

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
public class Random extends DefaultForagingAgent implements Agent, DisplayAgent {

    int segmentCount = 8;

    CircleScan resourceScan = new CircleScan(segmentCount, 1, 1, 10, 1, 0, 1, 0,
	    scenario.perceptionScope);
    CircleScan deliverDirection = new CircleScan(segmentCount, 1, 1, 10, 1, 1, 1,
	    0, scenario.perceptionScope);

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

	switch (state) {
	case wander:
	    directionAngle = getWanderDirection(moveCircleFree);
	    break;
	case acquire:
	    directionAngle = getAcquireDirection(moveCircleFree);
	    break;
	case deliver:
	    directionAngle = getDeliverDirection(moveCircleFree);
	    break;
	default:
	    System.err.println("unkown state: " + state);
	    break;
	}


	if (directionAngle > -10) {
	    currentLocation = space.moveByVector(this,
		    scenario.maxMoveDistance, directionAngle, 0);
	}
    }
    
    private double getWanderDirection(List<AngleSegment> moveCircleFree) {
	if (consecutiveMoveCount < scenario.rndConsecutiveMoves) {
	    boolean moveAllowed = false;
	    for (AngleSegment as : moveCircleFree) {
		if (as.start <= directionAngle && as.end >= directionAngle) {
		    moveAllowed = true;
		    break;
		}
	    }

	    if (moveAllowed) {
		consecutiveMoveCount++;
		return directionAngle;
	    }
	}
	
	/* Choose random direction */
	consecutiveMoveCount = 0;
	CircleScan res = CircleScan.merge(segmentCount, 0, moveCircleFree);
	return res.getMovementAngle();
    }
    
    private double getAcquireDirection(List<AngleSegment> moveCircleFree) {
	CircleScan res = CircleScan.merge(segmentCount, 0.12, moveCircleFree, resourceScan);
	return res.getMovementAngle();
    }
    
    private double getDeliverDirection(List<AngleSegment> moveCircleFree) {
	deliverDirection.clear();
	double moveAngleToBase = SpatialMath.calcAngleFor2DMovement(space, currentLocation, space.getLocation(scenario.baseAgent));
	deliverDirection.add(moveAngleToBase);
	CircleScan res = CircleScan.merge(segmentCount, 0.12, moveCircleFree, deliverDirection);
	return res.getMovementAngle();
    }

    private void scanEnv() {
	resourceScan.clear();
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
	    case Resource:
		double resDistance = space.getDistance(space.getLocation(this),
			space.getLocation(agent));
		if (state == agentState.wander) {
		    state = agentState.acquire;

		    if (resDistance > 0) {
			double angle = SpatialMath.calcAngleFor2DMovement(
				space, currentLocation,
				space.getLocation(agent));
			resourceScan.add(resDistance, angle);
		    }
		} else if (state == agentState.acquire && resDistance <= 0.5 * scenario.maxMoveDistance) {
		    state = agentState.deliver;
		    context.remove(agent);
		}
		break;
	    case Base:
		if(state == agentState.deliver) {
		    double baseDistance = space.getDistance(space.getLocation(this),
				space.getLocation(agent));
		    if(baseDistance <= 0.5 * scenario.maxMoveDistance)
			state = agentState.wander;
		}
	    default:
		break;
	    }
	}
    }

    @Override
    public String getName() {
	return "RandomForager" + agentId;
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.FAGN_Random;
    }
}
