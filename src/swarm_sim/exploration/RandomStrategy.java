package swarm_sim.exploration;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.IAgent;
import swarm_sim.IAgent.AgentType;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.ScanMoveDecision;

public class RandomStrategy extends ExplorationStrategy {

    int segmentCount = 8;
    int consecutiveMoveCount = 0;
    double directionAngle = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);

    ScanMoveDecision smd;

    public RandomStrategy(IChromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
    }

    @Override
    protected AgentState processMessage(AgentState prevState,
	    AgentState currentState, Message msg, boolean isLast) {
	// No Communication here
	
	smd = new ScanMoveDecision(config.segmentCount, config.k,
		config.distanceFactor, config.initProb);

	return currentState;
    }

    @Override
    protected void sendMessage(AgentState prevState, AgentState currentState,
	    INetworkAgent agentInRange) {
	// No Communication here
    }

    @Override
    protected AgentState processPerceivedAgent(AgentState prevState,
	    AgentState currentState, IAgent agent, boolean isLast) {
	if (agent.getAgentType() == AgentType.Resource)
	    return AgentState.acquire;

	return AgentState.wander;
    }

    @Override
    protected double makeDirectionDecision(AgentState prevState,
	    AgentState currentState, List<AngleSegment> collisionFreeSegments) {

	if (consecutiveMoveCount < config.consecutiveMoves) {
	    // Continue to go in same direction, if possible
	    boolean moveAllowed = false;
	    for (AngleSegment as : collisionFreeSegments) {
		if (as.start <= directionAngle && as.end >= directionAngle) {
		    moveAllowed = true;
		    break;
		}
	    }

	    if (moveAllowed) {
		consecutiveMoveCount++;
		return directionAngle;
	    }
	}

	/* Choose random direction */
	consecutiveMoveCount = 0;
	smd.setValidSegments(collisionFreeSegments);
	// smd.calcProbDist();
	smd.normalize();
	directionAngle = smd.getMovementAngle();
	return directionAngle;
    }

    @Override
    protected void reset() {
	consecutiveMoveCount = 0;
    }

    @Override
    protected void clear() {
	smd.clear();
    }

    @Override
    protected List<MessageTypeRegisterPair> getMessageTypesToRegister(
	    CommunicationType allowedCommTypes[]) {
	return new ArrayList<MessageTypeRegisterPair>();
    }
}
