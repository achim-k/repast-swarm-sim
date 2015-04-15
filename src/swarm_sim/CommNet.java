package swarm_sim;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import repast.simphony.context.Context;
import repast.simphony.context.space.graph.ContextJungNetwork;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.graph.UndirectedJungNetwork;

public class CommNet<T> extends ContextJungNetwork<T> {

    public CommNet(String name, Context<T> context) {
	super(new UndirectedJungNetwork<T>(name), context);

    }

    public void updateEdges(Iterable<RepastEdge<T>> edges) {
	this.removeAll();
	for (RepastEdge<T> edge : edges) {
	    this.addEdge(edge);
	}

    }
}
