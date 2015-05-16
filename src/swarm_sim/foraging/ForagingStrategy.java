package swarm_sim.foraging;

import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.IAgent;
import swarm_sim.IAgent.AgentType;
import swarm_sim.SectorMap;
import swarm_sim.Strategy;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.Scan;
import swarm_sim.perception.Scan.AttractionType;
import swarm_sim.perception.Scan.GrowingDirection;
import swarm_sim.perception.ScanMoveDecision;

public abstract class ForagingStrategy extends Strategy {

    protected ResourceTarget currentTarget;
    protected SectorMap map;

    protected double directionAngle = RandomHelper.nextDoubleFromTo(-Math.PI,
	    Math.PI);

    protected Scan scanResources = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, config.perceptionScope, 1,
	    100);
    protected Scan scanDeliverDirection = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 1E8, 1, 10);
    protected Scan scanCurrentTarget = new Scan(AttractionType.Attracting,
	    GrowingDirection.Inwards, 1, true, 0, 1E8, 1, 10);

    protected ScanMoveDecision smd;

    public class ResourceTarget {
	public int resourceCount;
	public NdPoint location;
	public SectorMap sector;
	public boolean isValid;

	public ResourceTarget(int resouceCount, NdPoint location,
		SectorMap sector) {
	    super();
	    this.resourceCount = resouceCount;
	    this.location = location;
	    this.sector = sector;
	    this.isValid = true;
	}

	public boolean isSameSector(SectorMap t) {
	    return t.equals(sector);
	}

	@Override
	public boolean equals(Object obj) {
	    if (obj.getClass() != ResourceTarget.class)
		return false;

	    ResourceTarget t = (ResourceTarget) obj;

	    if (t.sector.equals(this.sector)
		    && t.resourceCount == this.resourceCount)
		return true;

	    return false;
	}
    }

    protected int perceivedResourceCount = 0;

    public ForagingStrategy(IChromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);

	smd = new ScanMoveDecision(config.segmentCount, config.k,
		config.distanceFactor, config.initProb);

	int sectorsX = (int) (config.spaceWidth / config.perceptionScope);
	int sectorsY = (int) (config.spaceHeight / config.perceptionScope);

	if (sectorsX > config.spaceWidth)
	    sectorsX = config.spaceWidth;
	if (sectorsY > config.spaceHeight)
	    sectorsY = config.spaceHeight;

	map = new SectorMap(space.getDimensions(), sectorsX, sectorsY, 1);
    }

    public AgentState checkState(AgentState prevState, AgentState currentState) {
	NdPoint currentLocation = space.getLocation(controllingAgent);

	if (perceivedResourceCount > 0) {
	    /* Resources found */
	    if (currentTarget == null) {
		currentTarget = new ResourceTarget(perceivedResourceCount,
			null, map.getCurrentSector(currentLocation));
	    } else {
		if (currentTarget.isSameSector(map
			.getCurrentSector(currentLocation))) {
		    currentTarget.isValid = true;
		    currentTarget.resourceCount = perceivedResourceCount;
		} else {
		    currentTarget = new ResourceTarget(perceivedResourceCount,
			    null, map.getCurrentSector(currentLocation));
		}
	    }

	    return currentState;

	} else if (currentState == AgentState.acquire) {
	    /* Unset current target if the sector it is in has been reached */
	    if (currentTarget != null
		    && currentTarget.isValid
		    && currentTarget.isSameSector(map.getCurrentSector(space
			    .getLocation(controllingAgent)))) {
		currentTarget.isValid = false;
		currentTarget.resourceCount = 0;
	    }

	    if (currentTarget == null || !currentTarget.isValid) {
		return AgentState.wander;
	    }
	}

	return currentState;
    }

    @Override
    public AgentState processPerceivedAgent(AgentState prevState,
	    AgentState currentState, IAgent agent, boolean isLast) {
	NdPoint currentLocation = space.getLocation(controllingAgent);

	if (agent.getAgentType() == AgentType.Resource) {
	    perceivedResourceCount++;

	    if (currentState == AgentState.acquire) {
		double distance = space.getDistance(currentLocation,
			space.getLocation(agent));

		if (distance <= config.maxMoveDistance / 2) {
		    /* pick up that resource and go in deliver state */
		    context.remove(agent);
		    perceivedResourceCount--;
		    return AgentState.deliver;
		}

		double angle = SpatialMath.calcAngleFor2DMovement(space,
			currentLocation, space.getLocation(agent));
		scanResources.addInput(angle, distance);
	    }
	} else if (agent.getAgentType() == AgentType.Base) {
	    if (currentState == AgentState.deliver) {
		double distance = space.getDistance(currentLocation,
			space.getLocation(config.baseAgent));

		if (distance <= config.maxMoveDistance / 2) {
		    /* Deliver the resource */
		    data.deliveredResources++;
		    return AgentState.acquire;
		}
	    }
	}

	return currentState;
    }

    @Override
    public double makeDirectionDecision(AgentState prevState,
	    AgentState currentState, List<AngleSegment> collisionFreeSegments) {

	smd.setValidSegments(collisionFreeSegments);

	if (currentState == AgentState.acquire) {
	    if (!scanResources.isValid() && currentTarget != null
		    && currentTarget.isValid) {
		SectorMap currentSector = map.getCurrentSector(space
			.getLocation(controllingAgent));
		double direction = currentSector
			.getDirectionToSector(currentTarget.sector);
		scanCurrentTarget.addInput(direction);
	    }
	    smd.calcProbDist(scanResources, scanCurrentTarget);

	} else if (currentState == AgentState.deliver) {
	    double moveAngleToBase = SpatialMath.calcAngleFor2DMovement(space,
		    space.getLocation(controllingAgent),
		    space.getLocation(config.baseAgent));
	    scanDeliverDirection.addInput(moveAngleToBase);
	    smd.calcProbDist(scanDeliverDirection);
	} else {
	    System.err.println("state not existing: " + currentState);
	}

	smd.normalize();
	directionAngle = smd.getMovementAngle();
	return directionAngle;
    }

    @Override
    public void clear() {
	perceivedResourceCount = 0;
	scanResources.clear();
	scanDeliverDirection.clear();
	scanCurrentTarget.clear();
	smd.clear();
    }

    @Override
    public void reset() {
    }

}
