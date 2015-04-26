package swarm_sim;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jgap.Chromosome;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.util.ContextUtils;
import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.VSpatial;
import swarm_sim.AdvancedGridValueLayer.FieldDistancePair;
import swarm_sim.AdvancedGridValueLayer.FieldType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.exploration.ExplorationStrategy;
import swarm_sim.exploration.RandomEXPLStrategy;
import swarm_sim.foraging.StateCommFAStrategy;
import swarm_sim.perception.AngleFilter;
import swarm_sim.perception.AngleSegment;

public class Agent implements IAgent, IDisplayAgent, INetworkAgent {

    public enum AgentState {
	wander, acquire, deliver
    }

    static int agentCount = 0;

    /* Identity & state */
    int agentId;
    AgentState state = AgentState.wander;
    AgentState prevState = AgentState.wander;

    /* location/movement */
    NdPoint currentLocation;
    double directionAngle = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);
    List<FieldDistancePair> surroundingFields = new ArrayList<>();
    AngleFilter collisionAngleFilter = new AngleFilter(1);

    /* General */
    Scenario scenario;
    Context<IAgent> context;
    Network<IAgent> commNet;
    ContinuousSpace<IAgent> space;
    AdvancedGridValueLayer exploredArea;

    /* Learning */
    Chromosome chrom;

    /* Exploration + Foraging strategies */
    ExplorationStrategy explStrategy;
    ForagingStrategy faStrategy;

    /* Communication */
    protected List<Message> messageQueue = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public Agent(Context<IAgent> context, Chromosome chrom) {
	this.chrom = chrom;

	/* Get context, network, value layer etc. */
	this.context = ContextUtils.getContext(this);
	this.space = (ContinuousSpace<IAgent>) context.getProjection(
		ContinuousSpace.class, "space_continuous");
	this.commNet = (Network<IAgent>) context.getProjection(Network.class,
		"network_comm");
	this.exploredArea = (AdvancedGridValueLayer) context
		.getValueLayer("layer_explored");
	this.scenario = Scenario.getInstance();
	this.agentId = ++agentCount;

	/* TODO: Set exploration and foraging strategy */
	explStrategy = new RandomEXPLStrategy(chrom, context, this);
	faStrategy = new StateCommFAStrategy(chrom, context, this);
    }

    public void step() {
	if (currentLocation == null)
	    currentLocation = space.getLocation(this);

	rcvMessages();
	scanEnv();
	move();
	sendMessages();

	faStrategy.clear();
	explStrategy.clear();

	if (prevState != state) {
	    if (prevState == AgentState.wander)
		explStrategy.reset();
	    if (state == AgentState.wander)
		faStrategy.reset();
	}

	prevState = state;
    }

    private void rcvMessages() {
	Message msg = popMessage();

	while (msg != null) {
	    state = explStrategy.processMessage(msg, state);
	    state = faStrategy.processMessage(msg, state);
	    msg = popMessage();
	}
    }

    private void sendMessages() {
	for (IAgent netAgent : commNet.getAdjacent(this)) {
	    explStrategy.sendMessage((INetworkAgent) netAgent, state);
	    faStrategy.sendMessage((INetworkAgent) netAgent, state);
	}
    }

    public void scanEnv() {
	collisionAngleFilter.clear();

	surroundingFields = exploredArea.getFieldsRadial(currentLocation,
		scenario.perceptionScope);

	/*
	 * loop through surrounding fields and check for obstacles/set field as
	 * explored
	 */
	for (FieldDistancePair field : surroundingFields) {
	    if (field.fieldType == FieldType.Obstacle) {
		if (field.distance <= scenario.maxMoveDistance + 1
			&& field.distance > 0) {
		    double angle = SpatialMath.calcAngleFor2DMovement(space,
			    currentLocation, new NdPoint(field.x + .5,
				    field.y + .5));
		    collisionAngleFilter.add(field.distance, angle);
		}
	    } else {
		if (field.value == 0)
		    scenario.exploredAreaCount++;
		else
		    scenario.redundantExploredAreaCount++;
	    }
	    exploredArea.set(field.value + 1, field.x, field.y);
	}

	/* scan environment for surrounding agents, pheromones, resources, ... */
	ContinuousWithin<IAgent> withinQuery = new ContinuousWithin<IAgent>(
		space, this, scenario.perceptionScope);
	Iterator<IAgent> agentIter = withinQuery.query().iterator();

	while (agentIter.hasNext()) {
	    IAgent agent = agentIter.next();

	    /* Other agent → avoid collisions */
	    if (agent.getAgentType() == getAgentType()) {

		double distance = space.getDistance(space.getLocation(this),
			space.getLocation(agent));
		if (distance > 0 && distance <= scenario.maxMoveDistance + 1) {
		    double angle = SpatialMath.calcAngleFor2DMovement(space,
			    currentLocation, space.getLocation(agent));
		    collisionAngleFilter.add(distance, angle);
		}
	    }

	    if (state == AgentState.wander)
		state = explStrategy.processPerceivedAgent(agent,
			!agentIter.hasNext());

	    /*
	     * not in else statement, so perceived agent can be processed even
	     * when state changed
	     */
	    if (state != AgentState.wander)
		state = faStrategy.processPerceivedAgent(agent,
			!agentIter.hasNext());
	}

	if (state != AgentState.wander)
	    state = faStrategy.checkState();
    }

    public void move() {
	AngleSegment moveCircle = new AngleSegment(-Math.PI, Math.PI);
	List<AngleSegment> collisionFreeSegments = moveCircle
		.filterSegment(collisionAngleFilter.getFilterSegments());

	if (state == AgentState.wander) {
	    directionAngle = explStrategy
		    .makeDirectionDecision(collisionFreeSegments);
	} else {
	    directionAngle = faStrategy
		    .makeDirectionDecision(collisionFreeSegments);
	}

	if (directionAngle >= -Math.PI)
	    currentLocation = space.moveByVector(this,
		    scenario.maxMoveDistance, directionAngle, 0);
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.SwarmAgent;
    }

    @Override
    public Color getColor() {
	Color retColor = Color.BLUE;
	switch (state) {
	case wander:
	    retColor = Color.BLUE;
	    break;
	case acquire:
	    retColor = Color.YELLOW;
	    break;
	case deliver:
	    retColor = Color.RED;
	    break;
	default:
	    retColor = Color.BLUE;
	    break;
	}
	return retColor;
    }

    @Override
    public String getName() {
	return "Agent_" + agentId;
    }

    @Override
    public VSpatial getShape(ShapeFactory2D shapeFactory) {
	return shapeFactory.createCircle(3, 16);
    }

    @Override
    public void pushMessage(Message msg) {
	messageQueue.add(msg);
	scenario.messagesSent++;
    }

    private Message popMessage() {
	if (messageQueue.size() <= 0)
	    return null;
	else {
	    Message msg = messageQueue.get(0);
	    if (msg.getTick() >= (int) RunEnvironment.getInstance()
		    .getCurrentSchedule().getTickCount())
		return null;
	    messageQueue.remove(0);
	    return msg;
	}
    }
}
