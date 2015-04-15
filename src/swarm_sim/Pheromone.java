package swarm_sim;

import java.awt.Color;

import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.VSpatial;

public class Pheromone implements Agent {

    public Pheromone() {
    }

    @Override
    public AgentType getAgentType() {
	return AgentType.Pheromone;
    }
}
