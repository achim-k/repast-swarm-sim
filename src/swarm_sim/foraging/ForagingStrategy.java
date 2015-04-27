package swarm_sim.foraging;

import org.jgap.Chromosome;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.IAgent;
import swarm_sim.Strategy;
import swarm_sim.Agent.AgentState;

public abstract class ForagingStrategy extends Strategy {

    public class ResourcesTarget {
	public int resourceCount;
	public NdPoint location;

	public ResourcesTarget(int resouceCount, NdPoint location) {
	    super();
	    this.resourceCount = resouceCount;
	    this.location = location;
	}
    }

    protected int perceivedResourceCount = 0;

    public ForagingStrategy(Chromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
    }

    public abstract AgentState checkState(AgentState prevState,
	    AgentState currentState);

    @Override
    public void clear() {
	perceivedResourceCount = 0;
    }

    @Override
    public void reset() {
    }
}
