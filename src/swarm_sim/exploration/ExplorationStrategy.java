package swarm_sim.exploration;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentState;
import swarm_sim.IAgent;
import swarm_sim.IAgent.AgentType;
import swarm_sim.Strategy;

public abstract class ExplorationStrategy extends Strategy {

    public ExplorationStrategy(IChromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
    }

    @Override
    public AgentState processPerceivedAgent(AgentState prevState,
	    AgentState currentState, IAgent agent, boolean isLast) {
	if (agent.getAgentType() == AgentType.Resource
		|| agent.getAgentType() == AgentType.Pheromone)
	    return AgentState.acquire;

	return AgentState.wander;
    }

}
