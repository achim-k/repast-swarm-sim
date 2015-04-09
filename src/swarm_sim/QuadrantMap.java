package swarm_sim;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.Dimensions;
import repast.simphony.space.continuous.NdPoint;

public class QuadrantMap {

	private double width, height, binWidth, binHeight;
	private int binsX, binsY;
	
	private Quadrant quadrants [];

	public QuadrantMap(Dimensions dimensions, int binsX, int binsY) {
		this.width = dimensions.getWidth();
		this.height = dimensions.getHeight();
		this.binsX = binsX;
		this.binsY = binsY;
		this.quadrants = new Quadrant[binsX*binsY];
		this.binWidth = width/binsX;
		this.binHeight = height/binsY;
		
		for (int i = 0; i < quadrants.length; i++) {
			int x = i % binsX;
			int y = (int)(i/binsX);
			double centerX = binWidth*(x + 0.5);
			double centerY = binHeight*(y + 0.5);
			quadrants[i] = new Quadrant(i, x, y, centerX, centerY);
		}
	}
	
	public Quadrant locationToQuadrant(NdPoint location) {
		int x = (int)(location.getX() / binWidth);
		int y = (int)(location.getY() / binHeight);
		return quadrants[y*binsX + x];
	}
	
	public double getBinArea() {
		return binWidth*binHeight;
	}
	
	public void setData(NdPoint location, double data) {
		Quadrant q = locationToQuadrant(location);
		quadrants[q.index].data = data;
	}
	
	public void incrementData(NdPoint location, double data) {
		Quadrant q = locationToQuadrant(location);
		quadrants[q.index].data += data;
	}
	
	public double getData(Quadrant q) {
		return quadrants[q.index].data;
	}

	public List<Quadrant> getCloseQuadrants(Quadrant q) {
		List<Quadrant> neighbors = new ArrayList<Quadrant>();
		
		boolean hasRight = q.x < binsX - 1;
		boolean hasLeft = q.x > 0;
		boolean hasTop = q.y < binsY - 1;
		boolean hasBottom = q.y > 0;
		
		if(hasLeft) {
			neighbors.add(quadrants[q.index-1]);
			if(hasTop)
				neighbors.add(quadrants[q.index + binsX -1]);
			if(hasBottom)
				neighbors.add(quadrants[q.index - binsX -1]);
		}
		if(hasRight) {
			neighbors.add(quadrants[q.index+1]);
			if(hasTop)
				neighbors.add(quadrants[q.index + binsX +1]);
			if(hasBottom)
				neighbors.add(quadrants[q.index - binsX +1]);
		}
		return neighbors;
	}

	public Quadrant getRandomQuadrant() {
		return quadrants[RandomHelper.nextIntFromTo(0, quadrants.length - 1)];
	}

	public void merge(QuadrantMap data) {
		for (Quadrant q : data.quadrants) {
			if(q.data > quadrants[q.index].data)
				quadrants[q.index].data = q.data;
		}
	}	
}
