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
import swarm_sim.Strategy.MessageTypeRegisterPair;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.exploration.ExplorationStrategy;
import swarm_sim.exploration.MemoryComplexExplStrategy;
import swarm_sim.exploration.RandomEXPLStrategy;
import swarm_sim.foraging.ForagingStrategy;
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
    Configuration config;
    DataCollection data;
    
    Context<IAgent> context;
    Network<IAgent> commNet;
    ContinuousSpace<IAgent> space;
    AdvancedGridValueLayer exploredArea;

    /* Learning */
    Chromosome chrom;

    /* Exploration + Foraging strategies */
    ExplorationStrategy explStrategy;
    ForagingStrategy faStrategy;

    List<MessageTypeRegisterPair> faStrategyMessages, explStrategyMessages;

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
	this.config = Configuration.getInstance();
	this.data = DataCollection.getInstance();
	this.agentId = ++agentCount;

	/* Set exploration and foraging strategy */
	if(config.explStrat.equalsIgnoreCase("Random"))
	    this.explStrategy = new RandomEXPLStrategy(chrom, context, this);
	else if(config.explStrat.equalsIgnoreCase("MemoryComm"))
	    this.explStrategy = new MemoryComplexExplStrategy(chrom, context, this);
	
	if(config.foragingStrat.equalsIgnoreCase("StateCommunication"))
	    this.faStrategy = new StateCommFAStrategy(chrom, context, this);
	
	CommunicationType allowedCommunicationTypes[] = new CommunicationType[] {
		CommunicationType.State, CommunicationType.MapOrTargets };

	faStrategyMessages = faStrategy
		.getMessageTypesToRegister(allowedCommunicationTypes);
	explStrategyMessages = explStrategy
		.getMessageTypesToRegister(allowedCommunicationTypes);
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

	    for (MessageTypeRegisterPair mrp : faStrategyMessages) {
		if (msg.getType() == mrp.msgType) {
		    for (AgentState as : mrp.states) {
			if (state == as) {
			    state = faStrategy.processMessage(prevState, state,
				    msg, msg == null);
			    break;
			}
		    }
		    break;
		}
	    }
	    msg = popMessage();
	}

	state = explStrategy.processMessage(prevState, state, null, true);
	state = faStrategy.processMessage(prevState, state, null, true);
    }

    private void sendMessages() {
	int x = (int) RunEnvironment.getInstance().getCurrentSchedule()
		.getTickCount()
		% config.commFreq;

	if (x == 0) {
	    for (IAgent netAgent : commNet.getAdjacent(this)) {
		explStrategy.sendMessage(prevState, state,
			(INetworkAgent) netAgent);
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
		    collisionAngleFilter.add(field.distance, angle);
		}
	    } else {
		if (field.value == 0)
		    data.fieldsExplored++;
		else
		    data.fieldsRedundantlyExplored++;
	    }
	    exploredArea.set(field.value + 1, field.x, field.y);
	}

	/* scan environment for surrounding agents, pheromones, resources, ... */
	ContinuousWithin<IAgent> withinQuery = new ContinuousWithin<IAgent>(
		space, this, config.perceptionScope);
	Iterator<IAgent> agentIter = withinQuery.query().iterator();

	while (agentIter.hasNext()) {
	    IAgent agent = agentIter.next();

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

	if (directionAngle >= -Math.PI)
	    currentLocation = space.moveByVector(this,
		    config.maxMoveDistance, directionAngle, 0);
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
