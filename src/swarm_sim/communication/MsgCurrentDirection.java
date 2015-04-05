package swarm_sim.communication;

import swarm_sim.Agent;

public class MsgCurrentDirection extends DefaultMessage implements Message {

	public MsgCurrentDirection(Agent sender, Agent receiver, double data) {
		super(sender, receiver, data);
	}

	@Override
	public MessageType getType() {
		return Message.MessageType.Current_Direction;
	}
}
