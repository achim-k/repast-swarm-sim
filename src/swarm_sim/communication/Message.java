package swarm_sim.communication;

import swarm_sim.Agent;

public interface Message {

	MessageType getType();
	Agent getSender();
	Object getData();
	int getTick();
	
	public enum MessageType {
		Location,
		Blackbox_found, Current_Direction, QuadrantMap,
	}
}
