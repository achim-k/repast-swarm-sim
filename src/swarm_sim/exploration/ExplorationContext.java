package swarm_sim.exploration;

import java.util.ArrayList;
import java.util.List;

import org.jgap.Gene;
import org.jgap.impl.DoubleGene;
import org.jgap.impl.IntegerGene;

import repast.simphony.adaptation.ga.RepastGA;
import repast.simphony.context.Context;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentType;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.CircleScan;
import swarm_sim.Base;
import swarm_sim.PseudoRandomAdder;
import swarm_sim.RootContext;
import swarm_sim.Scenario;

public class ExplorationContext extends RootContext implements
	ContextBuilder<Agent> {

    // int populationSize = 10;
    //
    // RepastGA ga = new RepastGA(this, "evaluate", populationSize, new
    // Gene[]{new IntegerGene(1,
    // 2), new DoubleGene(1, 2)});

    public Context<Agent> build(Context<Agent> context) {
	context = super.build(context);

	/* Get scenario parameters */
	RunEnvironment runEnv = RunEnvironment.getInstance();
	Parameters params = runEnv.getParameters();
	Scenario scenario = Scenario.getInstance();

	PseudoRandomAdder<Agent> adder = new PseudoRandomAdder<Agent>(
		exploredArea);
	adder.setRandomAdderSaveClass(Base.class);
	space.setAdder(adder);

	/* spawn base */
	ISchedule schedule = runEnv.getCurrentSchedule();
	ScheduleParameters scheduleParams = ScheduleParameters.createRepeating(
		1, 1, ScheduleParameters.LAST_PRIORITY);
	Base base = new Base();
	schedule.schedule(scheduleParams, base, "step");
	context.add(base);
	scenario.baseAgent = base;
	scenario.networkAgents.add(base);

	String bb_agent = params.getString("agent_type");
	AgentType agentType = AgentType.EXPL_Random;

	if (bb_agent.equalsIgnoreCase("EXPL_Random"))
	    agentType = AgentType.EXPL_Random;
	else if (bb_agent.equalsIgnoreCase("EXPL_PheromoneAvoider"))
	    agentType = AgentType.EXPL_PheromoneAvoider;
	else if (bb_agent.equalsIgnoreCase("EXPL_AgentRepell"))
	    agentType = AgentType.EXPL_AgentRepell;
	else if (bb_agent.equalsIgnoreCase("EXPL_AgentAvoiderMimic"))
	    agentType = AgentType.EXPL_AgentAvoiderMimic;
	else if (bb_agent.equalsIgnoreCase("EXPL_Memory"))
	    agentType = AgentType.EXPL_Memory;
	else if (bb_agent.equalsIgnoreCase("EXPL_AvoidAppealMimicMemory"))
	    agentType = AgentType.EXPL_AvoidAppealMimicMemory;

	/* Create agents */
	scheduleParams = ScheduleParameters.createRepeating(1, 1);

	for (int i = 0; i < scenario.agentCount; i++) {
	    Agent agent = null;
	    switch (agentType) {
	    case EXPL_Random:
		agent = new Random(context);
		break;
	    case EXPL_PheromoneAvoider:
		agent = new PheromoneAvoider(context);
		break;
	    case EXPL_AgentRepell:
		agent = new AgentRepell(context);
		scenario.networkAgents.add(agent);
		break;
	    case EXPL_AgentAvoiderMimic:
		agent = new AgentAvoiderMimic(context);
		scenario.networkAgents.add(agent);
		break;
	    case EXPL_Memory:
		agent = new Memory(context);
		scenario.networkAgents.add(agent);
		break;
	    case EXPL_AvoidAppealMimicMemory:
		agent = new AvoidAppealMimicMemory(context);
		scenario.networkAgents.add(agent);
		break;
	    default:
		break;
	    }
	    schedule.schedule(scheduleParams, agent, "step");
	    context.add(agent);
	}

	System.out.println("Exploration context loaded");

	int binCount = 8;
	CircleScan agentRepell = new CircleScan(binCount, 1, 1, 10000, 1, 0,
		-1, 0, 0.6 * scenario.commScope);
	CircleScan agentAppeal = new CircleScan(binCount, 1, 1, 10000, 1, 1, 0,
		0.7 * scenario.commScope, 1 * scenario.commScope);

	 AngleSegment s = new AngleSegment(-Math.PI, Math.PI);
	 List<AngleSegment> l = new ArrayList();
	 l.add(s);

	 agentAppeal.add(0.2, 1 * scenario.commScope);
	 System.out.println(CircleScan.merge(binCount, 0, l, agentAppeal).getPrintable());
	 
	 //
	// agentAppeal.add(0.2, 1);
	// agentAppeal2.add(0.2, 1);
	// CircleScan res = CircleScan.merge(8, 0, l, agentAppeal);
	// System.out.println(res.getPrintable());
	// System.out.println(CircleScan.merge(8, 0, l,
	// agentAppeal2).getPrintable());
	return context;
    }
}
