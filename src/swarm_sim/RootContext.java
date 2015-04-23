package swarm_sim;

import java.awt.image.BufferedImage;
import java.io.File;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import swarm_sim.AdvancedGridValueLayer.FieldType;

/**
 * This class creates the global context, used by every scenario. It creates the
 * space where the agents move, value layers etc.
 * 
 * @author achim
 * 
 */
public class RootContext {

    protected int spaceWidth = 100;
    protected int spaceHeight = 100;

    protected AdvancedGridValueLayer exploredArea;
    protected ContinuousSpace<IAgent> space;

    protected Context<IAgent> build(Context<IAgent> context,
	    IIsSimFinishedFunction simFinishedFunc) {
	/* Set context id */
	context.setId("root");

	/* get parameters */
	RunEnvironment runEnv = RunEnvironment.getInstance();
	Parameters params = runEnv.getParameters();
	Scenario scenario = Scenario.getInstance();
	scenario.reset();

	/* Do not run more than 20k ticks */
	runEnv.endAt(20000);

	scenario.agentCount = params.getInteger("agent_count");
	scenario.perceptionScope = params.getDouble("perception_scope");
	scenario.commScope = params.getDouble("communication_scope");
	scenario.rndConsecutiveMoves = params
		.getInteger("random_consecutive_move");
	scenario.useGA = params.getBoolean("use_ga");

	/* Value layer to track explored area, default: 0.0 */
	exploredArea = new AdvancedGridValueLayer("layer_explored", 0.0, false,
		spaceWidth, spaceHeight);
	context.addValueLayer(exploredArea);
	readMapFromImage(exploredArea, "data/map.png");

	/* Create continuous space */
	ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
		.createContinuousSpaceFactory(null);

	space = spaceFactory.createContinuousSpace("space_continuous", context,
		new RandomCartesianAdder<IAgent>(),
		new repast.simphony.space.continuous.BouncyBorders(),
		spaceWidth, spaceWidth);

	/* Create agent network (holds all edges between all agents) */
	scenario.networkAgents.clear();
	/*
	 * Create comm network (holds only edges of agents which are within
	 * communication range
	 */
	NetworkBuilder<IAgent> builder = new NetworkBuilder<>("network_comm",
		context, true);
	builder.buildNetwork();

	/* init scenario */
	ISchedule schedule = runEnv.getCurrentSchedule();
	ScheduleParameters scheduleParams = ScheduleParameters.createOneTime(0,
		ScheduleParameters.FIRST_PRIORITY);
	schedule.schedule(scheduleParams, scenario, "init");
	context.add(scenario);

	/* add controller agent */
	scheduleParams = ScheduleParameters.createRepeating(1, 1,
		ScheduleParameters.FIRST_PRIORITY);
	ControllerAgent controller = new ControllerAgent(context,
		simFinishedFunc);
	schedule.schedule(scheduleParams, controller, "step");
	context.add(controller);

	/* schedule action which is called at end of simulation */
	scheduleParams = ScheduleParameters
		.createAtEnd(ScheduleParameters.LAST_PRIORITY);
	schedule.schedule(scheduleParams, this, "endAction");

	return context;
    }

    protected void endAction() {
	System.out.println("Simulation has ended");
    }

    private void readMapFromImage(AdvancedGridValueLayer exploredArea,
	    String image) {
	try {
	    BufferedImage map = javax.imageio.ImageIO.read(new File(image));

	    for (int x = 0; x < map.getWidth(); x++) {
		for (int y = 0; y < map.getHeight(); y++) {
		    double pixelValue = map.getData().getSampleDouble(x, y, 0);

		    if (pixelValue == AdvancedGridValueLayer.ObstaclePixelValue)
			exploredArea.setFieldType(FieldType.Obstacle, x, y);
		    else
			exploredArea.setFieldType(FieldType.Default, x, y);
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }
}
