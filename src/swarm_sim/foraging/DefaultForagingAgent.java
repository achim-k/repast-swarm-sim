package swarm_sim.foraging;

import java.awt.Color;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;
import swarm_sim.DefaultAgent;

public class DefaultForagingAgent extends DefaultAgent {

    public enum agentState {
	wander, acquire, deliver
    }
    
    public class ResourcesTarget {
	public int resourceCount;
	public NdPoint location;
	
	public ResourcesTarget(int resouceCount, NdPoint location) {
	    super();
	    this.resourceCount = resouceCount;
	    this.location = location;
	}
    }

    protected agentState state = agentState.wander;
    protected agentState prevState = agentState.wander;

    public DefaultForagingAgent(Context<Agent> context) {
	super(context);
    }

    public Color getColor() {
	Color retColor = Color.BLUE;
	switch (state) {
	case wander:
	    retColor = Color.BLUE;
	    break;
	case acquire:
	    retColor = Color.YELLOW;
	    break;
	case deliver:
	    retColor = Color.RED;
	    break;
	default:
	    retColor = Color.BLUE;
	    break;
	}
	return retColor;
    }
}
