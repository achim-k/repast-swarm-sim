package swarm_sim;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.RepastEdge;


public class CommNetEdge<T> extends RepastEdge<T> {
	
	private int checkTickCount = 0;
	private boolean isActive = false;
	
	public CommNetEdge(T source, T target, ContinuousSpace<T> space, double commScope, double agentSpeed) {
		super(source, target, false);
		checkIfActive(space, commScope, agentSpeed);
	}
	
	public boolean isActive(ContinuousSpace<T> space, double commScope, double agentSpeed) {
		checkTickCount--;
//		if(checkTickCount <= 0) {
			checkIfActive(space, commScope, agentSpeed);
//		}
		
		double distance = space.getDistance(space.getLocation(source), space.getLocation(target));
		System.out.println("commScope: " + commScope + " distance: " + distance+ " isActive: " + isActive);
		if(distance > commScope && isActive == true) {
			System.err.println("ohoh.... zu groß aber active");
		}
		if(distance <= commScope && isActive == false) {
			System.err.println("ohoh.... zu klein aber not active");
		}
		return isActive;
	}
	
	private void checkIfActive(ContinuousSpace<T> space, double commScope, double agentSpeed) {
		double distance = space.getDistance(space.getLocation(source), space.getLocation(target));
		if(distance <= commScope)
			isActive = true;
		else
			isActive = false;
		
		/* set number of ticks when edge distance needs to be checked again */
		double delta = Math.abs(commScope - distance);
		checkTickCount = (int)(delta/(2*agentSpeed));
	}
	

}
