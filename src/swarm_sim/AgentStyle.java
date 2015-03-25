package swarm_sim;

import java.awt.Color;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

public class AgentStyle extends DefaultStyleOGL2D{

	@Override
	public String getLabel(Object agent) {
		return ((DisplayAgent)agent).getName();
	}
	
	@Override
	public Color getColor(Object agent) {
		return ((DisplayAgent)agent).getColor();
	}
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
	    if (spatial == null) {
	    	spatial = ((DisplayAgent)agent).getShape(shapeFactory);
	    }
	    return spatial;
	  }
	
	@Override
	public float getScale(Object object) {
		return 3;
	}
	
	@Override
	public Color getBorderColor(Object object) {
		return Color.BLACK;
	}
	
	@Override
	public int getBorderSize(Object object) {
		return 4;
	}
}
