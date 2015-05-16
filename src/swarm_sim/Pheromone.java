package swarm_sim;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.ContextUtils;

public class Pheromone implements IAgent {

    double health;
    Configuration config = Configuration.getInstance();

    public Pheromone() {
	health = 100;
    }

    @ScheduledMethod(start = 1, interval = 1)
    public void decay() {
	health -= config.decayRate;
	if (health <= 0) {
	    ContextUtils.getContext(this).remove(this);
	}
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.Pheromone;
    }
}
