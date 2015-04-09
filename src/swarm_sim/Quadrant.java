package swarm_sim;

import repast.simphony.space.continuous.NdPoint;

public class Quadrant {
	public int index, x, y;
	public NdPoint center;
	public double data;

	public Quadrant(int index, int x, int y, double centerX, double centerY) {
		this.index = index;
		this.x = x;
		this.y = y;
		this.center = new NdPoint(centerX, centerY);
	}
}
