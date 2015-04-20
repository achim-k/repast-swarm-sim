package swarm_sim.exploration;

import java.util.List;

import org.jgap.Chromosome;
import org.jgap.Gene;

import repast.simphony.context.Context;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.DisplayAgent;
import swarm_sim.ScanCircle;
import swarm_sim.SectorMap;
import swarm_sim.communication.Message;
import swarm_sim.communication.MsgBlackboxFound;
import swarm_sim.communication.MsgSectorValues;
import swarm_sim.communication.NetworkAgent;
import swarm_sim.learning.GA;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.CircleScan;

public class AvoidAppealMimicMemory extends DefaultExplorationAgent implements
	Agent, DisplayAgent {

    int binCount = 8;

    SectorMap map = new SectorMap(space.getDimensions(), 40, 40, 1);
    
    CircleScan agentRepell = new CircleScan(binCount, 2, 1, 10000, 1, 0, -1, 0,
	    0.8 * scenario.commScope);
    CircleScan agentAppeal = new CircleScan(binCount, 1, 1, 100, 1, 1, 0,
	    0.8 * scenario.commScope, 1 * scenario.commScope);
    CircleScan agentMimic = new CircleScan(binCount, 1, 1, 100, 1, 1, 0,
	    0.8 * scenario.commScope, 1 * scenario.commScope);

    CircleScan memoryFollow = new CircleScan(binCount, 2, 1, 1000, 1, 1, 2, 0,
	    80);

    public AvoidAppealMimicMemory(Context<Agent> context) {
	super(context);
    }

    public AvoidAppealMimicMemory(Context<Agent> context,
	    Chromosome c) {
	super(context);
	Gene genes[] = c.getGenes();
	agentRepell.setMergeWeight((double)genes[GA.RepellIndex].getAllele());
	agentAppeal.setMergeWeight((double)genes[GA.AppealIndex].getAllele());
	agentMimic.setMergeWeight((double)genes[GA.MimicIndex].getAllele());
	memoryFollow.setMergeWeight((double)genes[GA.MemoryIndex].getAllele());
    }

    public void step() {
	defaultStepStart();
	processMessageQueue();
	scanEnv();
	move();
	prevState = state;
	sendMessages();
	defaultStepEnd();
    }

    public void move() {
	AngleSegment moveCircle = new AngleSegment(-Math.PI, Math.PI);
	List<AngleSegment> moveCircleFree = moveCircle
		.filterSegment(collisionAngleFilter.getFilterSegments());

	
	memoryFollow.clear();
//	dir.add(0.2, 1);
//	dir.add(-Math.PI + 0.2, 9);
	
	
	map.setPosition(currentLocation);
	List<Integer[]> closeUnfilledSectors = map.getCloseUnfilledSectors(5);
	for (Integer[] d : closeUnfilledSectors) {
	    double angle = SpatialMath.angleFromDisplacement(d[0], d[1]);
	    double distance = Math.sqrt(d[0]*d[0] + d[1]*d[1]);
	    scenario.movebins[ScanCircle.movementAngleToBin(angle, 8)]++;
//	    System.out.println(angle + "\t" + distance);
	    memoryFollow.add(angle, distance);
	}
	
//	memoryFollow.setValidSegments(moveCircleFree);
//	memoryFollow.calculateDirectionDistribution();
//	memoryFollow.normalize();
//	System.out.println(memoryFollow.getPrintable());
	

	CircleScan res = CircleScan.merge(binCount, 0.12, moveCircleFree,
		memoryFollow, agentRepell, agentAppeal, agentMimic);
//	System.out.println(res.getPrintable());
	directionAngle = res.getMovementAngle();
	if (directionAngle > -10) {
	    currentLocation = space.moveByVector(this,
		    scenario.maxMoveDistance, directionAngle, 0);
	}
    }

    private void scanEnv() {
	agentRepell.clear();
	agentAppeal.clear();

	for (Agent agent : commNet.getAdjacent(this)) {
	    switch (agent.getAgentType()) {
	    case EXPL_AvoidAppealMimicMemory:
		double angle = SpatialMath.calcAngleFor2DMovement(space,
			currentLocation, space.getLocation(agent));
		double distance = space.getDistance(space.getLocation(this),
			space.getLocation(agent));
		agentRepell.add(angle, distance);
		agentAppeal.add(angle, distance);
		break;
	    default:
		break;
	    }
	}

	/* scan environment for surrounding agents, pheromones, resources, ... */
	ContinuousWithin<Agent> withinQuery = new ContinuousWithin<Agent>(
		space, this, scenario.perceptionScope);
	for (Agent agent : withinQuery.query()) {
	    switch (agent.getAgentType()) {
	    case EXPL_AvoidAppealMimicMemory:
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
    
    private void processMessageQueue() {
	agentMimic.clear();
	
	Message msg = popMessage();
	while (msg != null) {
	    switch (msg.getType()) {
	    case SectorMap:
		Object data[] = (Object[]) msg.getData();
		SectorMap s = (SectorMap) data[0];
		map.merge(s);
		SectorMap targetSector = (SectorMap) data[2];
		if (targetSector.equals(map.getTargetSector())) {
		    NdPoint targetSectorCenter = s
			    .getSectorCenter(targetSector);
		    if (space.getDistance(currentLocation, targetSectorCenter) > space
			    .getDistance((NdPoint) data[1], targetSectorCenter))
			map.chooseNewTargetSector();
		}
		break;
	    case Current_Direction:
		double distance = space.getDistance(currentLocation,
			space.getLocation(msg.getSender()));
		agentMimic.add((double) msg.getData(), distance);
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

    private void sendMessages() {
	for (Agent agent : commNet.getAdjacent(this)) {
	    NetworkAgent netAgent = (NetworkAgent) agent;

	    if (state == agentState.blackbox_found)
		netAgent.addToMessageQueue(new MsgBlackboxFound(this, agent,
			null));
	    else {
		Object data[] = new Object[3];
		data[0] = map;
		data[1] = currentLocation;
		data[2] = map.getTargetSector();
		netAgent.addToMessageQueue(new MsgSectorValues(this, agent,
			data));
	    }

	}
    }

    @Override
    public String getName() {
	return "AvoidAppealMimicMemory_" + agentId;
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.EXPL_AvoidAppealMimicMemory;
    }
}
