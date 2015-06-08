package swarm_sim.exploration;

import java.util.ArrayList;
import java.util.List;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import swarm_sim.AbstractAgent;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.PDDP;

public class RandomStrategy extends ExplorationStrategy {

    int segmentCount = 8;
    int consecutiveMoveCount = 0;
    double prevDirection = RandomHelper.nextDoubleFromTo(-Math.PI, Math.PI);

    public RandomStrategy(IChromosome chrom, Context<AbstractAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
    }

    @Override
    protected AgentState processMessage(AgentState prevState,
	    AgentState currentState, Message msg, boolean isLast) {
	// No Communication here
	return currentState;
    }

    @Override
    protected void sendMessage(AgentState prevState, AgentState currentState,
	    INetworkAgent agentInRange) {
	// No Communication here
    }

    @Override
    protected double makeDirectionDecision(AgentState prevState,
	    AgentState currentState, PDDP pddp) {

	if (consecutiveMoveCount < config.consecutiveMoves && prevDirection > -10) {
	    // Continue to go in same direction, if possible
	    boolean moveAllowed = pddp.angleValid(prevDirection);
	    if (moveAllowed) {
		consecutiveMoveCount++;
		return prevDirection;
	    }
	}

	/* Choose random direction */
	consecutiveMoveCount = 0;
	// smd.calcProbDist();
	pddp.normalize();
	prevDirection = pddp.getMovementAngle();
	return prevDirection;
    }

    @Override
    protected void reset() {
	consecutiveMoveCount = 0;
    }

    @Override
    protected void clear() {
    }

    @Override
    protected List<MessageTypeRegisterPair> getMessageTypesToRegister(
	    CommunicationType allowedCommTypes[]) {
	return new ArrayList<MessageTypeRegisterPair>();
    }
}
