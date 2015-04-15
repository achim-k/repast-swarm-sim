package swarm_sim.exploration;

import java.awt.Color;

import repast.simphony.context.Context;
import swarm_sim.Agent;
import swarm_sim.DefaultAgent;

public class DefaultExplorationAgent extends DefaultAgent {

    public enum agentState {
	exploring, blackbox_found,
    }

    protected agentState state = agentState.exploring;
    protected agentState prevState = agentState.exploring;

    public DefaultExplorationAgent(Context<Agent> context) {
	super(context);
    }

    public Color getColor() {
	Color retColor = Color.BLUE;
	switch (state) {
	case blackbox_found:
	    retColor = Color.YELLOW;
	    break;
	default:
	    retColor = Color.BLUE;
	    break;
	}
	return retColor;
    }
}
