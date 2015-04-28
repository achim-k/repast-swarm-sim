package swarm_sim;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import repast.simphony.engine.environment.RunEnvironment;
import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.VSpatial;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;

public class Base implements IAgent, IDisplayAgent, INetworkAgent {

    private List<Message> messageQueue = new ArrayList<>();
    private DataCollection data = DataCollection.getInstance();

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
	return "Base";
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.Base;
    }

    @Override
    public Color getColor() {
	return Color.GREEN;
    }
    
    @Override
    public void pushMessage(Message msg) {
	messageQueue.add(msg);
	data.messageCount++;
    }

    private Message popMessage() {
	if (messageQueue.size() <= 0)
	    return null;
	else {
	    Message msg = messageQueue.get(0);
	    if (msg.getTick() >= (int) RunEnvironment.getInstance()
		    .getCurrentSchedule().getTickCount())
		return null;
	    messageQueue.remove(0);
	    return msg;
	}
    }

    public VSpatial getShape(ShapeFactory2D shapeFactory) {
	return shapeFactory.createCircle(4, 16);
    }

}
