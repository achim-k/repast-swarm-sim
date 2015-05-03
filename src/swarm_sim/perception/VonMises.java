package swarm_sim.perception;

import cern.jet.math.Bessel;

public class VonMises {
    private double k, divider;

    public VonMises(double k) {
	super();
	this.k = k;
	this.divider = 2 * Math.PI * Bessel.i0(k);
    }
    
    public double getValue(double mean, double x) {
	return Math.exp(k * Math.cos(x - mean)) / divider;
    }
}
