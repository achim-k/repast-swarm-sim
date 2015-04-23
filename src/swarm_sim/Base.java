package swarm_sim;

import java.awt.Color;

import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.VSpatial;
import swarm_sim.communication.DefaultNetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.exploration.DefaultExplorationAgent.agentState;

public class Base extends DefaultNetworkAgent implements IAgent, IDisplayAgent {

    private agentState state = agentState.exploring;

    private void processMessageQueue() {
	Message msg = popMessage();
	while (msg != null) {
	    switch (msg.getType()) {
	    case Location:
		break;
	    default:
		break;
	    }
	    msg = popMessage();
	}
    }

    public void step() {
	processMessageQueue();
    }

    @Override
    public String getName() {
	// TODO Auto-generated method stub
	return "Base";
    }

    @Override
    public AgentType getAgentType() {
	// TODO Auto-generated method stub
	return AgentType.Base;
    }

    @Override
    public Color getColor() {
	switch (state) {
	case blackbox_found:
	    return Color.RED;
	default:
	    return Color.GREEN;
	}

    }

    public VSpatial getShape(ShapeFactory2D shapeFactory) {
	return shapeFactory.createCircle(4, 16);
    }

}
