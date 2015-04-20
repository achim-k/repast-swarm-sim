package swarm_sim.learning;

import org.jgap.Chromosome;

public class GA {
    
    public static final int RepellIndex = 0;
    public static final int AppealIndex = 1;
    public static final int AppealRepellBorderIndex = 2;
    public static final int MimicIndex = 3;
    public static final int MemoryIndex = 4;
    
    public Chromosome currentChromosome;
    public int currentFitness;
    
    static GA instance;
    
    public static GA getInstance() {
	if(instance == null)
	    instance = new GA();
	return instance;
    }
}
