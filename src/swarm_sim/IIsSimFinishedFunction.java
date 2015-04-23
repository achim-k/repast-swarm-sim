package swarm_sim;

import repast.simphony.context.Context;

public interface IIsSimFinishedFunction {
    boolean isSimFinished(Context<IAgent> c, AdvancedGridValueLayer l);
}
