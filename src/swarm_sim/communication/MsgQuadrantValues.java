package swarm_sim.communication;

import swarm_sim.Agent;
import swarm_sim.QuadrantMap;

public class MsgQuadrantValues extends DefaultMessage implements Message {

	public MsgQuadrantValues(Agent sender, Agent receiver, QuadrantMap data) {
		super(sender, receiver, data);
	}

	@Override
	public MessageType getType() {
		return Message.MessageType.QuadrantMap;
	}
}
