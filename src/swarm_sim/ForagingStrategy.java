package swarm_sim;

import org.jgap.Chromosome;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.NdPoint;
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

    protected AgentState state = AgentState.acquire;
    protected int perceivedResourceCount = 0;

    public ForagingStrategy(Chromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super(chrom, context, controllingAgent);
    }

    protected abstract AgentState checkState();

    @Override
    protected void clear() {
	perceivedResourceCount = 0;
    }

    @Override
    protected void reset() {
	state = AgentState.acquire;
    }
}
