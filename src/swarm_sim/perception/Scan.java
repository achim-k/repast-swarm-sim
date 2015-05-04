package swarm_sim.perception;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class Scan {

    public class ScanInput {
	public double angle, distance, distanceRatio;

	public ScanInput(double angle, double distance) {
	    super();
	    this.angle = angle;
	    this.distance = distance;
	}
    }

    public enum AttractionType {
	Attracting, Repelling
    }

    public enum GrowingDirection {
	Inwards, Outwards
    }

    private double shortestDistance;
    private double hardInnerDistanceBorder, hardOuterDistanceBorder;

    private AttractionType attrType = AttractionType.Attracting;
    private GrowingDirection growDirection = GrowingDirection.Outwards;

    private boolean hasFluidBorders = false, isValid = false;
    private double mergeWeight;
    private int inputValidLowerBound, inputValidUpperBound;

    private List<ScanInput> inputs = new ArrayList<>();

    public Scan(AttractionType attrType, GrowingDirection growDirection,
	    double mergeWeight, boolean hasFluidSoftBorders,
	    double hardInnerDistanceBorder, double hardOuterDistanceBorder,
	    int inputValidLowerBound, int inputValidUpperBound) {
	super();
	this.growDirection = growDirection;
	this.hardInnerDistanceBorder = hardInnerDistanceBorder;
	this.hardOuterDistanceBorder = hardOuterDistanceBorder;
	this.attrType = attrType;
	this.hasFluidBorders = hasFluidSoftBorders;
	this.mergeWeight = mergeWeight;
	this.inputValidLowerBound = inputValidLowerBound;
	this.inputValidUpperBound = inputValidUpperBound;

	if (hasFluidSoftBorders) {
	    shortestDistance = 1E5;
	} else {
	    shortestDistance = hardInnerDistanceBorder;
	    if (shortestDistance <= 0)
		shortestDistance = 1;
	}
    }

    public void addInput(double angle, double distance) {
	if (distance < hardInnerDistanceBorder
		|| distance > hardOuterDistanceBorder || distance <= 0)
	    return;

	if (hasFluidBorders && distance < shortestDistance) {
	    shortestDistance = distance;
	}

	ScanInput input;
	if (attrType == AttractionType.Repelling) {
	    double newAngle = normAngle(angle + Math.PI); // shift angle by 180°
	    input = new ScanInput(newAngle, distance);
	} else
	    input = new ScanInput(angle, distance);

	inputs.add(input);

	if (!isValid && inputs.size() >= inputValidLowerBound)
	    isValid = true;
	if (isValid && inputs.size() > inputValidUpperBound)
	    isValid = false;
    }

    public void addInput(double angle) {
	addInput(angle, shortestDistance);
    }

    public List<ScanInput> getInputs() {
	List<ScanInput> ret = new ArrayList<Scan.ScanInput>();

	if (!isValid)
	    return ret;

	for (ScanInput i : inputs) {

	    double dRatio = 0;
	    if (hasFluidBorders) {
		dRatio = shortestDistance / i.distance;
	    } else {
		dRatio = (hardOuterDistanceBorder - i.distance)
			/ (hardOuterDistanceBorder - hardInnerDistanceBorder);
	    }

	    if (growDirection == GrowingDirection.Outwards)
		dRatio = 1 - dRatio;

	    i.distanceRatio = dRatio;
	    ret.add(i);
	}

	return ret;
    }

    public void clear() {
	inputs.clear();
    }

    public static double normAngle(double angle) {
	if (angle > Math.PI)
	    return -Math.PI + angle - Math.PI;
	if (angle < -Math.PI)
	    return Math.PI - (-Math.PI - angle);
	return angle;
    }

    public double getMergeWeight() {
	return mergeWeight;
    }

    public boolean isValid() {
	return isValid;
    }

    public GrowingDirection getGrowingDirection() {
	return growDirection;
    }
}
