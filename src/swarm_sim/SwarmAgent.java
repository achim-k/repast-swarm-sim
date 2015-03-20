package swarm_sim;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.valueLayer.ContinuousValueLayer;
import repast.simphony.query.space.continuous.ContinuousWithin;
import swarm_sim.neural.ActorCritic;

import com.vividsolutions.jts.geom.Geometry;

public class SwarmAgent implements Agent {

	private String name;
	private Context<Agent> context;
	private ContinuousSpace<Agent> spaceContinuous;
	private ContinuousValueLayer layerExploredArea;
	private NdPoint currentLocation;
	
	private double perceptionScope = 10; // in m
	private PrintWriter writer;

	private static int agentNr = 1;
	
	private ActorCritic actorCritic;
	private int currentTick = 0;
	
	
	public SwarmAgent(Context<Agent> context, PrintWriter writer, ActorCritic actorCritic) {
		this.context = context;
		this.spaceContinuous = (ContinuousSpace<Agent>)context.getProjection("space_continuous");
//		this.layerExploredArea = (ContinuousValueLayer)context.getProjection("layer_explored_area");
		
		this.writer = writer;
		this.name = "SwarmAgent #" + agentNr++;
		
		this.actorCritic = actorCritic;
	}
	
	public void step() {
//		System.out.println(getName());
		double moveX = RandomHelper.nextDoubleFromTo(-1, 1);
		double moveY = RandomHelper.nextDoubleFromTo(-1, 1);
//		currentLocation = spaceContinuous.moveByDisplacement(this, moveX, moveY);
		
		currentLocation = spaceContinuous.getLocation(this);
		List<ScanData> scanData = scanEnvironment();
		double direction = decideMoveDirection(scanData);
		move(direction);
		double reward = getReward();
		
		if(reward > 0)
			System.err.println(reward);
		actorCritic.trainSingle(reward, currentTick);
	}
	
	private double decideMoveDirection(List<ScanData> scanData) {
		currentTick ++;
		ScanData pheromones = scanData.get(0);
		
		double d[] = pheromones.getData();
		List<Double> input = new ArrayList<Double>();
		for (int i = 0; i < d.length; i++) {
			input.add(d[i]);
		}
		
		return actorCritic.getOutput(input, currentTick);
	}
	
	private void move(double direction) {
		currentLocation = spaceContinuous.moveByVector(this, 1, direction, 0);	
	}

	public List<ScanData> scanEnvironment() {
		List<ScanData> scanData = new ArrayList<ScanData>();
		ScanData otherAgents = new ScanData(8);
		ScanData pheromoneScan = new ScanData(8);
		
		ContinuousWithin<Agent> withinPerceptionScope = new ContinuousWithin<Agent>(context, this, perceptionScope);
		for(Agent agent : withinPerceptionScope.query()){
			switch (agent.getAgentType()) {
			case SwarmAgent:
				double angle = SpatialMath.calcAngleFor2DMovement(spaceContinuous, currentLocation, spaceContinuous.getLocation(agent));
				double distance = spaceContinuous.getDistance(currentLocation, spaceContinuous.getLocation(agent));
				otherAgents.addData(angle, distance);
				
				break;
			case Pheromone:
				double angleP = SpatialMath.calcAngleFor2DMovement(spaceContinuous, currentLocation, spaceContinuous.getLocation(agent));
				double distanceP = spaceContinuous.getDistance(currentLocation, spaceContinuous.getLocation(agent));
				pheromoneScan.addData(angleP, distanceP);
				
				break;
			case ControllerAgent:
			default:
				break;
			}
		}
		//System.out.println(otherAgents.getPrintable("A"));
//		pheromoneScan.normalize();
//		System.out.println(pheromoneScan.getPrintable("P"));
		double data[] = pheromoneScan.getData();
		String s = "";
		for (int i = 0; i < data.length; i++) {
			s += data[i] + ",";
		}
		writer.println(s);
//		System.out.println(s);
		
		scanData.add(pheromoneScan);
		return scanData;
	}
	
	public double getReward() {
		double collectionRange = 0.5;
		double reward = 0;
		ContinuousWithin<Agent> withinPerceptionScope = new ContinuousWithin<Agent>(context, this, collectionRange);
		for(Agent agent : withinPerceptionScope.query()){
			switch (agent.getAgentType()) {
			case Pheromone:
				reward += 1;
				this.context.remove(agent);
				break;
			case ControllerAgent:
			case SwarmAgent:
			default:
				break;
			}
		}
		return reward;
	}
	
	
	public NdPoint getLocation() {
		return this.currentLocation;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public AgentType getAgentType() {
		// TODO Auto-generated method stub
		return Agent.AgentType.SwarmAgent;
	}

}
