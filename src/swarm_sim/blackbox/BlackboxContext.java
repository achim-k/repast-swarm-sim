package swarm_sim.blackbox;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.DefaultSchedulableActionFactory;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import swarm_sim.Agent;
import swarm_sim.ScenarioParameters;

public class BlackboxContext extends DefaultContext<Agent>  {

	public BlackboxContext(Context<Agent> parentContext, String name) {
		super(name);
		
		RunEnvironment runEnv = RunEnvironment.getInstance();
		ISchedule schedule = runEnv.getCurrentSchedule();		
		ScheduleParameters scheduleParams = ScheduleParameters.createRepeating(1, 1);
		
		ScenarioParameters scenParams = ScenarioParameters.getInstance();
		
		for (int i = 0; i < scenParams.agentCount; i++) {
			Agent agent = new RandomExplorer(this, parentContext);
			schedule.schedule(scheduleParams, agent, "step");
			this.add(agent);
		}
		
		/* spawn blackbox */
		this.add(new Blackbox());
		

		System.out.println("BlackBoxContext loaded!");
	}
}