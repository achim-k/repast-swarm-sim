package swarm_sim.blackbox;

import java.awt.Color;

import repast.simphony.context.Context;
import swarm_sim.Agent;
import swarm_sim.DefaultAgent;

public class DefaultBlackboxAgent extends DefaultAgent {

	public enum agentState {
		exploring, blackbox_found,
	}

	protected agentState state = agentState.exploring;
	protected agentState prevState = agentState.exploring;

	protected BlackboxScenario bbScenario;

	public DefaultBlackboxAgent(Context<Agent> context,
			Context<Agent> rootContext) {
		super(context, rootContext);
		bbScenario = BlackboxScenario.getInstance();
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
