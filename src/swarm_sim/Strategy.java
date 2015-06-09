package swarm_sim;

import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.AdvancedGridValueLayer.FieldDistancePair;
import swarm_sim.Agent.AgentState;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.communication.Message.MessageType;
import swarm_sim.perception.PDDP;

public abstract class Strategy {

    public class MessageTypeRegisterPair {
	public MessageType msgType;
	public AgentState states[];

	public MessageTypeRegisterPair(MessageType msgType, AgentState[] states) {
	    super();
	    this.msgType = msgType;
	    this.states = states;
	}
    }

    protected Configuration config;
    protected DataCollection data;
    protected IChromosome chrom;
    protected ContinuousSpace<AbstractAgent> space;
    protected Context<AbstractAgent> context;
    protected Agent controllingAgent;

    /* Bug algorithm */
    private double dMin, dLeave;
    private boolean isWallFollowing = false, followRight = false;
    private NdPoint prevGoal;

    public Strategy(IChromosome chrom, Context<AbstractAgent> context,
	    Agent controllingAgent) {
	super();
	this.chrom = chrom;
	this.context = context;
	this.space = (ContinuousSpace<AbstractAgent>) context.getProjection(
		ContinuousSpace.class, "space_continuous");
	this.config = Configuration.getInstance();
	this.data = DataCollection.getInstance();
	this.controllingAgent = controllingAgent;
    }

    protected abstract List<MessageTypeRegisterPair> getMessageTypesToRegister(
	    CommunicationType allowedCommTypes[]);

    protected abstract AgentState processMessage(AgentState prevState,
	    AgentState currentState, Message msg, boolean isLast);

    protected abstract void sendMessage(AgentState prevState,
	    AgentState currentState, INetworkAgent agentInRange);

    protected abstract AgentState processPerceivedAgent(AgentState prevState,
	    AgentState currentState, AbstractAgent agent, boolean isLast);

    protected abstract double makeDirectionDecision(AgentState prevState,
	    AgentState currentState, PDDP pddp);

    protected abstract void handleObstacle(AgentState prevState,
	    AgentState currentState, FieldDistancePair obs);

    protected abstract void clear(); // After each step

    protected abstract void reset(); // After other strategy was chosen

    protected double motionToGoal(NdPoint goal, PDDP pddp) {
	double goalDirection = SpatialMath.calcAngleFor2DMovement(space,
		space.getLocation(controllingAgent), goal);
	int goalIndex = pddp.angleToSegmentIndex(goalDirection,
		config.segmentCount);
	double goalDistance = space.getDistance(
		space.getLocation(controllingAgent), goal);
	
	if(prevGoal == null || !prevGoal.equals(goal)) {
	    isWallFollowing = false;
	    prevGoal = goal;
	}
	    
	if (isWallFollowing && (goalDistance < dMin || (Math.abs(goalDistance - dMin) < 2 && pddp.segmentValid(goalIndex)))) {
	    isWallFollowing = false;
	}

	if (isWallFollowing) {
	    boolean obstacleFound = false;
	    int i = 0;
	    while (i < config.segmentCount) {
		if (!pddp.segmentValid(i))
		    break;
		i++;
	    }
	    int indexObstacle = i;
	    
	    if(indexObstacle == config.segmentCount) {
		isWallFollowing = false;
		return goalDirection;
	    }

	    if (followRight) {
		i = 1;
		while (i < config.segmentCount) {
		    int normIndex = indexObstacle - i;
		    if (normIndex < 0)
			normIndex += config.segmentCount;

		    if (pddp.segmentValid(normIndex))
			return pddp.randomSegmentDirection(normIndex);
		    i++;
		}
	    } else {

		i = 1;
		while (i < config.segmentCount) {
		    int normIndex = (indexObstacle + i) % config.segmentCount;

		    if (pddp.segmentValid(normIndex))
			return pddp.randomSegmentDirection(normIndex);
		    i++;
		}
	    }
	    return -100;
	} else {
	    if (pddp.segmentValid(goalIndex)) {
		return goalDirection;
	    }

	    /* Make decision in which direction to go */
	    isWallFollowing = true;
	    dMin = goalDistance;
	    int neighborIndex = 1;
	    int normIndex = 1;
	    while (neighborIndex < config.segmentCount) {
		normIndex = (goalIndex + neighborIndex) % config.segmentCount;
		if (pddp.segmentValid(normIndex)) {
		    followRight = false;
		    break;
		}

		normIndex = (goalIndex - neighborIndex);
		if (normIndex < 0)
		    normIndex += config.segmentCount;
		if (pddp.segmentValid(normIndex)) {
		    followRight = true;
		    break;
		}
		neighborIndex++;
	    }

	    if (neighborIndex == config.segmentCount) {
		isWallFollowing = false;
		return -100;
	    } else {
		return pddp.randomSegmentDirection(normIndex);
	    }
	}
    }
}
