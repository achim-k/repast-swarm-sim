package swarm_sim.communication;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.engine.environment.RunEnvironment;
import swarm_sim.Scenario;

public class DefaultNetworkAgent implements NetworkAgent {

	protected List<Message> messageQueue = new ArrayList<>();
	protected Scenario scenario;
	
	public DefaultNetworkAgent() {
		this.scenario = Scenario.getInstance();
	}
	
	@Override
	public void addToMessageQueue(Message msg) {
		messageQueue.add(msg);
		scenario.messagesSent++;
	}
	
	protected Message popMessage() {
		if(messageQueue.size() <= 0)
			return null;
		else {
			Message msg = messageQueue.get(0);
			if(msg.getTick() >= (int)RunEnvironment.getInstance().getCurrentSchedule().getTickCount())
				return null;
			messageQueue.remove(0);
			return msg;
		}
	}

}
