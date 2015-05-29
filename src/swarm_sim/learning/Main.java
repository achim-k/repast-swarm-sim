package swarm_sim.learning;

import java.io.File;
import java.io.PrintWriter;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DoubleGene;

import repast.simphony.runtime.RepastBatchMain;
import repast.simphony.util.FileUtils;

public class Main extends FitnessFunction {

    private static final long serialVersionUID = -6250273377433540039L;

    private final int numberOfEvolutions = 25;
    private final int runsPerChromosome = 10;
    private final int populationSize = 30;

    String[] args;
    int evaluateCount = 0;
    PrintWriter writer;
    int evolutionCount;

    GAConf conf;

    String CC = "CC/batch_params_a16_default.xml";
    String CMC = "CMC/batch_params_a16_default.xml";

    private void init() throws InvalidConfigurationException {
	evaluateCount = 0;
	evolutionCount = 0;

	conf = new GAConf();
	conf.setFitnessFunction(this);
	conf.setPopulationSize(populationSize);

    }

    private void run(String[] args) {
	this.args = args;

	try {
	    Configuration.reset();
	    init();

	    writer = new PrintWriter(args[1] + "evolution_output.txt", "UTF-8");
	    PrintWriter winning_writer = new PrintWriter(args[1]
		    + "winning_output.txt", "UTF-8");

	    Gene[] sampleGenes = getAlgGenes(conf, args[1]);

	    conf.setSampleChromosome(new Chromosome(conf, sampleGenes));

	    Genotype population = Genotype.randomInitialGenotype(conf);

	    for (evolutionCount = 0; evolutionCount < numberOfEvolutions; evolutionCount++) {
		population.evolve();

		evaluateCount = 0;

		/* Print fitness + Genes of winning chromosome */
		winning_writer.print(evolutionCount + "\t"
			+ population.getFittestChromosome().getFitnessValue());
		System.err.print(evolutionCount + "\t"
			+ population.getFittestChromosome().getFitnessValue());

		Gene[] genes = population.getFittestChromosome().getGenes();
		for (Gene gene : genes) {
		    winning_writer.print("\t" + gene.getAllele());
		    System.err.print("\t" + gene.getAllele());
		}
		winning_writer.print("\n");
		System.err.print("\n");

		FileUtils.delete(new File("output"));

	    }
	    winning_writer.close();
	    writer.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public Gene[] getAlgGenes(Configuration conf, String alg)
	    throws InvalidConfigurationException {
	GA ga = GA.getInstance();

	if (alg.equalsIgnoreCase(CC)) {
	    Gene[] sampleGenes = new Gene[5];

	    ga.RepellIndex = 0;
	    ga.AppealIndex = 1;
	    ga.AppealRepellBorderIndex = 2;
	    ga.MimicIndex = 3;
	    ga.PrevDirectionIndex = 4;

	    sampleGenes[ga.RepellIndex] = new DoubleGene(conf, 0, 1);
	    sampleGenes[ga.AppealIndex] = new DoubleGene(conf, 0, 1);
	    sampleGenes[ga.AppealRepellBorderIndex] = new DoubleGene(conf, 0, 1);
	    sampleGenes[ga.MimicIndex] = new DoubleGene(conf, 0, 1);
	    sampleGenes[ga.PrevDirectionIndex] = new DoubleGene(conf, 0, 1);

	    return sampleGenes;
	} else if (alg.equalsIgnoreCase(CMC)) {
	    Gene[] sampleGenes = new Gene[6];

	    ga.RepellIndex = 0;
	    ga.AppealIndex = 1;
	    ga.AppealRepellBorderIndex = 2;
	    ga.MimicIndex = 3;
	    ga.PrevDirectionIndex = 4;
	    ga.MemoryIndex = 5;

	    sampleGenes[ga.RepellIndex] = new DoubleGene(conf, 0, 1);
	    sampleGenes[ga.AppealIndex] = new DoubleGene(conf, 0, 1);
	    sampleGenes[ga.AppealRepellBorderIndex] = new DoubleGene(conf, 0, 1);
	    sampleGenes[ga.MimicIndex] = new DoubleGene(conf, 0, 1);
	    sampleGenes[ga.PrevDirectionIndex] = new DoubleGene(conf, 0, 1);
	    sampleGenes[ga.MemoryIndex] = new DoubleGene(conf, 0, 1);

	    return sampleGenes;
	}

	return null;
    }

    @Override
    protected double evaluate(IChromosome c) {
	double fitness = 0;
	GA ga = GA.getInstance();
	ga.currentFitness = 0;
	ga.currentChromosome = c;
	evaluateCount++;

	for (int i = 0; i < runsPerChromosome; i++) {
	    try {
		if (!ga.currentChromosome.equals(c)) {
		    System.err.println("Wrong chromosome!!!");
		    return 0;
		}
		System.out.println("Evolution :" + evolutionCount + "\tRun #"
			+ evaluateCount + "-" + i);
		RepastBatchMain.main(args);
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    fitness += ga.currentFitness;
	}

	fitness /= runsPerChromosome; // calc average

	writer.println(evaluateCount + "\t" + fitness + "\t"
		+ ga.currentChromosome);

	System.out.println("Chromosome: \t" + ga.currentChromosome);
	System.out.println("Fitness:    \t" + fitness);

	return fitness;
    }

    public static void main(String[] args) {

	try {
	    Main m = new Main();
	    // m.init();

	    args[1] = m.CC;
	    m.run(args);

	    // args[1] = m.CMC;
	    // m.run(args);

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }
}
