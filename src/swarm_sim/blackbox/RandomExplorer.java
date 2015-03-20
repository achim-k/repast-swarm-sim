package swarm_sim.blackbox;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.util.ContextUtils;
import swarm_sim.Agent;
import swarm_sim.DefaultAgent;

/**
 * Agent which explores the space randomly while searching for the blackbox. 
 * Doesn't care about other agents. Goes home to base directly when the blackbox was found
 * to communicate the location. 
 * @author achim
 *
 */
public class RandomExplorer extends DefaultAgent implements Agent {

	private static int agentNo = 1;
	
	public RandomExplorer(Context<Agent> context, Context<Agent> rootContext) {
		super(context, rootContext);
		agentNo++;
	}
	
	public void step() {
		move();
	}
	
	private void move() {
		double moveX = RandomHelper.nextDoubleFromTo(-1, 1);
		double moveY = RandomHelper.nextDoubleFromTo(-1, 1);
		super.spaceContinuous.moveByDisplacement(this, moveX, moveY);
		super.updateExploredLayer();
	}
	
	@Override
	public String getName() {
		return "RandomExplorer" + agentNo;
	}

	@Override
	public AgentType getAgentType() {
		return AgentType.BB_RandomExplorer;
	}

}
