package swarm_sim;

import repast.simphony.util.collections.IndexedIterable;

import com.vividsolutions.jts.geom.Geometry;

public interface Agent {
	String getName();
	Geometry getGeometry();
}
