package swarm_sim;

import repast.simphony.context.Context;

public interface IsSimFinishedFunction {
    boolean isSimFinished(Context<Agent> c, AdvancedGridValueLayer l);
}
