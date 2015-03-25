package swarm_sim.communication;

import repast.simphony.engine.environment.RunEnvironment;
import swarm_sim.Agent;

public class DefaultMessage implements Message {

	Object data;
	Agent sender, receiver;
	private int tick;

	public DefaultMessage(Agent sender, Agent receiver, Object data) {
		this.sender = sender;
		this.receiver = receiver;
		this.data = data;
		this.tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}

	@Override
	public MessageType getType() {
		return MessageType.Location;
	}

	@Override
	public Agent getSender() {
		return sender;
	}

	@Override
	public Object getData() {
		return data;
	}

	@Override
	public int getTick() {
		return tick;
	}
}
