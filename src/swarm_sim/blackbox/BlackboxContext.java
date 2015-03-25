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
import swarm_sim.Scenario;

public class BlackboxContext extends DefaultContext<Agent> {

	private ContinuousSpace<Agent> spaceContinuous;

	public BlackboxContext(Context<Agent> parentContext, String name) {
		super(name);

		RunEnvironment runEnv = RunEnvironment.getInstance();
		Parameters params = runEnv.getParameters();
		ISchedule schedule = runEnv.getCurrentSchedule();
		ScheduleParameters scheduleParams = ScheduleParameters.createRepeating(
				1, 1);

		Scenario scenario = Scenario.getInstance();
		BlackboxScenario bbScenario = BlackboxScenario.getInstance();
		this.spaceContinuous = (ContinuousSpace<Agent>) parentContext.getProjection(ContinuousSpace.class, "space_continuous");
		
		String bb_agent = params.getString("blackbox_agent");
		
		if(bb_agent.equalsIgnoreCase("BB_RandomExplorerNoComm"))
			bbScenario.agentType = AgentType.BB_RandomExplorerNoComm;
		else if(bb_agent.equalsIgnoreCase("BB_RandomExplorerWithComm"))
			bbScenario.agentType = AgentType.BB_RandomExplorerWithComm;

		/* spawn blackbox */
		Blackbox bb = new Blackbox();
		this.add(bb);
		bbScenario.blackboxAgent = bb;
		
		/* Create agents */
		for (int i = 0; i < scenario.agentCount; i++) {
			Agent agent = null;
			switch (bbScenario.agentType) {
			case BB_RandomExplorerNoComm:
				agent = new BB_RandomExplorerNoComm(this, parentContext);
				break;
			case BB_RandomExplorerWithComm:
				agent = new BB_RandomExplorerWithComm(this, parentContext);
				scenario.networkAgents.add(agent);
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