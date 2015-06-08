package swarm_sim.perception;

import java.io.PrintWriter;
import java.util.List;

import repast.simphony.random.RandomHelper;
import swarm_sim.perception.PDDPInput.ScanInput;

public class PDDP {

    private class CircleSegment {
	public double centerAngle, probability;
	public boolean isValid = true;

	public CircleSegment(double centerAngle, double probability) {
	    super();
	    this.centerAngle = centerAngle;
	    this.probability = probability;
	}
    }

    private int segmentCount;
    private CircleSegment segments[];

    private double distanceFactor;
    private VonMises vonMises;
    private double centerDelta;
    private double initProbability;
    private boolean hasInputs = false;

    public PDDP(int segmentCount, double vonMisesDegree,
	    double distanceFactor, double initProb) {
	super();
	this.segmentCount = segmentCount;
	this.segments = new CircleSegment[segmentCount];
	this.distanceFactor = distanceFactor;
	this.initProbability = initProb;

	vonMises = new VonMises(vonMisesDegree);

	centerDelta = 2 * Math.PI / (segmentCount * 2.0);

	for (int segmentNo = 0; segmentNo < segments.length; segmentNo++) {
	    double centerAngle = -Math.PI + segmentNo * 2 * Math.PI
		    / segmentCount + centerDelta;
	    segments[segmentNo] = new CircleSegment(centerAngle, initProb);
	}
    }

    public void calcProbDist(PDDPInput... input) {

	double mergeWeightSum = 0;

	for (PDDPInput scan : input) {
	    if (scan.isValid())
		mergeWeightSum += scan.getMergeWeight();
	}

	if (mergeWeightSum <= 0)
	    return;

	for (PDDPInput scan : input) {
	    double mergeWeight = scan.getMergeWeight();
	    List<ScanInput> inputs = scan.getInputs();

	    for (ScanInput scanInput : inputs) {
		addScanInputProb(scanInput, mergeWeight
			/ (mergeWeightSum * scan.getInputs().size()));
	    }
	}
    }

    private void addScanInputProb(ScanInput input, double mergeFactor) {
	hasInputs = true;
	double distanceValue = 1 + (distanceFactor - 1) * input.distanceRatio;

	// int inputSegIndex = angleToSegmentIndex(input.angle, segmentCount);

	for (CircleSegment cs : segments) {
	    if (!cs.isValid)
		continue;

	    double vonMisesValue = vonMises.getValue(input.angle,
		    cs.centerAngle);
	    cs.probability += vonMisesValue * distanceValue * mergeFactor;
	}

    }

    public void printProbabilities(PrintWriter file) {
	System.out.println("CenterAngle\tProbability\tisValid");
	for (CircleSegment cs : segments) {
	    String fmt = String.format("%+.3f \t\t %.3f \t\t %b",
		    cs.centerAngle, cs.probability, cs.isValid);
	    if (file != null)
		file.println(fmt);
	    else
		System.out.println(fmt);
	}
    }

    public double getMovementAngle() {
	double rndSum = 0;
	double rnd = Math.random();

	for (CircleSegment cs : segments) {
	    if (!cs.isValid)
		continue;

	    rndSum += cs.probability;
	    if (rndSum >= rnd) {
		return RandomHelper.nextDoubleFromTo(cs.centerAngle
			- centerDelta, cs.centerAngle + centerDelta);
	    }
	}
	return -100;
    }

    public double getMovementAngleWithHighestProbability() {
	double maxProb = 0;
	CircleSegment maxProbSegment = null;

	for (CircleSegment cs : segments) {
	    if (!cs.isValid)
		continue;

	    if (cs.probability > maxProb) {
		maxProb = cs.probability;
		maxProbSegment = cs;
	    } else if (cs.probability == maxProb && Math.random() > 0.5) {
		maxProb = cs.probability;
		maxProbSegment = cs;
	    }
	}

	if (maxProbSegment != null)
	    return RandomHelper.nextDoubleFromTo(maxProbSegment.centerAngle
		    - centerDelta, maxProbSegment.centerAngle + centerDelta);

	return -100;
    }

    public static int angleToSegmentIndex(double angle, int segmentCount) {
	return (int) ((angle + Math.PI) / (2 * Math.PI / segmentCount));
    }
    
    public boolean angleValid(double angle) {
	return segments[angleToSegmentIndex(angle, segmentCount)].isValid;
    }

    public void normalize() {
	double segmentProbSum = 0;

	for (CircleSegment cs : segments) {
	    if (cs.isValid)
		segmentProbSum += cs.probability;
	}

	for (CircleSegment cs : segments) {
	    if (cs.isValid)
		cs.probability /= segmentProbSum;
	}
    }

    public void setValidSegments(List<AngleSegment> freeToGoSegments) {
	for (AngleSegment as : freeToGoSegments) {
	    int startIndex = angleToSegmentIndex(as.start, segmentCount);
	    int endIndex = angleToSegmentIndex(as.end, segmentCount);

	    for (int i = startIndex; i <= endIndex && i < segmentCount; i++) {
		segments[i].isValid = true;
	    }
	}

	/* set probability to 0 for invalid segments */
	for (CircleSegment cs : segments) {
	    if (!cs.isValid) {
		cs.probability = 0;
	    }
	}
    }
    
    public void setInvalidSegments(double angleStart, double angleEnd) {
	int startIndex = angleToSegmentIndex(angleStart, segmentCount);
	int endIndex = angleToSegmentIndex(angleEnd, segmentCount);
	
	if(startIndex > endIndex) {
	    for (int i = startIndex; i < segmentCount; i++) {
		segments[i].isValid = false;
	    }
	    for (int i = 0; i <= endIndex; i++) {
		segments[i].isValid = false;
	    }
	} else {
	    for (int i = startIndex; i <= endIndex; i++) {
		segments[i].isValid = false;
	    }
	}
    }

    public void clear() {
	hasInputs = false;
	for (CircleSegment cs : segments) {
	    cs.isValid = true;
	    cs.probability = initProbability;
	}
    }

    public boolean hasInputs() {
	return hasInputs;
    }

    public double getSegmentProbability(int segmentIndex) {
	return segments[segmentIndex].probability;
    }
}
