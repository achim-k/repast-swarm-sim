package swarm_sim.exploration;

import repast.simphony.context.Context;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentType;
import swarm_sim.Base;
import swarm_sim.PseudoRandomAdder;
import swarm_sim.RootContext;
import swarm_sim.Scenario;

public class ExplorationContext extends RootContext implements
	ContextBuilder<Agent> {

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

	String bb_agent = params.getString("blackbox_agent");
	AgentType agentType = AgentType.EXPL_Random;

	if (bb_agent.equalsIgnoreCase("BB_Random"))
	    agentType = AgentType.EXPL_Random;
	else if (bb_agent.equalsIgnoreCase("BB_PheromoneAvoider"))
	    agentType = AgentType.EXPL_PheromoneAvoider;
	else if (bb_agent.equalsIgnoreCase("BB_AgentAvoiderComm"))
	    agentType = AgentType.EXPL_AgentAvoiderComm;
	else if (bb_agent.equalsIgnoreCase("BB_AgentAvoiderMimicDirectionComm"))
	    agentType = AgentType.EXPL_AgentAvoiderMimicDirectionComm;
	else if (bb_agent.equalsIgnoreCase("BB_MemoryComm"))
	    agentType = AgentType.EXPL_MemoryComm;

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
	    case EXPL_AgentAvoiderComm:
		agent = new AgentAvoiderComm(context);
		scenario.networkAgents.add(agent);
		break;
	    case EXPL_AgentAvoiderMimicDirectionComm:
		agent = new AgentAvoiderMimicDirectionComm(context);
		scenario.networkAgents.add(agent);
		break;
	    case EXPL_MemoryComm:
		agent = new MemoryComm(context);
		scenario.networkAgents.add(agent);
		break;
	    default:
		break;
	    }

	    schedule.schedule(scheduleParams, agent, "step");
	    context.add(agent);
	}

	return context;
    }
}
