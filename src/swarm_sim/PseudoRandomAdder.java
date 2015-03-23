/*CopyrightHere*/
package swarm_sim;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.Dimensions;
import repast.simphony.space.continuous.ContinuousAdder;
import repast.simphony.space.continuous.ContinuousSpace;

/**
 * This will place certain objects at a random location in the space. Other objects will be 
 * placed at the same location as the object class defined with setRandomAdderSaveClass
 */
public class PseudoRandomAdder<T> implements ContinuousAdder<T> {
	
	Class randomAddSaveClass;
	boolean randomAddSaveAdded = false;
	double[] savedLocation = new double[2];
	List<Class> randomAddClasses = new ArrayList<>();
	List<T> addQueue = new ArrayList<>();
	
	
	/**
	 * Adds the specified object to the space at a random location.
	 * 
	 * @param space
	 *            the space to add the object to.
	 * @param obj
	 *            the object to add.
	 */
	public void add(ContinuousSpace<T> space, T obj) {
		if(obj.getClass() == randomAddSaveClass) {
			savedLocation = addRandom(space, obj);
			randomAddSaveAdded = true;
			for (T o : addQueue) {
				space.moveTo(o, savedLocation);
			}
			
		} else if(randomAddClasses.contains(obj.getClass())) {
			addRandom(space, obj);
		} else {
			if(!randomAddSaveAdded) {
				addQueue.add(obj);
				return;
			}
			space.moveTo(obj, savedLocation);
		}
	}
	
	private double[] addRandom(ContinuousSpace<T> space, T obj) {
		Dimensions dims = space.getDimensions();
		double[] location = new double[dims.size()];
		findLocation(location, dims);
		while (!space.moveTo(obj, location)) {
			findLocation(location, dims);
		}
		return location;
	}
	
	/* Add class types which should be added randomly */
	public void addRandomAdderClass(Class c) {
		randomAddClasses.add(c);
	}
	
	/* Add class types which should be added randomly but all other classes will be added on same location  */
	public void setRandomAdderSaveClass(Class c) {
		randomAddSaveClass = c;
	}

	private void findLocation(double[] location, Dimensions dims) {
		double[] origin = dims.originToDoubleArray(null);
		for (int i = 0; i < location.length; i++) {
			try{
				location[i] = RandomHelper.getUniform().nextDoubleFromTo(0, dims.getDimension(i)) - origin[i];
			}
			catch(Exception e){
				
			}
		}
	}
}
