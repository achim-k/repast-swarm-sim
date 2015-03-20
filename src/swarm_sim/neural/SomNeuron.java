package swarm_sim.neural;

import java.util.ArrayList;
import java.util.List;

public class SomNeuron {

	private List<Double> weightVector = new ArrayList<Double>();
	private double x, y; /* location on the map */

	public SomNeuron(List<Double> weightVector, int x, int y) {
		this.weightVector = weightVector;
		this.x = x;
		this.y = y;
	}

	public double[] getMapLocation() {
		return new double[] { x, y };
	}

	public double calcMapDistance(SomNeuron v) {
		double[] l1 = this.getMapLocation();
		double[] l2 = v.getMapLocation();

		double distance = Math.pow(l1[0] - l2[0], 2);
		distance += Math.pow(l1[1] - l2[1], 2);

		return Math.sqrt(distance);
	}

	public double calcDistance(List<Double> input) {
		if (!checkDimensions(input))
			return 0;

		double sum = 0;
		for (int i = 0; i < input.size(); i++) {
			sum += Math.pow(input.get(i) - weightVector.get(i), 2);
		}
		return Math.sqrt(sum);
	}

	public void updateWeightVector(List<Double> input, double multiplier) {
		if (!checkDimensions(input))
			return;

		// Wv(s + 1) = Wv(s) + Θ(u, v, s) α(s)(D(t) - Wv(s))
		for (int i = 0; i < weightVector.size(); i++) {
			double newWeight = weightVector.get(i) + multiplier
					* (input.get(i) - weightVector.get(i));
			weightVector.set(i, newWeight);
		}
	}

	public int getDimension() {
		return weightVector.size();
	}

	private boolean checkDimensions(List<Double> input) {
		if (input.size() != weightVector.size()) {
			System.err.println("Size of input and weights don't match");
			return false;
		}
		return true;
	}

	public String getPrintable() {
		String ret = "";
		for (double d : weightVector) {
			ret += d + ",";
		}

		return ret;
	}
}
