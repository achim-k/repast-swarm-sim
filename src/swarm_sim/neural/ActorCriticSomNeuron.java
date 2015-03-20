package swarm_sim.neural;

import java.util.List;

public class ActorCriticSomNeuron extends SomNeuron {

	private double actorWeight;
	private double criticWeight;
	private double distance;

	public ActorCriticSomNeuron(List<Double> weightVector) {
		super(weightVector, 0, 0);
		actorWeight = Math.random();
		criticWeight = Math.random();
	}

	public double getActorWeight() {
		return actorWeight;
	}

	public void setActorWeight(double actorWeight) {
		this.actorWeight = actorWeight;
	}

	public double getCriticWeight() {
		return criticWeight;
	}

	public void setCriticWeight(double criticWeight) {
		this.criticWeight = criticWeight;
	}
	
	public double getDistance(List<Double> input) {
		distance = calcDistance(input);
		return distance;
	}

	/**
	 * Get distance has to be called before
	 * @return
	 */
	public double getActorOutput() {
		return actorWeight * distance;
	}

	/**
	 * Get distance has to be called before
	 * @return
	 */
	public double getCriticOutput() {
		return criticWeight * distance;
	}
}
