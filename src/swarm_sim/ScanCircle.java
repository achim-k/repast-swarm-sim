package swarm_sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import repast.simphony.random.RandomHelper;
import sun.rmi.server.UnicastRef;
import swarm_sim.ScanCircle.AttractionType;
import swarm_sim.ScanCircle.DistributionType;
import swarm_sim.ScanCircle.GrowingDirection;

public class ScanCircle {
	public enum AttractionType {
		Appealing, Repelling
	}

	public enum DistributionType {
		Linear, LinearFluid
	}

	public enum GrowingDirection {
		Inner, Outer
	}

	public class InputPair {
		double angle, distance;

		public InputPair(double angle, double distance) {
			this.angle = angle;
			this.distance = distance;
		}
	}

	int bins;
	int lowerValidLImit = 1;
	int upperValidLimit = 0;
	private boolean valid = false;
	double variance = 1;
	double mergeFactor = 1;
	double minValue = 1;
	double maxValue = 1;
	double innerCircleDistance = 0;
	double outerCircleDistance = 0;
	AttractionType attrType = AttractionType.Appealing;
	DistributionType distType = DistributionType.Linear;
	GrowingDirection growDirection = GrowingDirection.Inner;
	List<InputPair> inputs = new ArrayList<>();
	Double data[];
	private boolean needsToBeUpdated = false;

	public ScanCircle(int bins, int inputsUntilValid, double mergeFactor, AttractionType attrType,
			DistributionType distType, GrowingDirection growDirection,
			double minCircleDistance, double maxCircleDistance,
			double minValue, double maxValue) {
		this.bins = bins;
		this.lowerValidLImit = inputsUntilValid;
		this.mergeFactor = mergeFactor;
		this.attrType = attrType;
		this.distType = distType;
		this.growDirection = growDirection;
		this.innerCircleDistance = minCircleDistance;
		this.outerCircleDistance = maxCircleDistance;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.data = new Double[bins];

		if (minCircleDistance >= maxCircleDistance)
			System.err.println("minCircle needs to be smaller than maxCircle");
		if (minValue > maxValue)
			System.err.println("minValue needs to be smaller than maxValue");

		/*
		 * initialize data with same value for each bin (=same probability to go
		 * in this direction)
		 */
		for (int i = 0; i < data.length; i++) {
			data[i] = 0.5;
		}
	}

	public ScanCircle(int bins, int inputsUntilValid, double mergeFactor, AttractionType attrType,
			DistributionType distType, double minCircleDistance,
			double maxCircleDistance, double value) {
		this.bins = bins;
		this.lowerValidLImit = inputsUntilValid;
		this.mergeFactor = mergeFactor;
		this.attrType = attrType;
		this.distType = distType;
		this.innerCircleDistance = minCircleDistance;
		this.outerCircleDistance = maxCircleDistance;
		this.minValue = this.maxValue = value;
		this.data = new Double[bins];

		if (minCircleDistance >= maxCircleDistance)
			System.err.println("minCircle needs to be smaller than maxCircle");

		/*
		 * initialize data with same value for each bin (=same probability to go
		 * in this direction)
		 */
		for (int i = 0; i < data.length; i++) {
			data[i] = 0.5;
		}
	}

	private ScanCircle(int bins) {
		/*
		 * initialize data with same value for each bin (=same probability to go
		 * in this direction)
		 */
		this.bins = bins;
		this.data = new Double[bins];
		for (int i = 0; i < data.length; i++) {
			data[i] = 0.5;
		}
	}

	public ScanCircle(int bins, int lowerValidLimit, int upperValidLimit, double mergeFactor, AttractionType attrType,
			DistributionType distType, GrowingDirection growDirection,
			double minCircleDistance, double maxCircleDistance,
			double minValue, double maxValue) {
		this.bins = bins;
		this.lowerValidLImit = lowerValidLimit;
		this.upperValidLimit = upperValidLimit;
		this.mergeFactor = mergeFactor;
		this.attrType = attrType;
		this.distType = distType;
		this.innerCircleDistance = minCircleDistance;
		this.outerCircleDistance = maxCircleDistance;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.data = new Double[bins];

		if (minCircleDistance >= maxCircleDistance)
			System.err.println("minCircle needs to be smaller than maxCircle");
		if (minValue > maxValue)
			System.err.println("minValue needs to be smaller than maxValue");

		/*
		 * initialize data with same value for each bin (=same probability to go
		 * in this direction)
		 */
		for (int i = 0; i < data.length; i++) {
			data[i] = 0.5;
		}
	}

	public void add(InputPair input) {
		if (input.distance > outerCircleDistance
				|| input.distance < innerCircleDistance)
			return;

		inputs.add(input);
		needsToBeUpdated = true;
		if(!valid && inputs.size() >= lowerValidLImit)
			valid = true;
		if(valid && inputs.size() > upperValidLimit)
			valid = false;

		if (distType == DistributionType.LinearFluid) {
			/* min and max value are calculated online (fluid) */
			if (input.distance < innerCircleDistance)
				innerCircleDistance = input.distance;
			if (input.distance > outerCircleDistance)
				outerCircleDistance = input.distance;
		}
	}
	
	public void add(double angle) {
		this.add(new InputPair(angle, 1));
	}
	
	public void add(double angle, double distance) {
		this.add(new InputPair(angle, distance));
	}
	
	public void calculateDirectionDistribution() {
		if(!needsToBeUpdated )
			return;
		
		needsToBeUpdated = false;
		
		double minMaxCircleDistance = outerCircleDistance - innerCircleDistance;
		
		double value = 0;

		for (InputPair input : inputs) {
			int destBin = movementAngleToBin(input.angle);

			double distanceRatio = (input.distance - innerCircleDistance)
					/ minMaxCircleDistance;
			if (growDirection == GrowingDirection.Outer)
				distanceRatio = 1 - distanceRatio;

			value = maxValue - (maxValue - minValue) * distanceRatio;
//			System.out.println(value);

			/* update data bins */
			int maxNeighborBinDistance = (int) Math.ceil(bins / 8.0);
//			maxNeighborBinDistance = 2;

			for (int bin = destBin - maxNeighborBinDistance; bin <= destBin
					+ maxNeighborBinDistance; bin++) {
				int currBin = bin % bins;
				int binDistance = Math.abs(bin - destBin);

				if (currBin < 0)
					currBin += bins;

				double exp = (1 / (variance * 2.50))
						* Math.exp(-binDistance
								/ (2 * variance * variance * bins / 8));

				double delta = 0; 
				if (attrType == AttractionType.Appealing) {
					 delta = (1 - data[currBin]) * value * exp;
				} else {
					delta = - data[currBin] * value * exp;
				}
				data[currBin] += delta;
			}
		}
	}

	public double getMovementAngle() {

		double rnd = Math.random();
//		double rnd = RandomHelper.nextDoubleFromTo(0, 1);
		double sum = 0;

		for (int i = 0; i < data.length; i++) {
			sum += data[i];
			if (sum > rnd) {
				return binToMovementAngle(i);
			}
		}
		return 0;
	}

	private double binToMovementAngle(int bin) {
		double min = (2.0 * Math.PI * bin / (1.0 * bins)) - Math.PI;

		return min + Math.random()*Math.PI/4.0;
	}

	public int movementAngleToBin(double angle) {
		return (int) ((angle + Math.PI) / (2 * Math.PI / bins));
	}
	
	public static int movementAngleToBin(double angle, int bins) {
		return (int) ((angle + Math.PI) / (2 * Math.PI / bins));
	}

	public void normalize() {
		double sum = 0;
		for (int i = 0; i < data.length; i++) {
			sum += data[i];
		}
		if (sum <= 0)
			return;
		for (int i = 0; i < data.length; i++) {
			data[i] /= sum;
		}
	}

	public String getPrintable(String a) {
		String ret = "";

		for (int i = 0; i < data.length; i++) {
			ret += ", " + String.format("%.2f", data[i]);
		}

		return ret;
	}

	public void clear() {
		inputs.clear();
		for (int i = 0; i < data.length; i++) {
			data[i] = 0.5;
		}
	}
	
	public int getInputCount() {
		return inputs.size();
	}

	/**
	 * Merge scans together, weighted by their individual mergeFactors
	 * 
	 * @param bins
	 * @param scans
	 * @return
	 */
	public static ScanCircle merge(int bins, double surpressRandom, ScanCircle... scans) {
		ScanCircle merged = new ScanCircle(bins);

		for (ScanCircle scanCircle : scans) {
			if(!scanCircle.valid)
				continue;
			scanCircle.calculateDirectionDistribution();
			scanCircle.normalize();
//			System.out.println("B:" + scanCircle.getPrintable(null));
//			scanCircle.removeRandomNess(surpressRandom);
//			System.out.println("A:" + scanCircle.getPrintable(null));
		}

		for (int i = 0; i < bins; i++) {
			double weightedDataSum = 0;
			double mergeFactorSum = 0;

			for (ScanCircle scanCircle : scans) {
				if(!scanCircle.valid)
					continue;
				mergeFactorSum += scanCircle.mergeFactor;
				weightedDataSum += scanCircle.mergeFactor * scanCircle.data[i];
			}

			if(mergeFactorSum > 0)
				merged.data[i] = weightedDataSum / mergeFactorSum;
		}

		merged.removeRandomNess(surpressRandom);
		return merged;
	}
	
	public void removeRandomNess(double maxValue) {
		for (int i = 0; i < data.length; i++) {
			if(data[i] <= maxValue)
				data[i] = 0.0;
			else
				data[i] -= maxValue;
		}
		normalize();
	}
	
	public Double[] getData() {
		return data;
	}
}
