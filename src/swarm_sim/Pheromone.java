package swarm_sim;

import com.vividsolutions.jts.geom.Geometry;

public class Pheromone implements Agent {

	private Geometry geom;
	
	public Pheromone(Geometry geom) {
		this.geom = (Geometry) geom.clone();
	}
	
	@Override
	public String getName() {
		return "Pheromone agent";
	}

	@Override
	public Geometry getGeometry() {
		return geom;
	}
}
