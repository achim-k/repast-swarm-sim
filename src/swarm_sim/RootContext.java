package swarm_sim;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import swarm_sim.AdvancedGridValueLayer.FieldType;
import swarm_sim.foraging.Resource;
import swarm_sim.learning.GA;

/**
 * This class creates the global context, used by every config. It creates the
 * space where the agents move, value layers etc.
 * 
 * @author achim
 * 
 */
public class RootContext implements ContextBuilder<AbstractAgent> {
    protected AdvancedGridValueLayer exploredArea;
    protected ContinuousSpace<AbstractAgent> space;

    public Context<AbstractAgent> build(Context<AbstractAgent> context) {
	/* Set context id */
	context.setId("root");

	/* get parameters */
	RunEnvironment runEnv = RunEnvironment.getInstance();
	Parameters params = runEnv.getParameters();
	Configuration config = Configuration.getInstance();
	DataCollection data = DataCollection.getInstance();

	config.reset();
	data.reset();
	context.add(data);

	/* Read params */
	config.agentCount = params.getInteger("agent_count");

	/* Do not run more than XXXX ticks */
	config.maxTicks = 50000 / config.agentCount;

	if (config.maxTicks < 5000)
	    config.maxTicks = 5000;

	runEnv.endAt(config.maxTicks);

	config.k = params.getDouble("k");
	config.segmentCount = params.getInteger("segment_count");
	config.distanceFactor = params.getDouble("distance_factor");
	config.initProb = params.getDouble("prob_init");

	config.consecutiveMoves = params.getInteger("consecutive_move");
	config.useGA = params.getBoolean("use_ga");
	config.commFreq = params.getInteger("comm_frequency");
	config.printConfig = params.getBoolean("print_params");

	String dimensions[] = params.getString("space_dimensions").split(":");

	config.spaceWidth = Integer.parseInt(dimensions[0]);
	config.spaceHeight = Integer.parseInt(dimensions[1]);

	double spaceDiagonalLength = Math.sqrt(config.spaceWidth
		* config.spaceWidth + config.spaceHeight * config.spaceHeight);
	/*
	 * communication and perception scope are relative to max distance
	 * (which is the diagonal)
	 */
	config.perceptionScope = spaceDiagonalLength
		* params.getDouble("perception_scope") / 100;
	config.commScope = spaceDiagonalLength
		* params.getDouble("communication_scope") / 100;

	config.explStrat = params.getString("expl_strategy");
	config.foragingStrat = params.getString("foraging_strategy");

	config.resourceNestCount = params.getInteger("resource_nest_count");
	config.resourceCount = params.getInteger("resource_count");

	config.failureProbability = params.getDouble("failure_probability");

	config.diffusion_rate = params.getDouble("diffusion_rate");
	config.evaporation_rate = params.getDouble("evaporation_rate");

	config.takeHighestProb = params.getBoolean("highest_prob");

	/* Create continuous space */
	ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
		.createContinuousSpaceFactory(null);

	space = spaceFactory.createContinuousSpace("space_continuous", context,
		new RandomCartesianAdder<AbstractAgent>(),
		new repast.simphony.space.continuous.BouncyBorders(),
		config.spaceWidth, config.spaceHeight);

	/* Value layer to track explored area, default: 0.0 */
	exploredArea = new AdvancedGridValueLayer("layer_explored", 0.0, false,
		config.spaceWidth, config.spaceHeight);
	context.addValueLayer(exploredArea);
	readMapFromImage(exploredArea, params.getString("map"));

	/* Value layer to store pheromones default: 0.0 */
	AdvancedGridValueLayer pheromoneLayer = new AdvancedGridValueLayer(
		"layer_pheromones", 0.0, false, config.spaceWidth,
		config.spaceHeight);
	context.addValueLayer(pheromoneLayer);

	PseudoRandomAdder<AbstractAgent> adder = new PseudoRandomAdder<AbstractAgent>(
		exploredArea);
	adder.setRandomAdderSaveClass(Base.class);

	adder.setResourceAdderSaveClass(Resource.class,
		config.resourceNestCount, 1, space);
	space.setAdder(adder);

	/* add Resources */
	for (int i = 0; i < config.resourceCount; i++) {
	    context.add(new Resource());
	}

	/* spawn base */
	ISchedule schedule = runEnv.getCurrentSchedule();
	Base base = new Base();
	schedule.schedule(ScheduleParameters.createRepeating(1, 1,
		ScheduleParameters.LAST_PRIORITY), base, "step");
	context.add(base);
	config.baseAgent = base;

	/*
	 * Create comm network (holds only edges of agents which are within
	 * communication range
	 */
	NetworkBuilder<AbstractAgent> builder = new NetworkBuilder<>(
		"network_comm", context, true);
	builder.buildNetwork();

	ScheduleParameters agentScheduleParams = ScheduleParameters
		.createRepeating(0, 1);

	List<AbstractAgent> networkAgents = new ArrayList<>();
	// networkAgents.add(base);

	GA ga = GA.getInstance();

	/* add agents */
	for (int i = 0; i < config.agentCount; i++) {
	    AbstractAgent agent;
	    if (config.useGA)
		agent = new Agent(context, ga.currentChromosome);
	    else
		agent = new Agent(context, null);

	    networkAgents.add(agent);
	    schedule.schedule(agentScheduleParams, agent, "step");
	    context.add(agent);
	}

	/* add simulation control */
	SimulationControl simControl = new SimulationControl(context,
		networkAgents);
	schedule.schedule(ScheduleParameters.createRepeating(0, 5,
		ScheduleParameters.FIRST_PRIORITY), simControl,
		"checkIfSimFinished");
	schedule.schedule(ScheduleParameters.createRepeating(0,
		config.commFreq, ScheduleParameters.FIRST_PRIORITY),
		simControl, "recalculateNetworkEdges");
	context.add(simControl);

	if (config.foragingStrat.equalsIgnoreCase("PC")) {
	    schedule.schedule(ScheduleParameters.createRepeating(0, 10),
		    simControl, "diffusePheromones");
	}

	/* schedule action which is called at end of simulation */
	schedule.schedule(ScheduleParameters
		.createAtEnd(ScheduleParameters.LAST_PRIORITY), this,
		"endAction");

	if (config.printConfig) {
	    /* Print config */
	    System.out.println();
	    System.out.println("Foraging context loaded");
	    System.out.println("-----------------------------------");
	    System.out.println("Number of Agents:      \t" + config.agentCount);
	    System.out.println("Exploration Strategy:  \t" + config.explStrat);
	    System.out.println("Foraging Strategy:     \t"
		    + config.foragingStrat);
	    System.out.println("Dimensions:            \t" + config.spaceWidth
		    + "," + config.spaceHeight);
	    System.out.println("Resources/Nests:       \t"
		    + config.resourceCount + "/" + config.resourceNestCount);
	    System.out.println("Perception-Scope:      \t"
		    + config.perceptionScope);
	    System.out.println("Communic.-Scope:       \t" + config.commScope);
	    System.out.println("Communic.-Frequency:   \t" + config.commFreq);
	    System.out.println("Consecutive moves:     \t"
		    + config.consecutiveMoves);
	    System.out.println("N (segment count)      \t"
		    + config.segmentCount);
	    System.out.println("k                      \t" + config.k);
	    System.out.println("D (distance factor)    \t"
		    + config.distanceFactor);
	    System.out.println("initial segment prob   \t" + config.initProb);

	    System.out.println("Use Genetic Alg.:      \t" + config.useGA);
	    if (config.useGA)
		System.out.println("Chromosomes:       	\t:"
			+ ga.currentChromosome);
	}

	System.out.println("Run #" + (++data.runCount));

	return context;
    }

    public void endAction() {
	RunEnvironment runenv = RunEnvironment.getInstance();
	DataCollection data = DataCollection.getInstance();

	String out = String.format("Took %d ticks (%.1f s)", (int) runenv
		.getCurrentSchedule().getTickCount(),
		data.getTotalExecTime() / 1E9);
	System.out.println(out);
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
