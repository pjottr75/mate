package org.mate.exploration.genetic;

import org.mate.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class GeneticAlgorithmProvider {
    private boolean useDefaults;
    private Properties properties;

    public static <T> GeneticAlgorithm<T> getGeneticAlgorithm(Properties properties) {
        GeneticAlgorithmProvider gaProvider = new GeneticAlgorithmProvider(properties);
        return gaProvider.getGeneticAlgorithm();
    }

    private GeneticAlgorithmProvider(Properties properties) {
        this.properties = properties;
        setUseDefaults();
    }

    private void setUseDefaults() {
        useDefaults = properties.getProperty(GeneticAlgorithmBuilder.USE_DEFAULTS_KEY)
                .equals(GeneticAlgorithmBuilder.TRUE_STRING);
    }

    private <T> GeneticAlgorithm<T> getGeneticAlgorithm() {
        String algorithmName = properties.getProperty(GeneticAlgorithmBuilder.ALGORITHM_KEY);
        if (algorithmName == null) {
            throw new IllegalArgumentException("No algorithm specified");
        }
        switch (algorithmName) {
            case GenericGeneticAlgorithm.ALGORITHM_NAME:
                return initializeGenericGeneticAlgorithm();
            case OnePlusOne.ALGORITHM_NAME:
                return initializeOnePlusOne();
            case NSGAII.ALGORITHM_NAME:
                return initializeNSGAII();
            default:
                throw new UnsupportedOperationException("Unknown algorithm: " + algorithmName);
        }

    }

    private <T> GenericGeneticAlgorithm<T> initializeGenericGeneticAlgorithm() {
        return new GenericGeneticAlgorithm<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunction(),
                this.<T>initializeMutationFunction(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition(),
                getPopulationSize(),
                getGenerationSurvivorCount(),
                getPCrossOver(),
                getPMutate());
    }

    private <T> NSGAII<T> initializeNSGAII() {
        return new NSGAII<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunction(),
                this.<T>initializeMutationFunction(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition(),
                getPopulationSize(),
                getGenerationSurvivorCount(),
                getPCrossOver(),
                getPMutate());
    }

    private <T> OnePlusOne<T> initializeOnePlusOne() {
        return new OnePlusOne<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunction(),
                this.<T>initializeMutationFunction(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition());
    }

    private <T> IChromosomeFactory<T> initializeChromosomeFactory() {
        String chromosomeFactoryId
                = properties.getProperty(GeneticAlgorithmBuilder.CHROMOSOME_FACTORY_KEY);
        if (chromosomeFactoryId == null) {
            return null;
        }
        switch (chromosomeFactoryId) {
            case AndroidRandomChromosomeFactory.CHROMOSOME_FACTORY_ID:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new AndroidRandomChromosomeFactory(getNumEvents());
            case AndroidSuiteRandomChromosomeFactory.CHROMOSOME_FACTORY_ID:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new AndroidSuiteRandomChromosomeFactory(getNumTestCases(), getNumEvents());
            case HeuristicalChromosomeFactory.CHROMOSOME_FACTORY_ID:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new HeuristicalChromosomeFactory(getNumEvents());
            default:
                throw new UnsupportedOperationException("Unknown chromosome factory: "
                        + chromosomeFactoryId);
        }
    }

    private <T> ISelectionFunction<T> initializeSelectionFunction() {
        String selectionFunctionId
                = properties.getProperty(GeneticAlgorithmBuilder.SELECTION_FUNCTION_KEY);
        if (selectionFunctionId == null) {
            return null;
        } else {
            switch (selectionFunctionId) {
                case FitnessSelectionFunction.SELECTION_FUNCTION_ID:
                    return new FitnessSelectionFunction<T>();
                default:
                    throw new UnsupportedOperationException("Unknown selection function: "
                            + selectionFunctionId);
            }
        }
    }

    private <T> ICrossOverFunction<T> initializeCrossOverFunction() {
        String crossOverFunctionId
                = properties.getProperty(GeneticAlgorithmBuilder.CROSSOVER_FUNCTION_KEY);
        if (crossOverFunctionId == null) {
            return null;
        } else {
            switch (crossOverFunctionId) {
                case TestCaseMergeCrossOverFunction.CROSSOVER_FUNCTION_ID:
                    // Force cast. Only works if T is TestCase. This fails if other properties expect a
                    // different T for their chromosomes
                    return (ICrossOverFunction<T>) new TestCaseMergeCrossOverFunction();
                default:
                    throw new UnsupportedOperationException("Unknown crossover function: "
                            + crossOverFunctionId);
            }
        }
    }

    private <T> IMutationFunction<T> initializeMutationFunction() {
        String mutationFunctionId
                = properties.getProperty(GeneticAlgorithmBuilder.MUTATION_FUNCTION_KEY);
        if (mutationFunctionId == null) {
            return null;
        } else {
            switch (mutationFunctionId) {
                case CutPointMutationFunction.MUTATION_FUNCTION_ID:
                    // Force cast. Only works if T is TestCase. This fails if other properties expect a
                    // different T for their chromosomes
                    return (IMutationFunction<T>) new CutPointMutationFunction(getNumEvents());
                case SuiteCutPointMutationFunction.MUTATION_FUNCTION_ID:
                    // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                    // different T for their chromosomes
                    return (IMutationFunction<T>) new SuiteCutPointMutationFunction(getNumEvents());
                default:
                    throw new UnsupportedOperationException("Unknown mutation function: "
                            + mutationFunctionId);
            }
        }
    }

    private <T> List<IFitnessFunction<T>> initializeFitnessFunctions() {
        int amountFitnessFunctions = Integer.valueOf(properties.getProperty
                (GeneticAlgorithmBuilder.AMOUNT_FITNESS_FUNCTIONS_KEY));
        if (amountFitnessFunctions == 0) {
            return null;
        } else {
            List<IFitnessFunction<T>> fitnessFunctions = new ArrayList<>();
            for (int i = 0; i < amountFitnessFunctions; i++) {
                fitnessFunctions.add(this.<T>initializeFitnessFunction(amountFitnessFunctions));
            }
            return fitnessFunctions;
        }
    }

    private <T> IFitnessFunction<T> initializeFitnessFunction(int index) {
        String key = String.format(GeneticAlgorithmBuilder.FORMAT_LOCALE, GeneticAlgorithmBuilder
                .FITNESS_FUNCTION_KEY_FORMAT, index);
        String fitnessFunctionId = properties.getProperty(key);

        switch (fitnessFunctionId) {
            case AndroidStateFitnessFunction.FITNESS_FUNCTION_ID:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>) new AndroidStateFitnessFunction();
            case ActivityFitnessFunction.FITNESS_FUNCTION_ID:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>) new ActivityFitnessFunction();
            case SpecificActivityCoveredFitnessFunction.FITNESS_FUNCTION_ID:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>)
                        new SpecificActivityCoveredFitnessFunction(getFitnessFunctionArgument(index));
            case AmountCrashesFitnessFunction.FITNESS_FUNCTION_ID:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>)
                        new AmountCrashesFitnessFunction();
            case TestLengthFitnessFunction.FITNESS_FUNCTION_ID:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>)
                        new TestLengthFitnessFunction();
            case SuiteActivityFitnessFunction.FITNESS_FUNCTION_ID:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>)
                        new SuiteActivityFitnessFunction();
            case StatementCoverageFitnessFunction.FITNESS_FUNCTION_ID:
                return new StatementCoverageFitnessFunction<>();
            default:
                throw new UnsupportedOperationException("Unknown fitness function: "
                        + fitnessFunctionId);
        }
    }

    private String getFitnessFunctionArgument(int index) {
        String key = String.format(GeneticAlgorithmBuilder.FORMAT_LOCALE, GeneticAlgorithmBuilder
                .FITNESS_FUNCTION_ARG_KEY_FORMAT, index);
        return properties.getProperty(key);
    }

    private ITerminationCondition initializeTerminationCondition() {
        String terminationConditionId
                = properties.getProperty(GeneticAlgorithmBuilder.TERMINATION_CONDITION_KEY);
        if (terminationConditionId == null) {
            return null;
        }
        switch (terminationConditionId) {
            case IterTerminationCondition.TERMINATION_CONDITION_ID:
                return new IterTerminationCondition(getNumberIterations());
            default:
                throw new UnsupportedOperationException("Unknown termination condition: "
                        + terminationConditionId);
        }
    }

    private int getNumTestCases() {
        String numTestCases = properties.getProperty(GeneticAlgorithmBuilder.NUM_TEST_CASES_KEY);
        if (numTestCases == null) {
            if (useDefaults) {
                return org.mate.Properties.MAX_NUM_TCS;
            } else {
                throw new IllegalArgumentException(
                        "Without using defaults: number of test cases not specified");
            }
        } else {
            return Integer.valueOf(numTestCases);
        }
    }

    private int getNumEvents() {
        String numEvents = properties.getProperty(GeneticAlgorithmBuilder.MAX_NUM_EVENTS_KEY);
        if (numEvents == null) {
            if (useDefaults) {
                return org.mate.Properties.MAX_NUM_EVENTS;
            } else {
                throw new IllegalArgumentException(
                        "Without using defaults: maximum number of events not specified");
            }
        } else {
            return Integer.valueOf(numEvents);
        }
    }

    private int getNumberIterations() {
        String numberIterations
                = properties.getProperty(GeneticAlgorithmBuilder.NUMBER_ITERATIONS_KEY);
        if (numberIterations == null) {
            if (useDefaults) {
                return org.mate.Properties.EVO_ITERATIONS_NUMBER;
            } else {
                throw new IllegalArgumentException(
                        "Without using defaults: number of iterations not specified");
            }
        } else {
            return Integer.valueOf(numberIterations);
        }
    }

    private int getPopulationSize() {
        String populationSize
                = properties.getProperty(GeneticAlgorithmBuilder.POPULATION_SIZE_KEY);
        if (populationSize == null) {
            if (useDefaults) {
                //todo: add property
                return 4;
            } else {
                throw new IllegalArgumentException(
                        "Without using defaults: number of iterations not specified");
            }
        } else {
            return Integer.valueOf(populationSize);
        }
    }

    private int getGenerationSurvivorCount() {
        return 2;
    }

    private double getPCrossOver() {
        String pCrossover
                = properties.getProperty(GeneticAlgorithmBuilder.P_CROSSOVER_KEY);
        if (pCrossover == null) {
            if (useDefaults) {
                //todo: add property
                return 0;
            } else {
                throw new IllegalArgumentException(
                        "Without using defaults: number of iterations not specified");
            }
        } else {
            return Double.valueOf(pCrossover);
        }
    }

    private double getPMutate() {
        String pMutate
                = properties.getProperty(GeneticAlgorithmBuilder.P_MUTATE_KEY);
        if (pMutate == null) {
            if (useDefaults) {
                //todo: add property
                return 1;
            } else {
                throw new IllegalArgumentException(
                        "Without using defaults: number of iterations not specified");
            }
        } else {
            return Double.valueOf(pMutate);
        }
    }
}
