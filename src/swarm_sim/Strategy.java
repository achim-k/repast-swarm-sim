package swarm_sim;

import java.util.List;

import org.jgap.Chromosome;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import swarm_sim.Agent.AgentState;
import swarm_sim.communication.INetworkAgent;
import swarm_sim.communication.Message;
import swarm_sim.perception.AngleSegment;

public abstract class Strategy {
    protected Scenario scenario;
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
	this.scenario = Scenario.getInstance();
	this.controllingAgent = controllingAgent;
    }

    protected abstract AgentState processMessage(Message msg,
	    AgentState currentState);

    protected abstract void sendMessage(INetworkAgent agentInRange,
	    AgentState currentState);

    protected abstract AgentState processPerceivedAgent(IAgent agent,
	    boolean isLast);

    protected abstract double makeDirectionDecision(
	    List<AngleSegment> collisionFreeSegments);

    protected abstract void clear(); // After each step

    protected abstract void reset(); // After other strategy was chosen
}
