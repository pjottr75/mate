package org.mate.exploration.genetic.core;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.ant.AntStatsLogger;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.model.TestCase;
import org.mate.ui.EnvironmentManager;
import org.mate.utils.Coverage;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class that serves as a basis for genetic algorithms
 * @param <T> Type wrapped by the chromosome implementation
 */
public abstract class GeneticAlgorithm<T> implements IGeneticAlgorithm<T> {
    protected IChromosomeFactory<T> chromosomeFactory;
    protected ISelectionFunction<T> selectionFunction;
    protected ICrossOverFunction<T> crossOverFunction;
    protected IMutationFunction<T> mutationFunction;
    protected List<IFitnessFunction<T>> fitnessFunctions;
    protected ITerminationCondition terminationCondition;

    protected int populationSize;
    protected int bigPopulationSize;
    protected List<IChromosome<T>> population;
    protected int currentGenerationNumber;
    protected double pCrossover;
    protected double pMutate;

    private final AntStatsLogger antStatsLogger;


    /**
     * Initializing the genetic algorithm with all necessary attributes
     * @param chromosomeFactory see {@link IChromosomeFactory}
     * @param selectionFunction see {@link ISelectionFunction}
     * @param crossOverFunction see {@link ICrossOverFunction}
     * @param mutationFunction see {@link IMutationFunction}
     * @param fitnessFunctions see {@link IFitnessFunction}
     * @param terminationCondition see {@link ITerminationCondition}
     * @param populationSize size of population kept by the genetic algorithm
     * @param bigPopulationSize size which population will temporarily be after creating offspring
     * @param pCrossover probability that crossover occurs (between 0 and 1)
     * @param pMutate probability that mutation occurs (between 0 and 1)
     */
    public GeneticAlgorithm(IChromosomeFactory<T> chromosomeFactory, ISelectionFunction<T>
            selectionFunction, ICrossOverFunction<T> crossOverFunction, IMutationFunction<T> mutationFunction, List<IFitnessFunction<T>> fitnessFunctions, ITerminationCondition terminationCondition, int populationSize, int bigPopulationSize, double pCrossover, double pMutate) {
        this.chromosomeFactory = chromosomeFactory;
        this.selectionFunction = selectionFunction;
        this.crossOverFunction = crossOverFunction;
        this.mutationFunction = mutationFunction;
        this.fitnessFunctions = fitnessFunctions;
        this.terminationCondition = terminationCondition;

        this.populationSize = populationSize;
        this.bigPopulationSize = bigPopulationSize;
        population = new ArrayList<>();
        this.pCrossover = pCrossover;
        this.pMutate = pMutate;

        antStatsLogger = new AntStatsLogger();

        currentGenerationNumber = 0;
    }

    @Override
    public void run() {
        long algorithmStartTime = System.currentTimeMillis();
        long generationStartTime = System.currentTimeMillis();
        antStatsLogger.write("Algorithm Type; Generation Number; Population Number; " +
                "Fitness Value; Current Coverage; Combined Coverage; Runtime in s\n");

        String targetLine = Properties.TARGET_LINE();
        IFitnessFunction<TestCase> lineCoveredPercentageFitnessFunction
                = new LineCoveredPercentageFitnessFunction(targetLine);

        createInitialPopulation();

        antStatsLogger.write("genetic; " + currentGenerationNumber + "; -; -; -; -; ");
        logCurrentRuntime(generationStartTime);

        while (!terminationCondition.isMet()) {
            evolve();

            if(terminationCondition.isMet()) {
                antStatsLogger.write("genetic; -; -; -; -; -; ");
                logCurrentRuntime(algorithmStartTime);
                antStatsLogger.write("genetic; -; -; -; -; -; successful");
            }
        }
    }

    @Override
    public List<IChromosome<T>> getCurrentPopulation() {
        return population;
    }

    @Override
    public void createInitialPopulation() {
        MATE.log_acc("Creating initial population (1st generation)");
        for (int i = 0; i < populationSize; i++) {
            long populationStartTime = System.currentTimeMillis();

            population.add(chromosomeFactory.createChromosome());

            antStatsLogger.write("genetic; " + currentGenerationNumber + "; " + (i + 1) +
                    "; ?; ?; ?; ");
            logCurrentRuntime(populationStartTime);
        }

        logCurrentFitness();
        currentGenerationNumber++;
    }

    @Override
    public void evolve() {
        long generationStartTime = System.currentTimeMillis();

        MATE.log_acc("Creating population #" + (currentGenerationNumber + 1));
        List<IChromosome<T>> newGeneration = new ArrayList<>(population);

        while (newGeneration.size() < bigPopulationSize) {
            List<IChromosome<T>> parents = selectionFunction.select(population, fitnessFunctions);

            IChromosome<T> parent;

            if (Randomness.getRnd().nextDouble() < pCrossover) {
                parent = crossOverFunction.cross(parents);
            } else {
                parent = parents.get(0);
            }

            List<IChromosome<T>> offspring = new ArrayList<>();
            offspring.add(parent);
            if (Randomness.getRnd().nextDouble() < pMutate) {
                offspring = mutationFunction.mutate(parent);
            }


            for (IChromosome<T> chromosome : offspring) {
                if (newGeneration.size() == bigPopulationSize) {
                    break;
                }

                newGeneration.add(chromosome);

            }
        }

        //todo: beautify later when more time
        population.clear();
        population.addAll(newGeneration);
        List<IChromosome<T>> tmp = getGenerationSurvivors();
        population.clear();
        population.addAll(tmp);
        // TODO log new generation infos
        for (int i = 0; i < population.size(); i++) {
            IChromosome<T> chromosome = population.get(i);

            //LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);

            //double fitnessValue = lineCoveredPercentageFitnessFunction.getFitness(chromosome);

            antStatsLogger.write("genetic; " + currentGenerationNumber + "; " + (i + 1) +
                    "; ?; ?; ?; -;");
        }
        antStatsLogger.write("genetic; " + currentGenerationNumber + "; -; -; -; -; ");
        logCurrentRuntime(generationStartTime);

        logCurrentFitness();
        currentGenerationNumber++;
    }

    @Override
    public List<IChromosome<T>> getGenerationSurvivors() {
        return new ArrayList<>(population.subList(population.size() - populationSize, population.size()));
    }

    protected void logCurrentFitness() {
        if (population.size() <= 10 ) {
            MATE.log_acc("Fitness of generation #" + (currentGenerationNumber + 1) + " :");
            for (int i = 0; i < Math.min(fitnessFunctions.size(), 5); i++) {
                MATE.log_acc("Fitness function " + (i + 1) + ":");
                IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(i);
                for (int j = 0; j < population.size(); j++) {
                    IChromosome<T> chromosome = population.get(j);
                    MATE.log_acc("Chromosome " + (j + 1) + ": "
                            + fitnessFunction.getFitness(chromosome));
                }
            }
            if (fitnessFunctions.size() > 5) {
                MATE.log_acc("Omitted other fitness function because there are to many (" + fitnessFunctions.size() + ")");
            }
        }

        if (Properties.COVERAGE() == Coverage.BRANCH_COVERAGE) {
            MATE.log_acc("Total Coverage: " + Registry.getEnvironmentManager().getBranchCoverage());
        } else if (Properties.COVERAGE() == Coverage.LINE_COVERAGE) {
            MATE.log_acc("Combined coverage until now: " + Registry.getEnvironmentManager().getCombinedCoverage());
            if (population.size() <= 10) {
                MATE.log_acc("Combined coverage of current population: " + Registry.getEnvironmentManager().getCombinedCoverage(population));
            }
        }
    }

    private void logCurrentRuntime (long startTime) {
        long currentTime = System.currentTimeMillis();
        currentTime = currentTime - startTime;
        long seconds = (currentTime/(1000));
        antStatsLogger.write(seconds + "\n");
    }

}
