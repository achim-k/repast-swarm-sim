package swarm_sim.communication;

import repast.simphony.space.continuous.NdPoint;
import swarm_sim.Agent;

public class MsgBlackboxFound extends DefaultMessage implements Message {

	public MsgBlackboxFound(Agent sender, Agent receiver, NdPoint data) {
		super(sender, receiver, data);
	}

	@Override
	public MessageType getType() {
		// TODO Auto-generated method stub
		return Message.MessageType.Blackbox_found;
	}
}
