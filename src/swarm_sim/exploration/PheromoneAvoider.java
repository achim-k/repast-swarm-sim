package swarm_sim.exploration;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.AdvancedGridValueLayer.FieldDistancePair;
import swarm_sim.AdvancedGridValueLayer.FieldType;
import swarm_sim.IAgent;
import swarm_sim.IDisplayAgent;
import swarm_sim.Pheromone;
import swarm_sim.ScanCircle;
import swarm_sim.ScanCircle.AttractionType;
import swarm_sim.ScanCircle.DistributionType;
import swarm_sim.ScanCircle.GrowingDirection;

public class PheromoneAvoider extends DefaultExplorationAgent implements IAgent,
	IDisplayAgent {

    ScanCircle pheromones = new ScanCircle(8, 1, 4, 1,
	    AttractionType.Repelling, DistributionType.Linear,
	    GrowingDirection.Inner, scenario.maxMoveDistance,
	    scenario.perceptionScope, 1, 1);
    ScanCircle obstacles = new ScanCircle(8, 1, 5, AttractionType.Repelling,
	    DistributionType.Linear, GrowingDirection.Inner, 0,
	    scenario.perceptionScope, 1, 2);
    ScanCircle followDirection = new ScanCircle(8, 1, 1,
	    AttractionType.Appealing, DistributionType.Linear,
	    GrowingDirection.Inner, 0, scenario.perceptionScope, 2, 2);

    public PheromoneAvoider(Context<IAgent> context) {
	super(context);
    }

    public void step() {
	defaultStepStart();
	pheromones.clear();
	obstacles.clear();
	followDirection.clear();

	scanEnv();
	move();

	prevState = state;
	defaultStepEnd();
    }

    private void move() {
	double speed = scenario.maxMoveDistance;

	if (state == agentState.exploring) {

	    followDirection.add(directionAngle);
	    ScanCircle resulting = ScanCircle.merge(8, 0.08, pheromones,
		    followDirection, obstacles);
	    directionAngle = resulting.getMovementAngle();

	    currentLocation = space
		    .moveByVector(this, speed, directionAngle, 0);

	    if (consecutiveMoveCount % scenario.rndConsecutiveMoves == 0
		    && pheromones.getInputCount() == 0) {
		Pheromone p = new Pheromone();
		context.add(p);
		space.moveTo(p, currentLocation.getX(), currentLocation.getY());
	    }
	    consecutiveMoveCount++;

	} else if (state == agentState.blackbox_found) {
	    /* Go back to base */
	    NdPoint baseLocation = space.getLocation(scenario.baseAgent);

	    double moveDistance = space.getDistance(currentLocation,
		    baseLocation);
	    if (moveDistance > speed) {
		moveDistance = speed;
	    }
	    double movementAngle = SpatialMath.calcAngleFor2DMovement(space,
		    currentLocation, baseLocation);
	    currentLocation = space.moveByVector(this, moveDistance,
		    movementAngle, 0);

	    if (moveDistance <= 0.2) {
		System.out
			.println("Agent which found BB has arrived at Base, end of Simulation");
		RunEnvironment.getInstance().endRun();
	    }
	}
    }

    private void scanEnv() {

	/* check for obstacles */
	for (FieldDistancePair field : surroundingFields) {
	    if (field.fieldType == FieldType.Obstacle) {
		double angle = SpatialMath.calcAngleFor2DMovement(space,
			currentLocation, new NdPoint(field.x, field.y));
		obstacles.add(obstacles.new InputPair(angle, field.distance));
	    }
	}

	/* Pheromone scan */
	ContinuousWithin<IAgent> withinQuery = new ContinuousWithin<IAgent>(
		space, this, scenario.perceptionScope);
	for (IAgent agent : withinQuery.query()) {
	    switch (agent.getAgentType()) {
	    case Pheromone:
		double angle = SpatialMath.calcAngleFor2DMovement(space,
			currentLocation, space.getLocation(agent));
		double distance = space.getDistance(space.getLocation(this),
			space.getLocation(agent));
		pheromones.add(angle, distance);
		break;
	    default:
		break;
	    }
	}
    }

    @Override
    public String getName() {
	return "BB_PheromoneAvoider" + agentId;
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.EXPL_PheromoneAvoider;
    }

}
