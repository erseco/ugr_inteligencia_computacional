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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import qap.Genetic.AlgorithmType;

/**
 * 
 * @author Ernesto Serrano
 */
public class Individual implements Comparable<Individual> {
        
    private int[] solution;

    private int fitness;
    
    public Individual() {
        this.solution = new int[Genetic.PROBLEM_SIZE];
        this.fitness = Integer.MAX_VALUE;
        this.generateRandomSolution();
    }

    public Individual(Individual source) {
        solution = new int[Genetic.PROBLEM_SIZE];
        System.arraycopy(source.solution, 0, solution, 0, source.solution.length);
        fitness = source.fitness;
    }

    public void calculateFitness(AlgorithmType algType) {
        switch (algType) {
            case LAMARCKIAN:
                calculateFitnesLamarckian();
                break;
            case BALDWINIAN:
                calculateFitnesBalwinian();
                break;
            default:
                calculateFitnesStandard();
        }
    }

    public void calculateFitnesStandard (){
        int newFitness = 0;
        for (int i = 0; i < Genetic.PROBLEM_SIZE; i++) {
            for (int j = 0; j < Genetic.PROBLEM_SIZE; j++) {                
                newFitness += Genetic.flow_matrix.get(i).get(j) * Genetic.distance_matrix.get(solution[i]).get(solution[j]);
            }
        }
        fitness = newFitness;
    }

    public void calculateFitnesLamarckian (){
        // Changes the current individual using the optimized individual values
        Individual optimized = greedy();
        solution = new int[Genetic.PROBLEM_SIZE];
        System.arraycopy(optimized.solution, 0, solution, 0, optimized.solution.length);
        fitness = optimized.fitness;
    }

    public void calculateFitnesBalwinian (){
        // Only changes the fitness using the optimized individual one
        Individual optimized = greedy();
        this.fitness = optimized.getFitness();
    }
    
    public int getFitness() {
        return fitness;
    }
    
    public int[] getSolution() {
        return solution;
    }

    public void swap(int pos1, int pos2) {
        int gene = solution[pos1];
        solution[pos1] = solution[pos2];
        solution[pos2] = gene;
    }

    private void generateRandomSolution() {
        
        // Initialize ordered list
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < Genetic.PROBLEM_SIZE; i++)
            list.add(i);

        // Shuffle list
        Collections.shuffle(list, new Random());
        
        // Copy to primitive array
        for (int i = 0; i < Genetic.PROBLEM_SIZE; i++) {
            solution[i] = list.get(i);
        }
                
        // Calculate fitness
        calculateFitness(AlgorithmType.STANDARD);
    }

    /**
     * Greedy 2-opt algorithm to optimize an individual
     */
    private Individual greedy() {
        Individual best;
        Individual S = new Individual(this);
        S.calculateFitnesStandard();
        do {
            best = new Individual(S); // save best solution by now
            for (int i = 0; i < solution.length; i++) {
                for (int j = i + 1; j < solution.length; j++) {
                    // Create T exchanging i and j gene
                    Individual T = new Individual(S);
                    T.solution[i] = S.solution[j];
                    T.solution[j] = S.solution[i];
                    
                    T.calculateFitnesStandard();
                    
                    if (T.getFitness() < S.getFitness()) { // if new solution is better than older updates
                        S = new Individual(T);
                    }
                }
            }
        } while (S.getFitness() < best.getFitness());
        return S;
    }
    
    @Override
    public String toString() {
        return Arrays.toString(this.solution);
    }
    
    @Override
    public int compareTo(Individual o) {
        return this.fitness - o.fitness;
    }

}
