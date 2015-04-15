package swarm_sim;

import java.awt.Color;

import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.VSpatial;

public interface DisplayAgent {
    public Color getColor();

    String getName();

    public VSpatial getShape(ShapeFactory2D shapeFactory);
}
