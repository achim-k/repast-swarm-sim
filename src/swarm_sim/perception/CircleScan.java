package swarm_sim.perception;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.random.RandomHelper;
import swarm_sim.ScanCircle;
import swarm_sim.ScanCircle.AttractionType;
import swarm_sim.ScanCircle.DistributionType;
import swarm_sim.ScanCircle.GrowingDirection;
import swarm_sim.ScanCircle.InputPair;

public class CircleScan {

	private class AngleDistancePair {
		public double angle, distance;

		public AngleDistancePair(double angle, double distance) {
			super();
			this.angle = angle;
			this.distance = distance;
		}
	}

	private class DataAngleSegement extends AngleSegment {
		public double value;
		public List<AngleSegment> allowedSegments = new ArrayList<>();

		public DataAngleSegement(double start, double end, double value) {
			super(start, end);
			this.value = value;
		}
	}

	int segmentCount;
	double variance;

	int lowerValidLImit = 1;
	int upperValidLimit = 1000;
	boolean isValid = false;

	double mergeWeight = 1;
	double minValue = 1;
	double maxValue = 1;
	double innerCircleDistance = 0;
	double outerCircleDistance = 0;

	List<AngleDistancePair> inputs = new ArrayList<>();
	List<DataAngleSegement> segments = new ArrayList<>();

	boolean needsToBeUpdated = false;

	public CircleScan(int segmentCount, double mergeWeight,
			int lowerValidLImit, int upperValidLimit, double variance,
			double minValue, double maxValue, double innerCircleDistance,
			double outerCircleDistance) {
		super();
		this.segmentCount = segmentCount;
		this.mergeWeight = mergeWeight;
		this.lowerValidLImit = lowerValidLImit;
		this.upperValidLimit = upperValidLimit;
		this.variance = variance;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.innerCircleDistance = innerCircleDistance;
		this.outerCircleDistance = outerCircleDistance;

		for (int segmentNo = 0; segmentNo < segmentCount; segmentNo++) {
			double startAngle = -Math.PI + segmentNo * 2 * Math.PI
					/ segmentCount;
			double endAngle = startAngle + 2 * Math.PI / segmentCount;
			DataAngleSegement s = new DataAngleSegement(startAngle, endAngle,
					0.5);
			segments.add(s);
		}
	}
	
	public CircleScan(int segmentCount) {
		for (int segmentNo = 0; segmentNo < segmentCount; segmentNo++) {
			double startAngle = -Math.PI + segmentNo * 2 * Math.PI
					/ segmentCount;
			double endAngle = startAngle + 2 * Math.PI / segmentCount;
			DataAngleSegement s = new DataAngleSegement(startAngle, endAngle,
					0.5);
			segments.add(s);
		}
	}

	public void add(AngleDistancePair input) {
		if (input.distance > outerCircleDistance
				|| input.distance < innerCircleDistance)
			return;

		inputs.add(input);
		needsToBeUpdated = true;
		if (!isValid && inputs.size() >= lowerValidLImit)
			isValid = true;
		if (isValid && inputs.size() > upperValidLimit)
			isValid = false;
	}

	public void add(double angle) {
		this.add(new AngleDistancePair(angle, 1));
	}

	public void add(double angle, double distance) {
		this.add(new AngleDistancePair(angle, distance));
	}

	public void clear() {
		inputs.clear();
		isValid = false;
		needsToBeUpdated = false;
		for (DataAngleSegement s : segments) {
			s.allowedSegments.clear();
			s.value = 0.5;
		}
	}

	public void setExlusiveSegments(List<AngleSegment> exclusiveSegments) {
		for (DataAngleSegement s : segments) {
			s.allowedSegments = s.filterSegment(exclusiveSegments);
			if(s.allowedSegments.size() == 0)
				s.value = 0;
		}
	}

	public static double rndSegmentAngle(AngleSegment segment) {
		return RandomHelper.nextDoubleFromTo(segment.start, segment.end);
	}

	private AngleSegment angleToSegment(double angle) {
		int index = (int) ((angle + Math.PI) / (2 * Math.PI / segmentCount));
		return segments.get(index);
	}
	
	public static int movementAngleToSegmentIndex(double angle, int bins) {
		return (int) ((angle + Math.PI) / (2 * Math.PI / bins));
	}

	public double getMovementAngle() {
		double rnd = Math.random();
		double sum = 0;

		for (DataAngleSegement s : segments) {
			if (s.allowedSegments.size() > 0)
				sum += s.value;
			if (sum > rnd) {
				return rndSegmentAngle(s.allowedSegments.get(RandomHelper
						.nextIntFromTo(0, s.allowedSegments.size()-1)));
			}
		}
		System.err.println("No other choice :'(");
		return -100;
	}
	
	public void normalize() {
		double sum = 0;
		for (DataAngleSegement s : segments) {
			sum += s.value;
		}
		
		if(sum > 0)
			for (DataAngleSegement s : segments) {
				s.value /= sum;
			}
	}

	public String getPrintable() {
		String ret = "";

		for (DataAngleSegement s : segments) {
			ret += ", " + String.format("%.2f", s.value);
		}
		return ret;
	}
	
	public void calculateDirectionDistribution() {
		if(!needsToBeUpdated )
			return;
		needsToBeUpdated = false;
		
		double minMaxCircleDistance = outerCircleDistance - innerCircleDistance;
		
		double value = 0;

		for (AngleDistancePair input : inputs) {
			int destBin = movementAngleToSegmentIndex(input.angle, segmentCount);

			double distanceRatio = (input.distance - innerCircleDistance)
					/ minMaxCircleDistance;

			value = maxValue - (maxValue - minValue) * distanceRatio;

			/* update data bins */
			int maxNeighborBinDistance = (int) Math.ceil(segmentCount / 8.0);

			for (int bin = destBin - maxNeighborBinDistance; bin <= destBin
					+ maxNeighborBinDistance; bin++) {
				int currBin = bin % segmentCount;
				int binDistance = Math.abs(bin - destBin);

				if (currBin < 0)
					currBin += segmentCount;
				
				DataAngleSegement segment = segments.get(currBin);

				double exp = (1 / (variance * 2.50))
						* Math.exp(-binDistance
								/ (2 * variance * variance * segmentCount / 8));

				double delta = 0; 
				if (maxValue > 0) {
					 delta = (1 - segment.value) * value * exp;
				} else {
					delta = - segment.value * value * exp;
				}
				segment.value += delta;			
			}
		}
	}
	
	/**
	 * Merge scans together, weighted by their individual mergeFactors
	 * 
	 * @param bins
	 * @param scans
	 * @return
	 */
	public static CircleScan merge(int segmentCount, double surpressRandom, List<AngleSegment> exclusiveSegments, CircleScan... scans) {
		CircleScan merged = new CircleScan(segmentCount);

		for (CircleScan cs : scans) {
			if(!cs.isValid)
				continue;
			cs.calculateDirectionDistribution();
			cs.normalize();
		}
		
		
		for (int i = 0; i < segmentCount; i++) {
			double weightedDataSum = 0;
			double mergeFactorSum = 0;

			for (CircleScan cs : scans) {
				if(!cs.isValid)
					continue;
				mergeFactorSum += cs.mergeWeight;
				weightedDataSum += cs.mergeWeight * cs.segments.get(i).value;
			}

			if(mergeFactorSum > 0)
				merged.segments.get(i).value = weightedDataSum / mergeFactorSum;
		}

		
		merged.setExlusiveSegments(exclusiveSegments);
		if(exclusiveSegments.size() > 0)
			merged.normalize();
		
		merged.removeRandomNess(surpressRandom);
		return merged;
	}
	
	public void removeRandomNess(double maxValue) {
		for (DataAngleSegement s : segments) {
			s.value -= maxValue;
			if(s.value < 0)
				s.value = 0;
		}
		normalize();
	}
}
