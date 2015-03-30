package swarm_sim;

import repast.simphony.random.RandomHelper;

public class ScanData {
	private int binCount;
	private double maxDistance;
	private double[] data;
	private double variance = 0.5;

	public ScanData(int binCount, double maxDistance, double variance) {
		this.binCount = binCount;
		this.maxDistance = maxDistance;
		this.variance = variance;
		data = new double[binCount];
		clear();
		
		if(binCount %2 != 0)
			System.err.println("number of bins must be dividable by 2");
	}

	public void addData(double angle, double distance) {
		int bin = movementAngleToBin(angle);

		if (distance <= 0)
			return;

		double input = maxDistance / (4 * distance);
		if (input > 1) /* limit max */
			input = 1;

		/* update bins */
		int maxNeighborDistance = (int) Math.ceil(binCount / 8.0);

		for (int b = bin - maxNeighborDistance; b <= bin + maxNeighborDistance; b++) {
			int currBin = b % binCount;
			int binDistance = Math.abs(currBin - bin);
			
			if (currBin < 0)
				currBin += binCount;

			double exp = (1 / (variance * 2.50))
					* Math.exp(-binDistance
							/ (2 * variance * variance * binCount / 8));
			double delta = (data[currBin]) * input * 2*exp;
			if(delta >= data[currBin])
				delta = data[currBin];
			data[currBin] -= delta;
			
//			int binOpposite = (currBin + binCount/2) % binCount; 
//			data[binOpposite] += delta;
			/* update positively on the other side */
		}
	}
	
	public double getMovementAngle() {
		
		double rnd = RandomHelper.nextDouble();
		double sum = 0;
		
		/* normalize before!! */
		this.normalize();
		
		for (int i = 0; i < data.length; i++) {
			sum += data[i];
			if(sum >= rnd) {
				return binToMovementAngle(i);
			}
		}
		
		return 0;
	}
	
	private double binToMovementAngle(int bin) {
		double min = (2.0*Math.PI*bin/binCount) - Math.PI;
		double max = (2.0*Math.PI*(bin+1)/binCount) - Math.PI;
		
		return RandomHelper.nextDoubleFromTo(min, max);
	}
	
	private int movementAngleToBin(double angle) {
		return (int) ((angle + Math.PI) / (2 * Math.PI / binCount));
	}

	public int getBinCount() {
		return binCount;
	}

	public void clear() {
		for (int i = 0; i < data.length; i++) {
			data[i] = 1.0 / binCount;
		}
	}

	public double[] getData() {
		return data;
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
}
