package swarm_sim.communication;

import repast.simphony.engine.environment.RunEnvironment;

public class Message {

    public enum MessageType {
	Location, Direction, SectorMap, CurrentState
    }

    MessageType type;
    INetworkAgent sender;
    Object data;
    int tick;

    public Message(MessageType type, INetworkAgent sender, Object data) {
	super();
	this.type = type;
	this.sender = sender;
	this.data = data;
	this.tick = (int) RunEnvironment.getInstance().getCurrentSchedule()
		.getTickCount();
    }

    public MessageType getType() {
	return type;
    }

    public INetworkAgent getSender() {
	return sender;
    }

    public Object getData() {
	return data;
    }

    public int getTick() {
	return tick;
    }

}
