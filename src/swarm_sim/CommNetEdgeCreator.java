package swarm_sim;

import repast.simphony.space.graph.EdgeCreator;

public class CommNetEdgeCreator implements EdgeCreator<CommNetEdge<Robot>, Robot> {

	@Override
	public Class<CommNetEdge> getEdgeType() {
		// TODO Auto-generated method stub
		return CommNetEdge.class;
	}

	@Override
	public CommNetEdge<Robot> createEdge(Robot source, Robot target, boolean isDirected, double weight) {
		// TODO Auto-generated method stub
		return new CommNetEdge<Robot>(source, target, isDirected, weight);
	}
	
	

}
