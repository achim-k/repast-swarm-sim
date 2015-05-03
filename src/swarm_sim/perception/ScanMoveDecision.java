package swarm_sim.perception;

import java.io.PrintWriter;
import java.util.List;

import repast.simphony.random.RandomHelper;
import swarm_sim.perception.Scan.ScanInput;

public class ScanMoveDecision {

    private class CircleSegment {
	public double centerAngle, probability;
	public boolean isValid = false;

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

    public ScanMoveDecision(int segmentCount, double vonMisesDegree,
	    double distanceFactor, double initProb) {
	super();
	this.segmentCount = segmentCount;
	this.segments = new CircleSegment[segmentCount];
	this.distanceFactor = distanceFactor;

	vonMises = new VonMises(vonMisesDegree);

	centerDelta = 2 * Math.PI / (segmentCount * 2.0);

	for (int segmentNo = 0; segmentNo < segments.length; segmentNo++) {
	    double centerAngle = -Math.PI + segmentNo * 2 * Math.PI
		    / segmentCount + centerDelta;
	    segments[segmentNo] = new CircleSegment(centerAngle, initProb);
	}
    }

    public void calcProbDist(Scan... scans) {

	double mergeWeightSum = 0;

	for (Scan scan : scans) {
	    if (scan.isValid())
		mergeWeightSum += scan.getMergeWeight();
	}

	for (Scan scan : scans) {
	    double mergeWeight = scan.getMergeWeight();
	    List<ScanInput> inputs = scan.getInputs();

	    for (ScanInput scanInput : inputs) {
		addScanInputProb(scanInput, mergeWeight / mergeWeightSum);
	    }
	}
    }

    private void addScanInputProb(ScanInput input, double mergeFactor) {
	double distanceValue = 1 + (distanceFactor - 1) * input.distanceRatio;
	for (CircleSegment cs : segments) {
	    if(!cs.isValid)
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
	    rndSum += cs.probability;
	    if (rndSum >= rnd) {
		return RandomHelper.nextDoubleFromTo(cs.centerAngle
			- centerDelta, cs.centerAngle + centerDelta);
	    }
	}
	return -100;
    }

    public static int angleToSegmentIndex(double angle, int segmentCount) {
	return (int) ((angle + Math.PI) / (2 * Math.PI / segmentCount));
    }

    public void normalize() {
	double segmentProbSum = 0;
	for (CircleSegment cs : segments) {
	    if(cs.isValid)
		segmentProbSum += cs.probability;
	}

	for (CircleSegment cs : segments) {
	    if(cs.isValid)
		cs.probability /= segmentProbSum;
	}
    }

    public void setValidSegments(List<AngleSegment> freeToGoSegments) {
	for (AngleSegment as : freeToGoSegments) {
	    int startIndex = angleToSegmentIndex(as.start, segmentCount);
	    int endIndex = angleToSegmentIndex(as.end, segmentCount);

	    for (int i = startIndex; i <= endIndex; i++) {
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
}
