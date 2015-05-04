package swarm_sim.foraging;

import org.jgap.IChromosome;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent.AgentState;
import swarm_sim.Agent;
import swarm_sim.IAgent;
import swarm_sim.SectorMap;
import swarm_sim.Strategy;

public abstract class ForagingStrategy extends Strategy {

    public class ResourceTarget {
	public int resourceCount;
	public NdPoint location;
	public SectorMap sector;
	public boolean validity;

	public ResourceTarget(int resouceCount, NdPoint location, SectorMap sector) {
	    super();
	    this.resourceCount = resouceCount;
	    this.location = location;
	    this.sector = sector;
	    this.validity = true;
	}
    }

    protected int perceivedResourceCount = 0;

    public ForagingStrategy(IChromosome chrom, Context<IAgent> context,
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
