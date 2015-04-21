/*CopyrightHere*/
package swarm_sim;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.Dimensions;
import repast.simphony.space.continuous.ContinuousAdder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import swarm_sim.AdvancedGridValueLayer.FieldType;

/**
 * This will place certain objects at a random location in the space. Other
 * objects will be placed at the same location as the object class defined with
 * setRandomAdderSaveClass
 */
public class PseudoRandomAdder<T> implements ContinuousAdder<T> {

    Class randomAddSaveClass;
    boolean randomAddSaveAdded = false;
    double[] savedLocation = new double[2];
    List<Class> randomAddClasses = new ArrayList<>();
    List<T> addQueue = new ArrayList<>();
    AdvancedGridValueLayer exploredArea;
    private int resourceNests;
    private Class resourceAdderSaveClass;
    double variance = 1;

    NdPoint resourceNestLocations[];

    public PseudoRandomAdder(AdvancedGridValueLayer exploredArea) {
	this.exploredArea = exploredArea;
    }

    /**
     * Adds the specified object to the space at a random location.
     * 
     * @param space
     *            the space to add the object to.
     * @param obj
     *            the object to add.
     */
    public void add(ContinuousSpace<T> space, T obj) {
	if (obj.getClass() == randomAddSaveClass) {
	    savedLocation = addRandom(space, obj);
	    randomAddSaveAdded = true;
	    for (T o : addQueue) {
		space.moveTo(o, savedLocation);
	    }

	} else if (obj.getClass() == resourceAdderSaveClass) {
	    addRandomClose(space, obj,
		    resourceNestLocations[RandomHelper.nextIntFromTo(0,
			    resourceNests - 1)]);
	} else if (randomAddClasses.contains(obj.getClass())) {
	    addRandom(space, obj);
	} else {
	    if (!randomAddSaveAdded) {
		addQueue.add(obj);
		return;
	    }
	    space.moveTo(obj, savedLocation);
	}
    }

    private void addRandomClose(ContinuousSpace<T> space, T obj, NdPoint ndPoint) {
	/* add object close to ndPoint */
	Dimensions dims = space.getDimensions();
	double[] location = new double[dims.size()];
	findLocationClose(location, dims, ndPoint);
	while (!space.moveTo(obj, location)
		|| exploredArea.getFieldType(location[0], location[1]) == FieldType.Obstacle) {
	    findLocationClose(location, dims, ndPoint);
	}
    }

    private double[] addRandom(ContinuousSpace<T> space, T obj) {
	Dimensions dims = space.getDimensions();
	double[] location = new double[dims.size()];
	findLocation(location, dims);
	while (!space.moveTo(obj, location)
		|| exploredArea.getFieldType(location[0], location[1]) == FieldType.Obstacle) {
	    findLocation(location, dims);
	}
	return location;
    }

    /* Add class types which should be added randomly */
    public void addRandomAdderClass(Class c) {
	randomAddClasses.add(c);
    }

    /*
     * Add class types which should be added randomly but all other classes will
     * be added on same location
     */
    public void setRandomAdderSaveClass(Class c) {
	randomAddSaveClass = c;
    }

    public void setResourceAdderSaveClass(Class c, int nestCount, double variance,
	    ContinuousSpace<T> space) {
	resourceAdderSaveClass = c;
	this.resourceNests = nestCount;
	this.variance = variance;
	Dimensions dims = space.getDimensions();

	resourceNestLocations = new NdPoint[nestCount];
	for (int i = 0; i < nestCount; i++) {
	    double[] location = new double[dims.size()];
	    findLocation(location, dims);
	    while (exploredArea.getFieldType(location[0], location[1]) == FieldType.Obstacle)
		findLocation(location, dims);
	    resourceNestLocations[i] = new NdPoint(location);
	}
    }

    private void findLocation(double[] location, Dimensions dims) {
	double[] origin = dims.originToDoubleArray(null);
	for (int i = 0; i < location.length; i++) {
	    try {
		location[i] = RandomHelper.getUniform().nextDoubleFromTo(0,
			dims.getDimension(i))
			- origin[i];
	    } catch (Exception e) {

	    }
	}
    }

    private void findLocationClose(double[] location, Dimensions dims,
	    NdPoint closeToPoint) {
	do {
	    for (int i = 0; i < location.length; i++) {
		try {
		    location[i] = RandomHelper.createNormal(closeToPoint.getCoord(i), variance).nextDouble();
		} catch (Exception e) {

		}
	    }
	} while (location[0] > dims.getWidth() || location[0] < 0
		|| location[1] < 0 || location[1] > dims.getHeight());
    }
}
