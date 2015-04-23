package swarm_sim.foraging;

import java.awt.Color;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.DefaultAgent;
import swarm_sim.IAgent;

public class DefaultForagingAgent extends DefaultAgent {

    public enum AgentState {
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

    protected AgentState state = AgentState.wander;
    protected AgentState prevState = AgentState.wander;

    public DefaultForagingAgent(Context<IAgent> context) {
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
