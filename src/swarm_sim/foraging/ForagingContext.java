package swarm_sim.foraging;

import repast.simphony.context.Context;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import swarm_sim.AdvancedGridValueLayer;
import swarm_sim.Agent;
import swarm_sim.Base;
import swarm_sim.IAgent;
import swarm_sim.IAgent.AgentType;
import swarm_sim.IIsSimFinishedFunction;
import swarm_sim.PseudoRandomAdder;
import swarm_sim.RootContext;
import swarm_sim.Scenario;
import swarm_sim.learning.GA;

public class ForagingContext extends RootContext implements
	ContextBuilder<IAgent>, IIsSimFinishedFunction {

    public Context<IAgent> build(Context<IAgent> context) {
	context = super.build(context, this);

	/* Get scenario parameters */
	RunEnvironment runEnv = RunEnvironment.getInstance();
	Parameters params = runEnv.getParameters();
	Scenario scenario = Scenario.getInstance();

	PseudoRandomAdder<IAgent> adder = new PseudoRandomAdder<IAgent>(
		exploredArea);
	adder.setRandomAdderSaveClass(Base.class);

	int resourceNestCount = params.getInteger("resource_nest_count");
	adder.setResourceAdderSaveClass(Resource.class, resourceNestCount, 1,
		space);
	space.setAdder(adder);

	scenario.resourceCount = params.getInteger("resource_count");

	/* add Resources */
	for (int i = 0; i < scenario.resourceCount; i++) {
	    context.add(new Resource());
	}

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

	if (bb_agent.equalsIgnoreCase("FAGN_Random"))
	    agentType = AgentType.FAGN_Random;
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

	GA ga = GA.getInstance();

	/* Create agents */
	scheduleParams = ScheduleParameters.createRepeating(1, 1);

	for (int i = 0; i < scenario.agentCount; i++) {
	    IAgent agent = new Agent(context, null);
	    scenario.networkAgents.add(agent);
	    schedule.schedule(scheduleParams, agent, "step");
	    context.add(agent);
	}

	System.out.println();
	System.out.println("Foraging context loaded");
	System.out.println("-----------------------------------");
	System.out.println("Number of Agents:\t" + scenario.agentCount);
	System.out.println("Type of Agents:  \t" + agentType);
	System.out.println("Perception-Scope:\t" + scenario.perceptionScope);
	System.out.println("Communic.-Scope: \t" + scenario.commScope);
	System.out.println("Consecutive moves:\t"
		+ scenario.rndConsecutiveMoves);
	System.out.println("Use Genetic Alg.:\t" + scenario.useGA);
	if (scenario.useGA)
	    System.out.println("Chromosomes:     \t:" + ga.currentChromosome);

	return context;
    }

    @Override
    public void endAction() {
	RunEnvironment runenv = RunEnvironment.getInstance();

	String out = String.format(
		"Exploration Simulation has ended after %.1f ticks", runenv
			.getCurrentSchedule().getTickCount());
	System.out.println(out);
    }

    @Override
    public boolean isSimFinished(Context<IAgent> c, AdvancedGridValueLayer l) {
	Scenario scen = Scenario.getInstance();

	return scen.deliveredResources >= scen.resourceCount;

	// TODO GA!
    };

}
