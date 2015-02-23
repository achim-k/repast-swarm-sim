package swarm_sim;

import java.awt.Color;
import java.awt.Font;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;

public class RobotStyle extends DefaultStyleOGL2D {

	@Override
	public String getLabel(Object agent) {
		Robot rob = (Robot) agent;
		return rob.getName();
	}
	
	@Override
	public Color getColor(Object agent) {
		Robot rob = (Robot) agent;
		int numberOfEdges = rob.getEdgeCount();
		
		if(numberOfEdges <= 2)
			return Color.BLUE;
		if(numberOfEdges <= 4)
			return Color.YELLOW;
		if(numberOfEdges <= 6)
			return Color.ORANGE;
		
		return Color.RED;
	}
	
}
