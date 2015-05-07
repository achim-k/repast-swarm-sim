package swarm_sim;



public class Configuration implements IAgent {

    /* Params */
    public int agentCount;
    public double perceptionScope;
    public double commScope;
    public int consecutiveMoves = 1;
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
    public boolean printConfig = true;
    
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

    @Override
    public AgentType getAgentType() {
	return AgentType.Configuration;
    }

    public int getAgentCount() {
        return agentCount;
    }

    public double getPerceptionScope() {
        return perceptionScope;
    }

    public double getCommScope() {
        return commScope;
    }

    public int getRndConsecutiveMoves() {
        return consecutiveMoves;
    }

    public boolean isUseGA() {
        return useGA;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public int getResourceNestCount() {
        return resourceNestCount;
    }

    public int getCommFreq() {
        return commFreq;
    }

    public String getExplStrat() {
        return explStrat;
    }

    public String getForagingStrat() {
        return foragingStrat;
    }

    public int getMaxTicks() {
        return maxTicks;
    }

    public double getMaxMoveDistance() {
        return maxMoveDistance;
    }

    public Base getBaseAgent() {
        return baseAgent;
    }

    public int[] getMovebins() {
        return movebins;
    }

    public int getSpaceWidth() {
        return spaceWidth;
    }

    public int getSpaceHeight() {
        return spaceHeight;
    }
    
    
}
