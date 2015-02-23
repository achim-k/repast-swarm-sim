package swarm_sim;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.*;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.valueLayer.ContinuousValueLayer;

public class SwarmContextBuilder implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		context.setId("swarm_sim");

		/* Create continuous space */
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(), 50,
				50);

		/* Create communication network */
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("comm_network", context, false);
		netBuilder.buildNetwork();

		/* Value layer for painting? */
		ContinuousValueLayer exploredArea = new ContinuousValueLayer(
				"explored_area", 50, 50);
		context.addValueLayer(exploredArea);

		/* add the agents */
		int agentCount = 15;
		for (int i = 0; i < agentCount; i++) {
			context.add(new Robot(context, space, exploredArea));
		}

		return context;
	}

}
