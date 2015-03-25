package swarm_sim;

import com.jidesoft.plaf.windows.TMSchema.Control;

import repast.simphony.context.Context;
import repast.simphony.context.ContextFactoryFinder;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.dataLoader.engine.ContextBuilderFactory;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.valueLayer.GridValueLayer;
import swarm_sim.blackbox.Blackbox;
import swarm_sim.blackbox.BlackboxContext;
import swarm_sim.blackbox.BlackboxScenario;

/**
 * This class creates the global context, used by every scenario. It creates the
 * space where the agents move, value layers etc.
 * 
 * @author achim
 * 
 */
public class RootContextBuilder implements ContextBuilder<Agent> {

	private int spaceWidth = 100;
	private int spaceHeight = 100;

	public Context<Agent> build(Context<Agent> context) {
		/* Set context id */
		context.setId("root");

		/* get parameters */
		RunEnvironment runEnv = RunEnvironment.getInstance();
		Parameters params = runEnv.getParameters();
		Scenario scenario = Scenario.getInstance();

		scenario.agentCount = params.getInteger("agent_count");
		scenario.perceptionScope = params.getInteger("perception_scope");
		scenario.commScope = params.getInteger("communication_scope");

		/* Create continuous space */
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		
		PseudoRandomAdder<Agent> adder = new PseudoRandomAdder<Agent>();
		adder.setRandomAdderSaveClass(BaseAgent.class);
		adder.addRandomAdderClass(Blackbox.class);
		
		spaceFactory.createContinuousSpace("space_continuous", context,
				adder,
				new repast.simphony.space.continuous.BouncyBorders(),
				spaceWidth, spaceWidth);
		
		/* Create agent network (holds all edges between all agents) */
		scenario.agentNet = new AgentNet();
		scenario.networkAgents.clear();
		/* Create comm network (holds only edges of agents which are within communication range */
		CommNet<Agent> commNet = new CommNet<>("network_comm", context);
		context.addProjection(commNet);

		/* Value layer to track explored area, default: 0.0 */
		AdvancedGridValueLayer exploredArea = new AdvancedGridValueLayer(
				"layer_explored", 0.0, false, spaceWidth, spaceHeight);
		context.addValueLayer(exploredArea);
		
		/* Add scenario context */
		context.addSubContext(new BlackboxContext(context, "content_blackbox"));

		/* init scenario */
		ISchedule schedule = runEnv.getCurrentSchedule();
		ScheduleParameters scheduleParams = ScheduleParameters.createOneTime(0,
				ScheduleParameters.FIRST_PRIORITY);
		schedule.schedule(scheduleParams, scenario, "init");
		context.add(scenario);
		
		/* add controller agent */
		scheduleParams = ScheduleParameters.createRepeating(1, 1, ScheduleParameters.FIRST_PRIORITY);
		ControllerAgent controller = new ControllerAgent(context, scenario);
		schedule.schedule(scheduleParams, controller, "step");
		context.add(controller);
		
		/* spawn base */
		scheduleParams = ScheduleParameters.createRepeating(1, 1, ScheduleParameters.LAST_PRIORITY);
		BaseAgent base = new BaseAgent();
		schedule.schedule(scheduleParams, base, "step");
		context.add(base);
		scenario.baseAgent = base;		
		scenario.networkAgents.add(base);

		return context;
	}
}
