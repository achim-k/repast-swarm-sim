package swarm_sim.exploration;

import java.util.List;

import org.jgap.Chromosome;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.IAgent;
import swarm_sim.IAgent.AgentType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.CircleScan;

public class RandomEXPLStrategy extends ExplorationStrategy {

    int segmentCount = 8;
    int consecutiveMoveCount = 0;
    double directionAngle = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);

    public RandomEXPLStrategy(Chromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
    }

    @Override
    protected AgentState processMessage(Message msg, AgentState currentState) {
	// No Communication here

	return currentState;
    }

    @Override
    protected void sendMessage(INetworkAgent agentInRange,
	    AgentState currentState) {
	// No Communication here
    }

    @Override
    protected AgentState processPerceivedAgent(IAgent agent, boolean isLast) {
	if (agent.getAgentType() == AgentType.Resource)
	    return AgentState.acquire;

	return AgentState.wander;
    }

    @Override
    protected double makeDirectionDecision(
	    List<AngleSegment> collisionFreeSegments) {

	if (consecutiveMoveCount < scenario.rndConsecutiveMoves) {
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
	CircleScan res = CircleScan.merge(segmentCount, 0,
		collisionFreeSegments);
	directionAngle = res.getMovementAngle();
	return directionAngle;
    }

    @Override
    protected void reset() {
	consecutiveMoveCount = 0;
    }

    @Override
    protected void clear() {
    }
}
