package swarm_sim;

import repast.simphony.space.continuous.NdPoint;

public class Quadrant {
	public int x, y;
	public double quadrantWidth, quadrantHeight;
	public double data;
	public boolean doUpdate = true;

	public Quadrant(int x, int y, double quadrantWidth, double quadrantHeight) {
		this.x = x;
		this.y = y;
		this.quadrantWidth = quadrantWidth;
		this.quadrantHeight = quadrantHeight;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != Quadrant.class)
			return false;

		Quadrant q = (Quadrant) obj;
		if (q.x == x && q.y == y)
			return true;

		return false;
	};

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

	public NdPoint getCenter() {
		return new NdPoint(quadrantWidth * (x + 0.5), quadrantHeight
				* (y + 0.5));
	}

	public NdPoint getLowerLeftCorner(double perceptionScope) {
		return new NdPoint(quadrantWidth * (x + 0) + perceptionScope,
				quadrantHeight * (y + 0) + perceptionScope);
	}

	public NdPoint getLowerRightCorner(double perceptionScope) {
		return new NdPoint(quadrantWidth * (x + 1) - perceptionScope,
				quadrantHeight * (y + 0) + perceptionScope);
	}

	public NdPoint getUpperLeftCorner(double perceptionScope) {
		return new NdPoint(quadrantWidth * (x + 0) + perceptionScope,
				quadrantHeight * (y + 1) - perceptionScope);
	}

	public NdPoint getUpperRightCorner(double perceptionScope) {
		return new NdPoint(quadrantWidth * (x + 1) - perceptionScope,
				quadrantHeight * (y + 1) - perceptionScope);
	}

	public double getLocationX(NdPoint location) {
		double deltaX = location.getX() - x * quadrantWidth;
		return (deltaX / quadrantWidth);
	}

	public double getLocationY(NdPoint location) {
		double deltaY = location.getY() - y * quadrantHeight;
		return (deltaY / quadrantWidth);
	}

}
