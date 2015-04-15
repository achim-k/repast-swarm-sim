package swarm_sim.communication;

import swarm_sim.Agent;

public class MsgSectorValues extends DefaultMessage implements Message {

	public MsgSectorValues(Agent sender, Agent receiver, Object data[]) {
		super(sender, receiver, data);
	}

	@Override
	public MessageType getType() {
		return Message.MessageType.SectorMap;
	}
}
