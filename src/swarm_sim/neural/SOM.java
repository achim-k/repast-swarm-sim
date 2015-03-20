package swarm_sim.neural;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class SOM {

	public List<SomNeuron> neurons = new ArrayList<SomNeuron>();
	private int gridX, gridY;
	private int currentX = 0, currentY = 0;
	private int dimension;

	private int inputCount = 0;
	private double initialLearningRate = 0.6;
	private double learningRateDecay = -0.1;
	private double initialNeighborhoodRadius = 2.5;
	private double neighborhoodRadiusDecay = -0.1;

	public SOM(int gridX, int gridY, int dimension) {
		this.gridX = gridX;
		this.gridY = gridY;
		this.dimension = dimension;
	}

	public void createNeuron(List<Double> input) {
		neurons.add(new SomNeuron(input, currentX, currentY));
		currentX++;
		currentY = currentX % gridX;
		if (currentY > gridY) {
			System.err.println("Out of map bounds");
		}
	}

	public SomNeuron getBMU(List<Double> input) {
		SomNeuron bmu = null;
		double minDistance = 0;

		for (SomNeuron v : neurons) {
			double distance = v.calcDistance(input);
			if (minDistance == 0 | distance < minDistance) {
				minDistance = distance;
				bmu = v;
			}
		}
		return bmu;
	}

	private double getLearningRate() {
		// eta(t) = eta0*t^k_eta
		return initialLearningRate * Math.pow(inputCount, learningRateDecay);
	}

	private double getNeighborhoodRadius() {
		return initialNeighborhoodRadius
				* Math.pow(inputCount, neighborhoodRadiusDecay);
	}

	private double getNeighborhoodMultiplier(SomNeuron v, SomNeuron bmu) {
		return Math.exp(-bmu.calcMapDistance(v) / 2 * getNeighborhoodRadius());		
	}

	public void update(List<Double> input) {
		if (input.size() != dimension) {
			System.err.println("Wrong input dimension: " + input.size());
			return;
		}

		inputCount++;

		SomNeuron bmu = getBMU(input);

		for (SomNeuron v : neurons) {
			double multiplier = getNeighborhoodMultiplier(v, bmu)
					* getLearningRate();
			v.updateWeightVector(input, multiplier);
		}
	}

	public void printWeights() {
		for (SomNeuron n : neurons) {
			System.out.println(n.getPrintable());
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int gridX = 8, gridY = 1;
		SOM som = new SOM(gridX, gridY, 8);

		try {
			FileInputStream fstream = new FileInputStream("data/data.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));

			for (int i = 0; i < gridX * gridY; i++) {
				List<Double> input = new ArrayList<Double>();
				for(int j = 0; j < 8; j++)
					input.add(Math.random());
				som.createNeuron(input);
			}
			som.printWeights();

			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				List<Double> input = new ArrayList<Double>();
				for (String elem : strLine.split(",")) {
					input.add(Double.parseDouble(elem));
				}
				som.update(input);
			}
			// Close the input stream
			br.close();
			
			som.printWeights();

			double asdf[] = new double[] { 0.0, 0, 0, 0, 0.0, 0, 0, 1.0 };
			List<Double> input = new ArrayList<Double>();
			for(double d: asdf) {
				input.add(d);
			}
			
			System.out.println(som.getBMU(input).getPrintable());
			
			int i = 0;
			for(SomNeuron n : som.neurons) {
				i++;
				System.out.println(i + ": "+ n.calcDistance(input));
			}
			
			FileWriter fw = new FileWriter("data/som.txt");
			for(SomNeuron n : som.neurons) {
				fw.write(n.getPrintable()+"\n");
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
