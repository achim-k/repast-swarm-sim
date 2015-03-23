package swarm_sim.blackbox;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentType;
import swarm_sim.BaseAgent;

public class BlackboxContext extends DefaultContext<Agent> {

	private ContinuousSpace<Agent> spaceContinuous;

	public BlackboxContext(Context<Agent> parentContext, String name) {
		super(name);

		RunEnvironment runEnv = RunEnvironment.getInstance();
		Parameters params = runEnv.getParameters();
		ISchedule schedule = runEnv.getCurrentSchedule();
		ScheduleParameters scheduleParams = ScheduleParameters.createRepeating(
				1, 1);

		BlackboxScenario scenario = BlackboxScenario.getInstance();
		this.spaceContinuous = (ContinuousSpace<Agent>) parentContext.getProjection(ContinuousSpace.class, "space_continuous");
		
		String bb_agent = params.getString("blackbox_agent");
		
		if(bb_agent.equalsIgnoreCase("BB_RandomExplorerNoComm"))
			scenario.agentType = AgentType.BB_RandomExplorerNoComm;
		

		/* spawn blackbox */
		Blackbox bb = new Blackbox();
		this.add(bb);
		scenario.blackboxAgent = bb;
		
		/* spwan base */
		BaseAgent base = new BaseAgent();
		this.add(base);
		scenario.baseAgent = base;		
		
		/* Create agents */
		for (int i = 0; i < scenario.agentCount; i++) {
			Agent agent = null;
			switch (scenario.agentType) {
			case BB_RandomExplorerNoComm:
				agent = new BB_RandomExplorerNoComm(this, parentContext);
				break;
			default:
				break;
			}
			
			schedule.schedule(scheduleParams, agent, "step");
			this.add(agent);
		}
		
		System.out.println("BlackBoxContext loaded!");
	}
}