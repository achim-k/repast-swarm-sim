package swarm_sim;

import java.awt.Color;
import java.awt.Shape;

import repast.simphony.visualization.gui.styleBuilder.IconFactory2D;
import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.VSpatial;

import com.vividsolutions.jts.geom.Geometry;

public class Pheromone implements Agent, DisplayAgent {

	
	public Pheromone() {
	}
	
	@Override
	public String getName() {
		return "Pheromone agent";
	}
	
	@Override
	public AgentType getAgentType() {
		return AgentType.Pheromone;
	}

	@Override
	public Color getColor() {
		return null;
	}
	
	@Override
	public VSpatial getShape(ShapeFactory2D shapeFactory) {
		Shape shape = IconFactory2D.getShape("X");
		return shapeFactory.createShape(shape);
	}
}
