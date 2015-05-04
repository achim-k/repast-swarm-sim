package swarm_sim.learning;

import org.jgap.IChromosome;

public class GA {

    public int RepellIndex = 0;
    public int AppealIndex = 1;
    public int AppealRepellBorderIndex = 2;
    public int MimicIndex = 3;
    public int MemoryIndex = 4;
    public int PrevDirectionIndex = 5;

    public IChromosome currentChromosome;
    public double currentFitness;

    static GA instance;

    public static GA getInstance() {
	if (instance == null)
	    instance = new GA();
	return instance;
    }
}
