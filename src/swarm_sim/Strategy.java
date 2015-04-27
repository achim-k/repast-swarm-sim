package swarm_sim;

import java.util.List;

import org.jgap.Chromosome;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import swarm_sim.Agent.AgentState;
import swarm_sim.communication.CommunicationType;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.communication.Message.MessageType;
import swarm_sim.perception.AngleSegment;

public abstract class Strategy {

    public class MessageTypeRegisterPair {
	public MessageType msgType;
	public AgentState states[];

	public MessageTypeRegisterPair(MessageType msgType, AgentState[] states) {
	    super();
	    this.msgType = msgType;
	    this.states = states;
	}
    }

    protected Configuration config;
    protected DataCollection data;
    protected Chromosome chrom;
    protected ContinuousSpace<IAgent> space;
    protected Context<IAgent> context;
    protected Agent controllingAgent;

    public Strategy(Chromosome chrom, Context<IAgent> context,
	    Agent controllingAgent) {
	super();
	this.chrom = chrom;
	this.context = context;
	this.space = (ContinuousSpace<IAgent>) context.getProjection(
		ContinuousSpace.class, "space_continuous");
	this.config = Configuration.getInstance();
	this.data = DataCollection.getInstance();
	this.controllingAgent = controllingAgent;
    }

    protected abstract List<MessageTypeRegisterPair> getMessageTypesToRegister(
	    CommunicationType allowedCommTypes[]);

    protected abstract AgentState processMessage(AgentState prevState,
	    AgentState currentState, Message msg, boolean isLast);

    protected abstract void sendMessage(AgentState prevState,
	    AgentState currentState, INetworkAgent agentInRange);

    protected abstract AgentState processPerceivedAgent(AgentState prevState,
	    AgentState currentState, IAgent agent, boolean isLast);

    protected abstract double makeDirectionDecision(AgentState prevState,
	    AgentState currentState, List<AngleSegment> collisionFreeSegments);

    protected abstract void clear(); // After each step

    protected abstract void reset(); // After other strategy was chosen
}
