/*  
 *  Inteligencia computacional
 *  Master Profesional en Ingeniería Informática
 * 
 *  2018 © Copyleft - All Wrongs Reserved
 *
 *  Ernesto Serrano <erseco@correo.ugr.es>
 * 
 */
package qap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 * 
 * @author Ernesto Serrano
 */
public class Genetic {
    
    public enum AlgorithmType {
        STANDARD, 
        BALDWINIAN, 
        LAMARCKIAN,
    }
    
    protected static ArrayList<ArrayList<Integer>> distance_matrix;
    
    protected static ArrayList<ArrayList<Integer>>  flow_matrix;
    
    protected static int PROBLEM_SIZE;
    
    protected static final int POPULATION_SIZE = 100;
    
    private static final int NUMBER_OF_GENERATIONS = 400;

    private static final double INDIVIDUAL_MUTATION_PROBABILITY = 0.3;

    private static final double GENE_MUTATION_PROBABILITY = 0.002;

    private static final int TOURNAMENT_SIZE = 4;

    private final AlgorithmType type;
        
    private ArrayList<Individual> population;
    
    public Genetic(AlgorithmType type, String path) {
        this.type = type;
        this.load(path);
        
        population = createPopulation(POPULATION_SIZE);
        calculateFitness(population, type);
    }
    
    private  ArrayList<Individual> createPopulation(int size)  {
        ArrayList<Individual> newPopulation = new ArrayList<>();
        for (int i = 0; i < size; i++)
            newPopulation.add(new Individual());
        return newPopulation;
    }
    
    private void calculateFitness(ArrayList<Individual> population, AlgorithmType type) {
        population.forEach((individual) -> {
            individual.calculateFitness(type);
        }); 
    }

    private void load(String path) {
        
        try (Scanner scanner = new Scanner(new File(path))) {
            PROBLEM_SIZE = scanner.nextInt();
            flow_matrix = new ArrayList<>();
            distance_matrix = new ArrayList<>();

            for (int i = 0; i < PROBLEM_SIZE; i++) {
                flow_matrix.add(new ArrayList<>());
                for (int j = 0; j < PROBLEM_SIZE; j++) {
                    flow_matrix.get(i).add(scanner.nextInt());
                }
            }

            for (int i = 0; i < PROBLEM_SIZE; i++) {
                distance_matrix.add(new ArrayList<>());
                for (int j = 0; j < PROBLEM_SIZE; j++) {
                    distance_matrix.get(i).add(scanner.nextInt());
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Error opening file: " + ex.getMessage());
        }

    }

    public void execute(int optimal) {
        for (int i = 0; i < NUMBER_OF_GENERATIONS; i++) {
            ArrayList<Individual> generation = createPopulation(POPULATION_SIZE);

            for (int j = 0; j < POPULATION_SIZE / 2; j++) {
                // Parents to crossover
                Individual parent1 = select();
                Individual parent2 = select();
                // cannot repeat parent
                while (parent1.getFitness() == parent2.getFitness()) {
                    parent2 = select();
                } 

                // Crossover, mutate and calculate fitness of generated children
                Individual[] children = crossover(parent1, parent2);

                mutate(children[0]);
                mutate(children[1]);

                // Insert children in new population
                generation.set(2 * j, new Individual(children[0]));
                generation.set(2 * j + 1, new Individual(children[1]));
            }
            generation.add(new Individual(Collections.min(population))); // preserve best individual
            
            calculateFitness(generation, type);

            population = generation; // Evolve into the new generation
            
            // Remove the worst individuals to maintain the population size
            Collections.sort(population);
            while(population.size() > POPULATION_SIZE)
                population.remove(population.size()-1);
            
            //System.out.println("\t " + i + ": fitness: " + Collections.min(population).getFitness());
            
        }
        System.out.println("-------------");
        System.out.println("Type: " + type.toString());
        System.out.println("Solution: " + Collections.min(population).toString());
        System.out.println("Fitness: " + Collections.min(population).getFitness());
        System.out.println("Problem size: " + PROBLEM_SIZE);
        System.out.println("Population size: " + POPULATION_SIZE);
        System.out.println("Number of generations: " + NUMBER_OF_GENERATIONS);
        System.out.println("Individual mutation probability: " + INDIVIDUAL_MUTATION_PROBABILITY);
        System.out.println("Gene mutation probability: " + GENE_MUTATION_PROBABILITY);
        System.out.println("Tournament size: " + TOURNAMENT_SIZE);
        System.out.println("Difference from optimal solution: " + ((((double) Collections.min(population).getFitness() - optimal) / optimal) * 100) + "%");
    
    }
  
    private Individual select() {

        ArrayList<Individual> tournament = new ArrayList<>();
        Collections.shuffle(population);

        for (int i = 0; i < TOURNAMENT_SIZE; i++)
            tournament.add(population.get(i));        

        // return that individual
        return Collections.min(tournament) ;
    }

    /**
     * OX Crossover operation
     * @param parent0 First parent
     * @param parent1 Second parent
     */
    private Individual[] crossover(Individual parent0, Individual parent1) {
        Random random = new Random();

        // Create children
        Individual[] children  = new Individual[2];
        children[0] = new Individual();
        children[1] = new Individual();

        // Generating positions to cut
        int position1, position2;
        position1 = random.nextInt(PROBLEM_SIZE);
        do {
            position2 = random.nextInt(PROBLEM_SIZE);
        } while (position1 == position2); // cannot repeat the same position

        // First must be lower than second
        if (position2 < position1) {
            int swap = position1;
            position1 = position2;
            position2 = swap;
        }

        // Sets for a quicker search if a number is already in the solution of a child
        Set<Integer> individualsInChildren0 = new HashSet<>();
        Set<Integer> individualsInChildren1 = new HashSet<>();

        // Copy between the positions the partial solution of each parent
        for (int i = position1; i < position2; i++) {
            // Children 0 copies from parent 0
            children[0].getSolution()[i] = parent0.getSolution()[i];
            individualsInChildren0.add(children[0].getSolution()[i]);
            // Children 1 copies from parent 1
            children[1].getSolution()[i] = parent1.getSolution()[i];
            individualsInChildren1.add(children[1].getSolution()[i]);
        }

        for (int i = 0; i < PROBLEM_SIZE; i++) { // iterate children
            if (i < position1 || i >= position2) { // not between the positions
                // Search for the first value in the parent 1 which is not currently in the child 0
                int iterator = 0;
                while (individualsInChildren0.contains(parent1.getSolution()[iterator])) {
                    iterator++;
                }
                // Children 0 copies from parent 1
                children[0].getSolution()[i] = parent1.getSolution()[iterator];
                individualsInChildren0.add(parent1.getSolution()[iterator]);

                // Search for the first value in the parent 1 which is not currently in the child 0
                iterator = 0;
                while (individualsInChildren1.contains(parent0.getSolution()[iterator])) {
                    iterator++;
                }
                // Children 1 copies from parent 0
                children[1].getSolution()[i] = parent0.getSolution()[iterator];
                individualsInChildren1.add(parent0.getSolution()[iterator]);
            }
        }

        return children;
    }

    private void mutate(Individual individual) {
        Random random = new Random();
        double prob = random.nextDouble();
        // Check if mutate
        if (prob < INDIVIDUAL_MUTATION_PROBABILITY) {
            for (int i = 0; i < PROBLEM_SIZE; i++) {
                prob = random.nextDouble();
                if (prob < GENE_MUTATION_PROBABILITY) {
                    // Position to mutate with i
                    int j;
                    do {
                        j = random.nextInt(PROBLEM_SIZE);
                    } while (j == i); // cannot mutate the same position
                    individual.swap(i, j); // mutate
                }
            }
        }
    }

}
