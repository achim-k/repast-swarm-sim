package swarm_sim;

import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.valueLayer.GridFunction;
import repast.simphony.valueLayer.GridValueLayer;

public class AdvancedGridValueLayer extends GridValueLayer {

	public AdvancedGridValueLayer(String name, boolean dense, int[] dimensions) {
		super(name, dense, dimensions);
	}
	
	public AdvancedGridValueLayer(String name, double defaultValue, boolean dense, int... dimensions) {
		super(name, defaultValue, dense, dimensions);
	}
	
	/**
	 * Apply function to each gridpoint within the given radius from the origin
	 * 
	 * @param function
	 * @param origin
	 * @param radius
	 */
	public void forEachRadial(GridFunction function, NdPoint origin,
			double radius) {
		int dimsSize = dims.size();

		if (dimsSize != 2)
			throw new IllegalArgumentException("forEachRadial is only "
					+ "supported on 2D GridValueLayers");

		int[] mins = new int[dimsSize];
		int[] maxs = new int[dimsSize];

		for (int i = 0; i < dimsSize; i++) {
			int min = (int)(origin.getCoord(i) - radius);
			if (min < dims.getOrigin(i))
				min = (int) dims.getOrigin(i);
			mins[i] = min;

			int max = (int)(origin.getCoord(i) + radius);
			if (max >= dims.getOrigin(i) + dims.getDimension(i))
				max = (int) (dims.getOrigin(i) + dims.getDimension(i)) - 1;
			maxs[i] = max;
		}
		
		for (int x = mins[0]; x <= maxs[0]; x++) {
	        for (int y = mins[1]; y <= maxs[1]; y++) {
	        	double distanceSquared = Math.pow((x - origin.getX()), 2) + Math.pow((y - origin.getY()), 2);
	        	if(Math.sqrt(distanceSquared) <= radius)
	        		function.apply(get(x, y), x, y);
	        }
	      }
	}

}
