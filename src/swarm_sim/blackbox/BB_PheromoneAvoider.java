package swarm_sim.blackbox;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.AdvancedGridValueLayer.FieldDistancePair;
import swarm_sim.AdvancedGridValueLayer.FieldType;
import swarm_sim.Agent;
import swarm_sim.DisplayAgent;
import swarm_sim.Pheromone;
import swarm_sim.ScanCircle;
import swarm_sim.ScanCircle.AttractionType;
import swarm_sim.ScanCircle.DistributionType;
import swarm_sim.ScanCircle.GrowingDirection;

public class BB_PheromoneAvoider extends DefaultBlackboxAgent implements
		Agent, DisplayAgent {

	ScanCircle pheromones = new ScanCircle(8, 1, 4, 1, AttractionType.Repelling, DistributionType.Linear, GrowingDirection.Inner, scenario.agentMovementSpeed,
			scenario.perceptionScope, 1, 1);
	ScanCircle obstacles = new ScanCircle(8, 1, 5, AttractionType.Repelling, DistributionType.Linear, GrowingDirection.Inner, 0,
			scenario.perceptionScope, 1, 2);
	ScanCircle followDirection = new ScanCircle(8, 1, 1, AttractionType.Appealing, DistributionType.Linear, GrowingDirection.Inner, 0,
			scenario.perceptionScope, 2, 2);

	public void step() {
		defaultStepStart();
		pheromones.clear();
		obstacles.clear();
		followDirection.clear();
		
		move();
		
		
		if (scanEnv()) {
			bbScenario.blackboxFound();
			state = agentState.blackbox_found;
		}
		prevState = state;
		defaultStepEnd();
	}

	private void move() {
		double speed = scenario.agentMovementSpeed;

		/* check for obstacles */
		for (FieldDistancePair field : surroundingFields) {
			if (field.fieldType == FieldType.Obstacle) {
				double angle = SpatialMath.calcAngleFor2DMovement(space,
						currentLocation, new NdPoint(field.x, field.y));
				obstacles.add(obstacles.new InputPair(angle, field.distance));
			}
		}
		
		/* Pheromone scan */
		ContinuousWithin<Agent> withinQuery = new ContinuousWithin<Agent>(
				space, this, scenario.perceptionScope);
		for (Agent agent : withinQuery.query()) {
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
		
		if (state == agentState.exploring) {
			
			followDirection.add(directionAngle);
			ScanCircle resulting = ScanCircle.merge(8, 0.08, pheromones, followDirection, obstacles);
			directionAngle = resulting.getMovementAngle();
			
			currentLocation = space
					.moveByVector(this, speed, directionAngle, 0);
			
			if(consecutiveMoveCount % scenario.randomConsecutiveMoves == 0 && pheromones.getInputCount() == 0)
			{
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

	private boolean scanEnv() {

		/* CHeck if bb in perception scope */
		NdPoint baseLocation = space.getLocation(bbScenario.blackboxAgent);
		if (space.getDistance(currentLocation, baseLocation) <= scenario.perceptionScope) {
			System.out.println("bb found");
			return true; /* Blackbox found */
		}
		return false;
	}

	public BB_PheromoneAvoider(Context<Agent> context,
			Context<Agent> rootContext) {
		super(context, rootContext);
	}

	@Override
	public String getName() {
		return "BB_PheromoneAvoider" + agentId;
	}

	@Override
	public AgentType getAgentType() {
		return AgentType.BB_PheromoneAvoider;
	}

}
