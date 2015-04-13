package swarm_sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.Dimensions;
import repast.simphony.space.continuous.NdPoint;

public class QuadrantMap {

	private double width, height, binWidth, binHeight;
	private int binsX, binsY;
	
	private Quadrant quadrants [][];
	
	public Quadrant target = null;

	public QuadrantMap(Dimensions dimensions, int binsX, int binsY, double quadrantDoneValue) {
		this.width = dimensions.getWidth();
		this.height = dimensions.getHeight();
		this.binsX = binsX;
		this.binsY = binsY;
		this.quadrants = new Quadrant[binsX][binsY];
		this.binWidth = width/binsX;
		this.binHeight = height/binsY;
		
		for (int x = 0; x < binsX; x++) {
			for (int y = 0; y < binsY; y++) {
				quadrants[x][y] = new Quadrant(x, y, binWidth, binHeight, quadrantDoneValue);
			}
		}
	}
	
	public Quadrant locationToQuadrant(NdPoint location) {
		int x = (int)(location.getX() / binWidth);
		int y = (int)(location.getY() / binHeight);
		return quadrants[x][y];
	}
	
	public double getBinArea() {
		return binWidth*binHeight;
	}
	
	public boolean isDone(Quadrant q) {
		for (int x = 0; x < binsX; x++) {
			for (int y = 0; y < binsY; y++) {
				if(!quadrants[x][y].isDone())
					return false;
			}
		}
		return true;
	}
	
	public void setData(NdPoint location, double data) {
		Quadrant q = locationToQuadrant(location);
		quadrants[q.x][q.y].data = data;
	}
	
	public void incrementData(NdPoint location, double data) {
		Quadrant q = locationToQuadrant(location);
		quadrants[q.x][q.y].data += data;
	}
	
	public double getData(Quadrant q) {
		return quadrants[q.x][q.y].data;
	}
	
	public List<Quadrant> getNeighboringQuadrants(Quadrant q, int degree) {
		List<Quadrant> neighbors = new ArrayList<Quadrant>();
		
		for (int x = q.x - degree; x <= q.x + degree; x++) {
			int yUpper = q.y + degree;
			int yLower = q.y - degree;
			
			if(x >= 0 && x < binsX && yUpper < binsY)
				neighbors.add(quadrants[x][yUpper]);
			if(x >= 0 && x < binsX && yLower >= 0)
				neighbors.add(quadrants[x][yLower]);
		}
		
		for (int y = q.y - degree + 1; y <= q.y + degree - 1; y++) {
			int xLeft = q.x - degree;
			int xRight = q.x + degree;
			
			if(y >= 0 && y < binsY && xLeft >= 0)
				neighbors.add(quadrants[xLeft][y]);
			if(y >= 0 && y < binsY && xRight < binsX)
				neighbors.add(quadrants[xRight][y]);
		}
		
		return neighbors;
	}
	
	public Quadrant getCloseQuadrant(Quadrant q, double maxValue) {
		List<Quadrant> neighbors = null;
		int degree = 1;
		
		do {
			neighbors = getNeighboringQuadrants(q, degree++);
			Collections.shuffle(neighbors);
			for (Quadrant quadrant : neighbors) {
				if(quadrant.data < maxValue) {
					return quadrant;
				}
			}
		}
		while(neighbors.size() > 0);
		
		return null;
	}

//	public List<Quadrant> getCloseQuadrants(Quadrant q) {
//		List<Quadrant> neighbors = new ArrayList<Quadrant>();
//		
//		boolean hasRight = q.x < binsX - 1;
//		boolean hasLeft = q.x > 0;
//		boolean hasTop = q.y < binsY - 1;
//		boolean hasBottom = q.y > 0;
//		
//		if(hasLeft) {
//			neighbors.add(quadrants[q.index-1]);
//			if(hasTop)
//				neighbors.add(quadrants[q.index + binsX -1]);
//			if(hasBottom)
//				neighbors.add(quadrants[q.index - binsX -1]);
//		}
//		if(hasRight) {
//			neighbors.add(quadrants[q.index+1]);
//			if(hasTop)
//				neighbors.add(quadrants[q.index + binsX +1]);
//			if(hasBottom)
//				neighbors.add(quadrants[q.index - binsX +1]);
//		}
//		return neighbors;
//	}

	public Quadrant getRandomQuadrant() {
		return quadrants[RandomHelper.nextIntFromTo(0, binsX - 1)][RandomHelper.nextIntFromTo(0, binsY - 1)];
	}

	public void merge(QuadrantMap data) {
		for (int x = 0; x < binsX; x++) {
			for (int y = 0; y < binsY; y++) {
				if(quadrants[x][y].doUpdate && data.quadrants[x][y].data > quadrants[x][y].data)
					quadrants[x][y].data = data.quadrants[x][y].data;
			}
		}
	}	
}
