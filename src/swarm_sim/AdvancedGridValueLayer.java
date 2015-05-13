package swarm_sim;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.space.SpatialException;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.valueLayer.GridFunction;
import repast.simphony.valueLayer.GridValueLayer;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;

public class AdvancedGridValueLayer extends GridValueLayer {

    public static final double ObstaclePixelValue = 0;

    protected DoubleMatrix1D fieldTypeMatrix;

    private int obstacleCount = 0;

    public enum FieldType {
	Default, Obstacle
    }

    public class FieldDistancePair {
	public double distance, value;
	public FieldType fieldType;
	public int x, y;

	public FieldDistancePair(int x, int y, FieldType fieldType,
		double value, double distance) {
	    this.x = x;
	    this.y = y;
	    this.fieldType = fieldType;
	    this.value = value;
	    this.distance = distance;
	}
    }

    public AdvancedGridValueLayer(String name, boolean dense, int[] dimensions) {
	super(name, dense, dimensions);

	int _size = 1;
	for (int dim : dimensions) {
	    _size *= dim;
	}

	if (dense)
	    fieldTypeMatrix = new DenseDoubleMatrix1D(_size);
	else
	    fieldTypeMatrix = new SparseDoubleMatrix1D(_size);
	fieldTypeMatrix.assign(FieldType.Default.ordinal());
    }

    public AdvancedGridValueLayer(String name, double defaultValue,
	    boolean dense, int... dimensions) {
	super(name, defaultValue, dense, dimensions);

	int _size = 1;
	for (int dim : dimensions) {
	    _size *= dim;
	}

	if (dense)
	    fieldTypeMatrix = new DenseDoubleMatrix1D(_size);
	else
	    fieldTypeMatrix = new SparseDoubleMatrix1D(_size);
	fieldTypeMatrix.assign(FieldType.Default.ordinal());
    }

    public void setFieldType(FieldType fieldType, int... coordinate) {
	if (coordinate.length != dims.size())
	    throw new SpatialException("Invalid number coordinates");
	int index = getIndex(getTransformedLocation(coordinate));
	fieldTypeMatrix.set(index, fieldType.ordinal());

	if (fieldType == FieldType.Obstacle)
	    obstacleCount++;
    }

    public FieldType getFieldType(double... coordinates) {

	int[] coords = new int[coordinates.length];

	for (int i = 0; i < coordinates.length; i++)
	    coords[i] = (int) coordinates[i];

	int index = getIndex(getTransformedLocation(coords));
	int val = (int) fieldTypeMatrix.get(index);

	if (val == FieldType.Obstacle.ordinal())
	    return FieldType.Obstacle;

	return FieldType.Default;
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
	    int min = (int) (origin.getCoord(i) - radius);
	    if (min < dims.getOrigin(i))
		min = (int) dims.getOrigin(i);
	    mins[i] = min;

	    int max = (int) (origin.getCoord(i) + radius);
	    if (max >= dims.getOrigin(i) + dims.getDimension(i))
		max = (int) (dims.getOrigin(i) + dims.getDimension(i)) - 1;
	    maxs[i] = max;
	}

	for (int x = mins[0]; x <= maxs[0]; x++) {
	    for (int y = mins[1]; y <= maxs[1]; y++) {
		double distanceSquared = Math.pow((x - origin.getX()), 2)
			+ Math.pow((y - origin.getY()), 2);
		if (Math.sqrt(distanceSquared) <= radius)
		    function.apply(get(x, y), x, y);
	    }
	}
    }

    public List<FieldDistancePair> getFieldsRadial(NdPoint origin, double radius) {
	List<FieldDistancePair> ret = new ArrayList<>();

	int dimsSize = dims.size();

	int[] mins = new int[dimsSize];
	int[] maxs = new int[dimsSize];

	for (int i = 0; i < dimsSize; i++) {
	    int min = (int) Math.round(origin.getCoord(i) - radius);
	    // if (min < dims.getOrigin(i))
	    // min = (int) dims.getOrigin(i);
	    mins[i] = min;

	    int max = (int) Math.round(origin.getCoord(i) + radius) - 1;
	    // if (max >= dims.getOrigin(i) + dims.getDimension(i))
	    // max = (int) (dims.getOrigin(i) + dims.getDimension(i)) - 1;
	    maxs[i] = max;
	}

	for (int x = mins[0]; x <= maxs[0]; x++) {
	    for (int y = mins[1]; y <= maxs[1]; y++) {
		double distance = Math.sqrt(Math.pow((x + .5 - origin.getX()),
			2) + Math.pow((y + .5 - origin.getY()), 2));

		if (x < dims.getOrigin(0) || x >= dims.getDimension(0)
			|| y < dims.getOrigin(1) || y >= dims.getDimension(1)) {
		    ret.add(new FieldDistancePair(x, y, FieldType.Obstacle, -1,
			    distance));
		    continue;
		}

		if (distance <= radius) {
		    ret.add(new FieldDistancePair(x, y, getFieldType(x, y),
			    get(x, y), distance));
		}
	    }
	}

	return ret;
    }

    public boolean isObstacleOnLine(NdPoint origin, NdPoint target) {
	List<FieldDistancePair> ret = new ArrayList<>();

	double diffX = target.getX() - origin.getX();
	double diffY = target.getY() - origin.getY();
	double distance = Math.sqrt(diffX * diffX + diffY * diffY);
	/* normalize */
	diffX /= distance;
	diffY /= distance;

	double x = origin.getX();
	double y = origin.getY();
	while (Math.abs(x - target.getX()) > 1
		|| Math.abs(y - target.getY()) > 1) {
	    if (getFieldType(x, y) == FieldType.Obstacle) {
		return true;
	    }

	    x += diffX;
	    y += diffY;
	}

	return false;
    }

    // returns the index into the matrix given a point
    // this casts the double to an int
    private int getIndex(int... point) {
	int[] matrixPoint = new int[point.length];
	int[] origin = this.dims.originToIntArray(null);
	for (int i = 0; i < point.length; i++) {
	    matrixPoint[i] = point[i] + origin[i];
	}
	int index = 0;
	for (int i = 0; i < matrixPoint.length; i++) {
	    index = index + (int) matrixPoint[i] * stride[i];
	}
	return index;
    }

    public int getObstacleFieldCount() {
	return obstacleCount;
    }

}
