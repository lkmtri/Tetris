import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class GeneticOptimization {	
	
	private static final int POPULATION_SIZE = 16;
	private static final double MUTATION_PROB = 0.1;
	private static String[] population = new String[POPULATION_SIZE];
	public static final int CHROMOSOME_SIZE = 28;
	public static final int NUM_OF_WEIGHT = 8;
	private static int[] selected = new int[POPULATION_SIZE / 2];
	private static int[] trashed = new int[POPULATION_SIZE / 2];
	

	private void readWeightFromFile() {
		Scanner reader;
		try {
			reader = new Scanner(new FileReader("Result.txt"));
			for(int i = 0; i < POPULATION_SIZE; i++) {
				population[i] = reader.nextLine();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void printPopulation(int gen) {
		FileWriter fstream;
		try {
			fstream = new FileWriter("Gen.txt",true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("Gen " + gen + "--------------\n");
			for(int i = 0; i < POPULATION_SIZE; i++) {
				out.write(population[i] + "\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generateNewGen() {
		shufflePopulation();
		selectParent();
		generateChild();
	}
	
	private void generateChild() {
		for(int i = 0; i < selected.length; i+=2) {
			String[] child = crossOverAndMutation(population[selected[i]], population[selected[i+1]]);
			population[trashed[i]] = child[0];
			population[trashed[i+1]] = child[1];
		}
	}
	
	private String[] crossOverAndMutation(String parent1, String parent2) {
		// crossover
		int crossPos = (int) (Math.random() * parent1.length() - 1);
		StringBuilder child1 = new StringBuilder(parent1.substring(0, crossPos) +  parent2.substring(crossPos));
		StringBuilder child2 = new StringBuilder(parent2.substring(0, crossPos) +  parent1.substring(crossPos));
		String[] newChild = new String[2]; 
		// mutation
		if(Math.random() < MUTATION_PROB) {
			int mutationPos = (int) (Math.random() * parent1.length());
			child1.setCharAt(mutationPos, Character.forDigit((int)(Math.random()*10), 10));
		}
		if(Math.random() < MUTATION_PROB) {
			int mutationPos = (int) (Math.random() * parent1.length());
			child2.setCharAt(mutationPos, Character.forDigit((int)(Math.random()*10), 10));
		}
		newChild[0] = child1.toString();
		newChild[1] = child2.toString();

		return newChild;
	}

	private void selectParent() {
		PlayerSkeleton player = new PlayerSkeleton();
		for(int i = 0; i < POPULATION_SIZE; i+=2) {
			if(player.runGame(population[i]) > player.runGame(population[i+1])) {
				selected[i/2] = i;
				trashed[i/2] = i + 1;
			} else {
				selected[i/2] = i + 1;
				trashed[i/2] = i;
			}
		}
	}
	
	private void shufflePopulation() {
		ArrayList<String> pop = new ArrayList<String>();
		for(int i = 0; i < POPULATION_SIZE; i++) {
			pop.add(population[i]);
		}
		Random rand = new Random();
		Collections.shuffle(pop, rand);
		for(int i = 0; i < POPULATION_SIZE; i++) {
			population[i] = pop.get(i);
		}
		pop.clear();
		return;
	}
	
	public static void main(String[] args) {
		GeneticOptimization genAlgo = new GeneticOptimization();
		genAlgo.readWeightFromFile();
		int numOfGen = 0;
		while(numOfGen < 1000) {
			System.out.printf("-----Generation %d \n", numOfGen);
			genAlgo.printPopulation(numOfGen);
			genAlgo.generateNewGen();
			numOfGen++;
		}
		return;
	}
}
