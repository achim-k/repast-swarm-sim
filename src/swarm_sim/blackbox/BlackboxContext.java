package swarm_sim.blackbox;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import swarm_sim.Agent;
import swarm_sim.Agent.AgentType;
import swarm_sim.ScanCircle.AttractionType;
import swarm_sim.ScanCircle.DistributionType;
import swarm_sim.ScanCircle.GrowingDirection;
import swarm_sim.BaseAgent;
import swarm_sim.ScanCircle;
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
		bbScenario.reset();
		this.spaceContinuous = (ContinuousSpace<Agent>) parentContext.getProjection(ContinuousSpace.class, "space_continuous");
		
		String bb_agent = params.getString("blackbox_agent");
		bbScenario.pauseOnBBfound = params.getBoolean("pauseOnBBfound");
		
		if(bb_agent.equalsIgnoreCase("BB_Random"))
			bbScenario.agentType = AgentType.BB_Random;
		else if(bb_agent.equalsIgnoreCase("BB_RandomComm"))
			bbScenario.agentType = AgentType.BB_RandomComm;
		else if(bb_agent.equalsIgnoreCase("BB_PheromoneAvoider"))
			bbScenario.agentType = AgentType.BB_PheromoneAvoider;
		else if(bb_agent.equalsIgnoreCase("BB_RandomPoint"))
			bbScenario.agentType = AgentType.BB_RandomPoint;
		else if(bb_agent.equalsIgnoreCase("BB_AgentAvoiderComm"))
			bbScenario.agentType = AgentType.BB_AgentAvoiderComm;
		else if(bb_agent.equalsIgnoreCase("BB_AgentAvoiderMimicDirectionComm"))
			bbScenario.agentType = AgentType.BB_AgentAvoiderMimicDirectionComm;
		else if(bb_agent.equalsIgnoreCase("BB_RandomObstacleAvoider"))
			bbScenario.agentType = AgentType.BB_RandomObstacleAvoider;
			
		/* spawn blackbox */
		Blackbox bb = new Blackbox();
		this.add(bb);
		bbScenario.blackboxAgent = bb;
		
		/* Create agents */
		for (int i = 0; i < scenario.agentCount; i++) {
			Agent agent = null;
			switch (bbScenario.agentType) {
			case BB_RandomPoint:
				agent = new BB_RandomPoint(this, parentContext);
				break;
			case BB_Random:
				agent = new BB_Random(this, parentContext);
				break;
			case BB_RandomComm:
				agent = new BB_RandomComm(this, parentContext);
				scenario.networkAgents.add(agent);
				break;
			case BB_PheromoneAvoider:
				agent = new BB_PheromoneAvoider(this, parentContext);
				break;
			case BB_AgentAvoiderComm:
				agent = new BB_AgentAvoiderComm(this, parentContext);
				scenario.networkAgents.add(agent);
				break;
			case BB_AgentAvoiderMimicDirectionComm:
				agent = new BB_AgentAvoiderMimicDirectionComm(this, parentContext);
				scenario.networkAgents.add(agent);
				break;
			case BB_RandomObstacleAvoider:
				agent = new BB_RandomObstacleAvoider(this, parentContext);
				break;
			default:
				break;
			}
			
			schedule.schedule(scheduleParams, agent, "step");
			this.add(agent);
		}
		
		
		ScanCircle s = new ScanCircle(8, 1, 1, AttractionType.Appealing, DistributionType.Linear, GrowingDirection.Inner, 0, 10, 0.5, 2);
		ScanCircle d = new ScanCircle(8, 1, 1, AttractionType.Appealing, DistributionType.Linear, GrowingDirection.Inner, 0, 10, 0.5, 2);
		s.add(s.new InputPair(0.1, 0.5));
		d.add(d.new InputPair(2.1, 0.5));
		s.calculateDirectionDistribution();
		d.calculateDirectionDistribution();
		s.normalize();
		d.normalize();
		System.out.println(s.getPrintable(null));
		System.out.println(d.getPrintable(null));
		System.out.println(ScanCircle.merge(8, 0, s,d).getPrintable(null));
		System.out.println(ScanCircle.merge(8, 0.10, s,d).getPrintable(null));
//		System.out.println(ScanCircle.getMerged(8, s).getPrintable(""));
		
		System.out.println("BlackBoxContext loaded!");
	}
}