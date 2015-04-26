package swarm_sim.exploration;

import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.SpatialMath;
import swarm_sim.IAgent;
import swarm_sim.IDisplayAgent;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.CircleScan;

public class AgentRepell extends DefaultExplorationAgent implements IAgent,
	IDisplayAgent {

    int binCount = 8;

    CircleScan agentRepell = new CircleScan(binCount, 1, 1, 10000, 1, -1, -2,
	    0, 1 * scenario.commScope);

    public AgentRepell(Context<IAgent> context) {
	super(context);
    }

    public void step() {
	defaultStepStart();
	scanEnv();
	move();
	prevState = state;
	defaultStepEnd();
    }

    public void move() {
	AngleSegment moveCircle = new AngleSegment(-Math.PI, Math.PI);
	List<AngleSegment> moveCircleFree = moveCircle
		.filterSegment(collisionAngleFilter.getFilterSegments());

	CircleScan res = CircleScan.merge(binCount, 0.12, moveCircleFree,
		agentRepell);
	directionAngle = res.getMovementAngle();
	if (directionAngle > -10) {
	    currentLocation = space.moveByVector(this,
		    scenario.maxMoveDistance, directionAngle, 0);
	}
    }

    private void scanEnv() {
	agentRepell.clear();

	for (IAgent agent : commNet.getAdjacent(this)) {
	    switch (agent.getAgentType()) {
	    case EXPL_AgentRepell:
		double angle = SpatialMath.calcAngleFor2DMovement(space,
			currentLocation, space.getLocation(agent));
		double distance = space.getDistance(space.getLocation(this),
			space.getLocation(agent));
		if (distance > 0)
		    agentRepell.add(angle, distance);
		break;
	    default:
		break;
	    }
	}

	/* scan environment for surrounding agents, pheromones, resources, ... */
	ContinuousWithin<IAgent> withinQuery = new ContinuousWithin<IAgent>(
		space, this, scenario.perceptionScope);
	for (IAgent agent : withinQuery.query()) {
	    switch (agent.getAgentType()) {
	    case EXPL_AgentRepell:
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
	return "Repell" + agentId;
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.EXPL_AgentRepell;
    }
}
