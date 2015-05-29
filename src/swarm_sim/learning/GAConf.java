package swarm_sim.learning;

import org.jgap.Configuration;
import org.jgap.DefaultFitnessEvaluator;
import org.jgap.InvalidConfigurationException;
import org.jgap.event.EventManager;
import org.jgap.impl.BestChromosomesSelector;
import org.jgap.impl.ChromosomePool;
import org.jgap.impl.CrossoverOperator;
import org.jgap.impl.GABreeder;
import org.jgap.impl.MutationOperator;
import org.jgap.impl.StockRandomGenerator;
import org.jgap.util.ICloneable;

public class GAConf extends Configuration implements ICloneable {
    public GAConf() {
	this("", "");
    }

    public GAConf(String a_id, String a_name) {
	super(a_id, a_name);
	try {
	    setBreeder(new GABreeder());
	    setRandomGenerator(new StockRandomGenerator());
	    setEventManager(new EventManager());
	    BestChromosomesSelector bestChromsSelector = new BestChromosomesSelector(
		    this, 0.90d);
	    bestChromsSelector.setDoubletteChromosomesAllowed(false); // ←
								      // changed
								      // to
								      // false
								      // here
	    addNaturalSelector(bestChromsSelector, false);
	    setMinimumPopSizePercent(0);
	    //
	    setSelectFromPrevGen(1.0d);
	    setKeepPopulationSizeConstant(true);
	    setFitnessEvaluator(new DefaultFitnessEvaluator());
	    setChromosomePool(new ChromosomePool());
	    addGeneticOperator(new CrossoverOperator(this, 0.5d)); // ← changed
								   // from 0.35
								   // to 0.5
								   // here
	    addGeneticOperator(new MutationOperator(this, 4)); // ← changed from
							       // 12 to 4 here
	} catch (InvalidConfigurationException e) {
	    throw new RuntimeException(
		    "Fatal error: DefaultConfiguration class could not use its "
			    + "own stock configuration values. This should never happen. "
			    + "Please report this as a bug to the JGAP team.");
	}
    }

    public Object clone() {
	return super.clone();
    }
}
