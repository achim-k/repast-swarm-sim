package swarm_sim.blackbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.algorithm.Angle;

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
import swarm_sim.perception.AngleFilter;
import swarm_sim.perception.AngleSegment;
import swarm_sim.perception.CircleScan;
import swarm_sim.Base;
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
		else if(bb_agent.equalsIgnoreCase("BB_MemoryComm"))
			bbScenario.agentType = AgentType.BB_MemoryComm;
			
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
			case BB_MemoryComm:
				agent = new BB_MemoryComm(this, parentContext);
				scenario.networkAgents.add(agent);
				break;
			default:
				break;
			}
			
			schedule.schedule(scheduleParams, agent, "step");
			this.add(agent);
		}
		
		
		AngleFilter f = new AngleFilter(1);
		f.add(2, -3*Math.PI/4, 1);
		List<AngleSegment> l = new ArrayList<AngleSegment>();
		
		CircleScan c = new CircleScan(8, 1, 1, 10, 1, 1, 2, 0, 10);
		c.add(0.2, 5);
//		c.add(-1.2, 5);
		c.setExlusiveSegments(l);
		c.calculateDirectionDistribution();
		c.normalize();
		System.out.println(c.getPrintable());
//		l.add(new AngleSegment(-0, Math.PI));
		
		CircleScan res = CircleScan.merge(8, 0.12, l, c);
		System.out.println(res.getPrintable());
		System.out.println(res.getMovementAngle());
		
		
		AngleSegment test1 = new AngleSegment(3*Math.PI/4, Math.PI);
		AngleSegment test2 = new AngleSegment(-Math.PI, -3*Math.PI/4);
		
		
		l.add(new AngleSegment(Math.PI/2, 13*Math.PI/16));
		l.add(new AngleSegment(3*Math.PI/4, 14*Math.PI/16));
		l.add(new AngleSegment(15*Math.PI/16, -14*Math.PI/16));
		l.add(new AngleSegment(15*Math.PI/16, -15*Math.PI/16));
		l.add(new AngleSegment(-13*Math.PI/16, -8*Math.PI/16));
				
//		f.new AngleSegment(0, 8).filterSegment(l);
		Collections.shuffle(l);
		Collections.sort(l, new AngleSegment(0, 0));
		System.out.println();
		System.out.println(test1.start + "\t" + test1.end);
		for (AngleSegment ap : test1.filterSegment(l)) {
			System.out.println(ap.start + "\t" + ap.end);
		}
		
		System.out.println();
		System.out.println(test2.start + "\t" + test2.end);
		for (AngleSegment ap : test2.filterSegment(l)) {
			System.out.println(ap.start + "\t" + ap.end);
		}
		
		
		l.clear();
//		l.add(new AngleSegment(-1, 0.5));
//		l.add(new AngleSegment(1, 1.75));
		l.add(new AngleSegment(-1, 3));
		System.out.println("mutual");
		for (AngleSegment ap : new AngleSegment(0, 2).calcMutualSegments(l)) {
			System.out.println(ap.start + "\t" + ap.end);
		}
		
		System.out.println("BlackBoxContext loaded!");
	}
}