package swarm_sim.exploration;

import org.jgap.Chromosome;

import repast.simphony.context.Context;
import swarm_sim.Agent;
import swarm_sim.IAgent;
import swarm_sim.Strategy;

public abstract class ExplorationStrategy extends Strategy {

    public ExplorationStrategy(Chromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
    }

}
