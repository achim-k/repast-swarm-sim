package swarm_sim.exploration;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.DisplayAgent;
import swarm_sim.ScanCircle;
import swarm_sim.AdvancedGridValueLayer.FieldDistancePair;
import swarm_sim.AdvancedGridValueLayer.FieldType;
import swarm_sim.ScanCircle.AttractionType;
import swarm_sim.ScanCircle.DistributionType;
import swarm_sim.ScanCircle.GrowingDirection;
import swarm_sim.ScanCircle.InputPair;
import swarm_sim.communication.Message;
import swarm_sim.communication.MsgBlackboxFound;
import swarm_sim.communication.MsgCurrentDirection;
import swarm_sim.communication.NetworkAgent;

public class AgentAvoiderMimicDirectionComm extends DefaultExplorationAgent implements
		Agent, DisplayAgent {

	ScanCircle agentRepellingScan = new ScanCircle(8, 1, 1, AttractionType.Repelling, DistributionType.Linear, GrowingDirection.Inner, 0, 0.8*scenario.commScope, 0.5, 2);
	ScanCircle agentAppealingScan = new ScanCircle(8, 1, 1.5, AttractionType.Appealing, DistributionType.Linear, GrowingDirection.Outer, 0.9*scenario.commScope, scenario.commScope, 0.5, 2);
	ScanCircle agentMimicMoveScan = new ScanCircle(8, 1, 1, AttractionType.Appealing, DistributionType.Linear, GrowingDirection.Outer, 0, scenario.commScope, 0.5, 0.5);
	ScanCircle obstacles = new ScanCircle(8, 1, 5, AttractionType.Repelling, DistributionType.Linear, GrowingDirection.Inner, 0,
			scenario.perceptionScope, 2, 2);
	
	public AgentAvoiderMimicDirectionComm(Context<Agent> context) {
		super(context);
	}
	
	public void step() {
		defaultStepStart();
		agentMimicMoveScan.clear();
		agentAppealingScan.clear();
		agentRepellingScan.clear();
		obstacles.clear();
		
		processMessageQueue();
		scanEnv();
		move();
		
		if (state == agentState.blackbox_found) {
			/* tell others */
			for (Agent agent : commNet.getAdjacent(this)) {
				NetworkAgent netAgent = (NetworkAgent) agent;
				netAgent.addToMessageQueue(new MsgBlackboxFound(this, agent,
						null));
			}
			
		} else {
			/* tell others current direction */
			for (Agent agent : commNet.getAdjacent(this)) {
				NetworkAgent netAgent = (NetworkAgent) agent;
				netAgent.addToMessageQueue(new MsgCurrentDirection(this, agent,
						directionAngle));
			}
		}
		prevState = state;
		defaultStepEnd();
	}
	
	private void processMessageQueue() {
		agentMimicMoveScan.clear();
		Message msg = popMessage();
		while (msg != null) {
			switch (msg.getType()) {
			case Current_Direction:
				agentMimicMoveScan.add((double)msg.getData());
				break;
			case Location:
				break;
			case Blackbox_found:
				/* This agent also knows where the blackbox is */
				this.state = agentState.blackbox_found;
			default:
				break;
			}
			msg = popMessage();
		}
	}

	private void move() {
		double speed = scenario.maxMoveDistance;
		
		/* check for obstacles */
		for (FieldDistancePair field : surroundingFields) {
			if (field.fieldType == FieldType.Obstacle) {
				double angle = SpatialMath.calcAngleFor2DMovement(space,
						currentLocation, new NdPoint(field.x, field.y));
				obstacles.add(obstacles.new InputPair(angle, field.distance));
			}
		}

		if (state == agentState.exploring) {
			agentMimicMoveScan.add(directionAngle);
			
//			agentMimicMoveScan.add(3*Math.PI/4);
			ScanCircle resulting = ScanCircle.merge(8, 0.12, agentAppealingScan, agentRepellingScan, agentMimicMoveScan, obstacles);
			
			directionAngle = resulting.getMovementAngle();

			currentLocation = space
					.moveByVector(this, speed, directionAngle, 0);

			if (consecutiveMoveCount >= scenario.rndConsecutiveMoves) {
				consecutiveMoveCount = 1;
			} else {
				consecutiveMoveCount++;
			}

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
		exploredArea.getFieldsRadial(currentLocation, scenario.perceptionScope);
		/* Pheromone scan */

		for (Agent agent : commNet.getAdjacent(this)) {
			
			switch (agent.getAgentType()) {
			case EXPL_AgentAvoiderMimicDirectionComm:
				double angle = SpatialMath.calcAngleFor2DMovement(space,
						currentLocation, space.getLocation(agent));
				double distance = space.getDistance(space.getLocation(this),
						space.getLocation(agent));
				
				agentRepellingScan.add(agentRepellingScan.new InputPair(angle, distance));
				agentAppealingScan.add(agentAppealingScan.new InputPair(angle, distance));
				break;
			default:
				break;
			}
		}
	}


	@Override
	public String getName() {
		return "BB_AgentAvoiderMimicDirectionComm_" + agentId;
	}

	@Override
	public AgentType getAgentType() {
		return AgentType.EXPL_AgentAvoiderMimicDirectionComm;
	}

}
