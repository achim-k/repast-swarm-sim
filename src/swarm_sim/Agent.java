package swarm_sim;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.VSpatial;
import swarm_sim.AdvancedGridValueLayer.FieldDistancePair;
import swarm_sim.AdvancedGridValueLayer.FieldType;
import swarm_sim.Strategy.MessageTypeRegisterPair;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.exploration.ComplexCommStrategy;
import swarm_sim.exploration.ComplexMemoryStrategy;
import swarm_sim.exploration.ComplexQTMemoryCommStrategy;
import swarm_sim.exploration.ExplorationStrategy;
import swarm_sim.exploration.QTMemoryCommStrategy;
import swarm_sim.exploration.RandomStrategy;
import swarm_sim.foraging.ForagingStrategy;
import swarm_sim.foraging.GoalCommunication;
import swarm_sim.foraging.NoCommStrategy;
import swarm_sim.foraging.PheromoneStrategy;
import swarm_sim.foraging.StateCommStrategy;
import swarm_sim.perception.AngleFilter;
import swarm_sim.perception.AngleSegment;

public class Agent extends AbstractAgent implements IDisplayAgent,
	INetworkAgent {

    public enum AgentState {
	wander, acquire, deliver, failure
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
    Configuration config;
    DataCollection data;

    Context<AbstractAgent> context;
    Network<AbstractAgent> commNet;
    ContinuousSpace<AbstractAgent> space;
    AdvancedGridValueLayer exploredArea;

    /* Learning */
    IChromosome chrom;

    /* Exploration + Foraging strategies */
    ExplorationStrategy explStrategy;
    ForagingStrategy faStrategy;
    boolean isForagingScenario;

    List<MessageTypeRegisterPair> faStrategyMessages, explStrategyMessages;

    /* Communication */
    protected List<Message> messageQueue = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public Agent(Context<AbstractAgent> context, IChromosome chrom) {
	this.chrom = chrom;

	/* Get context, network, value layer etc. */
	this.context = context;
	this.space = (ContinuousSpace<AbstractAgent>) context.getProjection(
		ContinuousSpace.class, "space_continuous");
	this.commNet = (Network<AbstractAgent>) context.getProjection(
		Network.class, "network_comm");
	this.exploredArea = (AdvancedGridValueLayer) context
		.getValueLayer("layer_explored");
	this.config = Configuration.getInstance();
	this.data = DataCollection.getInstance();
	this.agentId = ++agentCount;

	/* Set exploration and foraging strategy */
	if (config.explStrat.equalsIgnoreCase("R"))
	    this.explStrategy = new RandomStrategy(chrom, context, this);
	else if (config.explStrat.equalsIgnoreCase("CM"))
	    this.explStrategy = new ComplexMemoryStrategy(chrom, context, this);
	else if (config.explStrat.equalsIgnoreCase("CC"))
	    this.explStrategy = new ComplexCommStrategy(chrom, context, this);
	else if (config.explStrat.equalsIgnoreCase("MC"))
	    this.explStrategy = new QTMemoryCommStrategy(chrom, context, this);
	else if (config.explStrat.equalsIgnoreCase("CMC"))
	    this.explStrategy = new ComplexQTMemoryCommStrategy(chrom, context,
		    this);
	// else if (config.explStrat //Old version using grid map
	// .equalsIgnoreCase("ComplexMemoryCommunication"))
	// this.explStrategy = new ComplexMemoryCommStrategy(chrom, context,
	// this);
	// else if (config.explStrat.equalsIgnoreCase("MemoryCommunication"))
	// //Old version using grid map
	// this.explStrategy = new MemoryCommStrategy(chrom, context, this);

	if (config.foragingStrat.equalsIgnoreCase("NC"))
	    this.faStrategy = new NoCommStrategy(chrom, context, this);
	else if (config.foragingStrat.equalsIgnoreCase("SC"))
	    this.faStrategy = new StateCommStrategy(chrom, context, this);
	else if (config.foragingStrat.equalsIgnoreCase("GC"))
	    this.faStrategy = new GoalCommunication(chrom, context, this);
	else if (config.foragingStrat.equalsIgnoreCase("PC"))
	    this.faStrategy = new PheromoneStrategy(chrom, context, this);

	CommunicationType allowedCommunicationTypes[] = new CommunicationType[] {
		CommunicationType.State, CommunicationType.MapOrTargets,
		CommunicationType.Location,
		CommunicationType.TargetOrDirection,
		CommunicationType.Presence, CommunicationType.Pheromone };

	faStrategyMessages = faStrategy
		.getMessageTypesToRegister(allowedCommunicationTypes);
	explStrategyMessages = explStrategy
		.getMessageTypesToRegister(allowedCommunicationTypes);

	isForagingScenario = config.resourceCount > 0;

    }

    public void step() {
	if (state == AgentState.failure)
	    return;

	if (currentLocation == null)
	    currentLocation = space.getLocation(this);

	/* check if agent will fail */
	double failRand = Math.random();
	if (failRand < config.failureProbability) {
	    if (state == AgentState.deliver) {
		/* drop pheromone */
		Pheromone p = new Pheromone();
		context.add(p);
		space.moveTo(p, currentLocation.getX(), currentLocation.getY());
	    }
	    state = AgentState.failure;
	    return;
	}

	long start = System.nanoTime();
	rcvMessages();
	data.execTimeProcessMessages += System.nanoTime() - start;

	start = System.nanoTime();
	scanEnv();
	data.execTimeScanEnv += System.nanoTime() - start;

	start = System.nanoTime();
	move();
	data.execTimeMoveDecision += System.nanoTime() - start;

	start = System.nanoTime();
	sendMessages();
	data.execTimeSendMessages += System.nanoTime() - start;

	faStrategy.clear();
	explStrategy.clear();

	if (prevState != state) {
	    if (prevState == AgentState.wander)
		explStrategy.reset();
	    if (state == AgentState.wander)
		faStrategy.reset();
	}

	prevState = state;

	switch (state) {
	case wander:
	    data.wanderingAgents++;
	    break;
	case acquire:
	    data.acquiringAgents++;
	    break;
	case deliver:
	    data.deliveringAgents++;
	    break;
	default:
	    break;
	}
    }

    private void rcvMessages() {
	Message msg = popMessage();

	while (msg != null) {
	    for (MessageTypeRegisterPair mrp : explStrategyMessages) {
		if (msg.getType() == mrp.msgType) {
		    for (AgentState as : mrp.states) {
			if (state == as) {
			    state = explStrategy.processMessage(prevState,
				    state, msg, msg == null);
			    break;
			}
		    }
		    break;
		}
	    }

	    if (isForagingScenario) {
		for (MessageTypeRegisterPair mrp : faStrategyMessages) {
		    if (msg.getType() == mrp.msgType) {
			for (AgentState as : mrp.states) {
			    if (state == as) {
				state = faStrategy.processMessage(prevState,
					state, msg, msg == null);
				break;
			    }
			}
			break;
		    }
		}
	    }

	    msg = popMessage();

	}

	state = explStrategy.processMessage(prevState, state, null, true);
	if (isForagingScenario)
	    state = faStrategy.processMessage(prevState, state, null, true);
    }

    private void sendMessages() {
	int x = (int) RunEnvironment.getInstance().getCurrentSchedule()
		.getTickCount()
		% config.commFreq;

	if (x == 0) {
	    for (AbstractAgent netAgent : commNet.getAdjacent(this)) {
		explStrategy.sendMessage(prevState, state,
			(INetworkAgent) netAgent);

		if (!isForagingScenario)
		    continue;

		faStrategy.sendMessage(prevState, state,
			(INetworkAgent) netAgent);
	    }
	}
    }

    public void scanEnv() {
	collisionAngleFilter.clear();

	surroundingFields = exploredArea.getFieldsRadial(currentLocation,
		config.perceptionScope);

	/*
	 * loop through surrounding fields and check for obstacles/set field as
	 * explored
	 */
	for (FieldDistancePair field : surroundingFields) {
	    if (field.fieldType == FieldType.Obstacle) {
		if (field.distance <= config.maxMoveDistance + .5
			&& field.distance > 0) {
		    double angle = SpatialMath.calcAngleFor2DMovement(space,
			    currentLocation, new NdPoint(field.x + .5,
				    field.y + .5));
		    collisionAngleFilter.add(field.distance, angle, 0.5);
		}
		continue;
	    } else {
		if (field.value == 0)
		    data.fieldsExplored++;
		else
		    data.fieldsRedundantlyExplored++;
	    }
	    exploredArea.set(field.value + 1, field.x, field.y);
	}

	/* scan environment for surrounding agents, pheromones, resources, ... */
	ContinuousWithin<AbstractAgent> withinQuery = new ContinuousWithin<AbstractAgent>(
		space, this, config.perceptionScope);
	Iterator<AbstractAgent> agentIter = withinQuery.query().iterator();

	while (agentIter.hasNext()) {
	    AbstractAgent agent = agentIter.next();

	    /* Other agent → avoid collisions */
	    if (agent.getAgentType() == getAgentType()) {

		double distance = space.getDistance(space.getLocation(this),
			space.getLocation(agent));
		if (distance > 0 && distance <= config.maxMoveDistance + 1) {
		    double angle = SpatialMath.calcAngleFor2DMovement(space,
			    currentLocation, space.getLocation(agent));
		    collisionAngleFilter.add(distance, angle);
		}
	    }

	    if (state == AgentState.wander)
		state = explStrategy.processPerceivedAgent(prevState, state,
			agent, !agentIter.hasNext());

	    /*
	     * not in else statement, so perceived agent can be processed even
	     * when state changed
	     */
	    if (state != AgentState.wander)
		state = faStrategy.processPerceivedAgent(prevState, state,
			agent, !agentIter.hasNext());
	}

	if (state != AgentState.wander)
	    state = faStrategy.checkState(prevState, state);
    }

    public void move() {
	AngleSegment moveCircle = new AngleSegment(-Math.PI, Math.PI);
	List<AngleSegment> collisionFreeSegments = moveCircle
		.filterSegment(collisionAngleFilter.getFilterSegments());

	if (state == AgentState.wander) {
	    directionAngle = explStrategy.makeDirectionDecision(prevState,
		    state, collisionFreeSegments);
	} else {
	    directionAngle = faStrategy.makeDirectionDecision(prevState, state,
		    collisionFreeSegments);
	}

	if (directionAngle >= -Math.PI) {
	    currentLocation = space.moveByVector(this, config.maxMoveDistance,
		    directionAngle, 0);
	    data.moveCount++;
	}
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.SwarmAgent;
    }

    @Override
    public boolean hasFailed() {
	return state == AgentState.failure;
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
	case failure:
	    retColor = Color.LIGHT_GRAY;
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
	data.messageCount++;
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
