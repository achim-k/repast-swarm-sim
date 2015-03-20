package swarm_sim.neural;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ActorCritic {

	private class InputOutputTrainingSample {
		public List<Double> somDistances;
		public double criticValue;
		public int tick;
		
		public InputOutputTrainingSample() {
			this.somDistances = new ArrayList<>();
		}
	}

	private List<ActorCriticSomNeuron> somLayer = new ArrayList<ActorCriticSomNeuron>();
	private List<InputOutputTrainingSample> trainingSamples = new ArrayList<InputOutputTrainingSample>();

	private double prevCriticValue = -1, maxValue, minValue;
	private int theta = 10;
	private double explorationFactor;
	private double actorLearningRate = 0.4;
	private double criticLearningRate = 0.4;

	public ActorCritic(double explorationFactor, double maxValue,
			double minValue) {
		this.explorationFactor = explorationFactor;
		this.maxValue = maxValue;
		this.minValue = minValue;
	}

	public double getOutput(List<Double> input, int tick) {
		InputOutputTrainingSample sample = new InputOutputTrainingSample();

		for (ActorCriticSomNeuron n : somLayer) {
			double distance = n.getDistance(input);
			sample.somDistances.add(distance);
		}

		sample.criticValue = getCriticOutput();
		sample.tick = tick;
		trainingSamples.add(sample);

		double exploration = getExploration(sample.criticValue, tick);

		return getActorOutput(exploration);
	}

	public void trainSingle(double reward, int tick) {
		InputOutputTrainingSample sample = trainingSamples.get(0);
		if (prevCriticValue == -1) {
			prevCriticValue = sample.criticValue;
			trainingSamples.clear();
			return;
		}

		double tdError = getTdError(reward, theta, sample.criticValue,
				prevCriticValue);
		double exploration = getExploration(sample.criticValue, sample.tick);
		for (int i = 0; i < sample.somDistances.size(); i++) {
			ActorCriticSomNeuron n = somLayer.get(i);
			double distance = sample.somDistances.get(i);
			updateActorWeight(n, distance, exploration, tdError,
					actorLearningRate);
			
			System.out.println(n.getActorWeight() + " a vs. "
					+ somLayer.get(i).getActorWeight());
			updateCriticWeight(n, distance, tdError, criticLearningRate);
			System.out.println(n.getCriticWeight() + " c vs. "
					+ somLayer.get(i).getCriticWeight());
		}

		prevCriticValue = sample.criticValue;
		trainingSamples.clear();
	}

	public void train(double reward, int tick) {
		if (prevCriticValue == -1)
			prevCriticValue = trainingSamples.get(0).criticValue;

		for (int i = 0; i < trainingSamples.size(); i++) {
			int somNeuronIndex = i % somLayer.size();
			ActorCriticSomNeuron n = somLayer.get(somNeuronIndex);
			InputOutputTrainingSample sample = trainingSamples.get(i);

			double tdError = getTdError(reward, theta, sample.criticValue,
					prevCriticValue);
			double exploration = getExploration(sample.criticValue, sample.tick);
			// updateActorWeights(n, , exploration, tdError, actorLearningRate);

			prevCriticValue = sample.criticValue;
		}
	}

	private double getActorOutput(double exploration) {
		double output = 0;
		for (ActorCriticSomNeuron n : somLayer) {
			output += n.getActorOutput();
		}
		output += exploration;
		return output;
	}

	private double getCriticOutput() {
		double output = 0;
		for (ActorCriticSomNeuron n : somLayer) {
			output += n.getCriticOutput();	
		}
		return output;
	}

	private void updateActorWeight(ActorCriticSomNeuron n, double distance,
			double exploration, double tdError, double learningRate) {
		double weight = n.getActorWeight();
		n.setActorWeight(weight + learningRate * tdError * distance
				* exploration);
	}

	private void updateCriticWeight(ActorCriticSomNeuron n, double distance,
			double tdError, double learningRate) {
		double weight = n.getCriticWeight();
		n.setCriticWeight(weight + learningRate * tdError * distance);
	}

	/**
	 * TODO: What when not learned every timestep?
	 * 
	 * @param reward
	 * @param theta
	 * @return
	 */
	private double getTdError(double reward, int theta, double currValue,
			double prevValue) {
		return reward + (1 - 1.0 / theta) * currValue - prevValue;
	}

	private double getExploration(double criticValue, int tick) {
		double gauss = (1 / 1 * Math.sqrt(Math.PI * 2))
				* Math.exp(-tick * tick / 2);

		double v = (maxValue - criticValue) / (maxValue - minValue);
		if (v < 0)
			v = 0;
		if (v > 1)
			v = 1;
		return explorationFactor * gauss * v;
	}

	public void loadSomNeuronsFromFile(String filename, String separator) {
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));

			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				List<Double> weightVector = new ArrayList<>();
				for(String s : strLine.split(separator)) {
					weightVector.add(Double.parseDouble(s));
				}
				ActorCriticSomNeuron n = new ActorCriticSomNeuron(weightVector);
				this.somLayer.add(n);
			}

			// Close the input stream
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
