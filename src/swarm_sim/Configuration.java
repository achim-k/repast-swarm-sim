package swarm_sim;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.util.ContextUtils;

public class Configuration {

    /* Params */
    public int agentCount;
    public double perceptionScope;
    public double commScope;
    public int rndConsecutiveMoves = 1;
    public boolean useGA = false;
    public int resourceCount;
    public int resourceNestCount;
    public int commFreq;
    public String explStrat;
    public String foragingStrat;

    /* Other stuff */
    public int maxTicks;
    public double maxMoveDistance = 1.0;
    public Base baseAgent;
    
    public int[] movebins = new int[8];
    public int spaceWidth, spaceHeight;
    
    
    private static Configuration instance = null;

    private Configuration() {
    }

    public static Configuration getInstance() {
	if (instance == null) {
	    instance = new Configuration();
	}
	return instance;
    }
    
    public void reset() {
	movebins = new int[8];
    }
}
