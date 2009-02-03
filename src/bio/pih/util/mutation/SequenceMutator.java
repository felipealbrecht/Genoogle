package bio.pih.util.mutation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 * @author albrecht
 *
 */
/**
 * @author albrecht (felipe.albrecht@gmail.com)
 *
 * TODO: javadoc methods comments, a better main method. 
 */
public class SequenceMutator {

	private static Random random = new Random();

	/**
	 * Default probability for a base mutation occurs.
	 */
	public static final double DEFAULT_MUTATE_BASE_RATION = 0.25;
		
	/**
	 * Default probability for a sub-sequence be inserted occurs.
	 */
	public static final double DEFAULT_INSERTION_RATIO = 0.005;
	
	/**
	 * Default probability for a sub-sequence be removed occurs.
	 */
	public static final double DEFAULT_REMOTION_RATIO = 0.01;
	
	/**
	 * Default probability for a sub-sequence be duplicated occurs.
	 */
	public static final double DEFAULT_DUPLICATION_RATIO = 0.005;
	
	/**
	 * Default probability for a sub-sequence be dislocated occurs.
	 */
	public static final double DEFAULT_DISLOCATION_RATIO = 0.005;
	
	/**
	 * Default probability for a sub-sequence be inverted occurs.
	 */
	public static final double DEFAULT_INVERSION_RATIO = 0.005;

	/**
	 * Default probability for a sub-sequence be inverted occurs.
	 */
	public static final double DEFAULT_DISLOCATION_INVERSION_RATIO = 0.005;

	/**
	 * Proportional size related with sequence that will be modified.
	 * <p>Example, for a deletion mutation on a 20 bases length sequence and proportional_size of 4
	 *  the maximum size of the deleted sub-sequence will be 20/4: 5 bases.
	 */
	public static final int DEFAULT_PROPORTION_SIZE = 4;

	private final static int PROBABILITIES_VECTOR_SIZE = 10000;
	private static final int[] DEFAULT_PROBABILITY_VECTOR = createProbabilitiesVector(DEFAULT_MUTATE_BASE_RATION, DEFAULT_INSERTION_RATIO, DEFAULT_REMOTION_RATIO, DEFAULT_DUPLICATION_RATIO, DEFAULT_DISLOCATION_RATIO, DEFAULT_INVERSION_RATIO, DEFAULT_DISLOCATION_INVERSION_RATIO);

	// Informations to fill the probabilities vector.
	static final int NOTHING = 0;
	static final int MUTATE_BASE = 1;
	static final int DELETION = 2;
	static final int INSERTION = 3;
	static final int REMOTION = 4;
	static final int DUPLICATION = 5;
	static final int DISLOCATION = 6;
	static final int INVERSION = 7;
	static final int DISLOCATION_INVERSION = 8;

	/**
	 * Create a probabilities vector with the given probabilities.
	 * The sensibilities for each mutation came from 0.001 (0.00001%) to 10,000.00 (100%)
	 * This methods do <b>not</b> check if a single or the total probabilities are higher then   
	 * @param mutateBase
	 * @param deletion
	 * @param insertion
	 * @param duplication
	 * @param dislocation
	 * @param inversion
	 * @param dislocationWithInversion
	 * @return a vector with the probabilities.
	 */
	public static int[] createProbabilitiesVector(double mutateBase, double deletion, double insertion, double duplication, double dislocation, double inversion, double dislocationWithInversion) {
		int[] probabilitiesVector = new int[PROBABILITIES_VECTOR_SIZE];

		int pos = 0;
		for (int i = pos; i < mutateBase * 1000; i++) {
			probabilitiesVector[i] = MUTATE_BASE;
		}

		pos += mutateBase * 1000;
		for (int i = pos; i < (deletion * 1000) + pos; i++) {
			probabilitiesVector[i] = DELETION;
		}

		pos += deletion * 1000;
		for (int i = pos; i < (insertion * 1000) + pos; i++) {
			probabilitiesVector[i] = INSERTION;
		}

		pos += insertion * 1000;
		for (int i = pos; i < (duplication * 1000) + pos; i++) {
			probabilitiesVector[i] = DUPLICATION;
		}

		pos += duplication * 1000;
		for (int i = pos; i < (dislocation * 1000) + pos; i++) {
			probabilitiesVector[i] = DISLOCATION;
		}

		pos += dislocation * 1000;
		for (int i = pos; i < (inversion * 1000) + pos; i++) {
			probabilitiesVector[i] = INVERSION;
		}

		pos += inversion * 1000;
		for (int i = pos; i < (dislocationWithInversion * 1000) + pos; i++) {
			probabilitiesVector[i] = DISLOCATION_INVERSION;
		}

		return probabilitiesVector;
	}

	/**
	 * Same as mutateSequence(String sequence, int generations, int proporsionSize, int[] probabilitiesVector)
	 * but using default probability vector.
	 * 
	 * @param sequence
	 * @param generations
	 * @param proporsionSize
	 * @return sequence with mutations.
	 */
	public static String mutateSequence(String sequence, int generations, int proporsionSize) {
		return mutateSequence(sequence, generations, proporsionSize, DEFAULT_PROBABILITY_VECTOR);
	}
	
	/**
	 * Mutate the input sequence using the probabilities vector generations times.
	 * 
	 * @param sequence
	 * @param generations
	 * @param proporsionSize
	 * @param probabilitiesVector
	 * @return a new sequence mutated.
	 */
	public static String mutateSequence(String sequence, int generations, int proporsionSize, int[] probabilitiesVector) {

		for (int i = 0; i < generations; i++) {
			switch (probabilitiesVector[random.nextInt(PROBABILITIES_VECTOR_SIZE)]) {

			case MUTATE_BASE:
				sequence = mutateBase(sequence);
				break;

			case DELETION:
				sequence = mutateDelete(sequence, proporsionSize);
				break;

			case INSERTION:
				sequence = mutateInsertion(sequence, proporsionSize);
				break;

			case DUPLICATION:
				sequence = mutateDuplication(sequence, proporsionSize);
				break;

			case DISLOCATION:
				sequence = mutateDislocation(sequence, proporsionSize);
				break;

			case INVERSION:
				sequence = mutateInvertation(sequence);
				break;

			case DISLOCATION_INVERSION:
				sequence = mutateDislocationInvertation(sequence, proporsionSize);
				break;
			}
		}

		return sequence;
	}

	/**
	 * Simple main for test and fast applications.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length == 1) {
			printHelp();			
		}
		
		File sequenceFile = new File(args[0]);
		if (!sequenceFile.exists()) {
			throw new FileNotFoundException("File " + args[0] + " was not found.");
		}
		String sequence = readSequence(sequenceFile);
		int generations = Integer.parseInt(args[1]);
		System.out.println(mutateSequence(sequence, generations, 4, DEFAULT_PROBABILITY_VECTOR));
		
	}
	
	private static void printHelp() {
		System.out.println("SequenceMutator help:");
		System.out.println("<sequence> <generations>");
		System.out.println("Exemple: java SequenceMutator <file name> 100");
		System.out.println("to change the probabilities values, change at souce code :-)");
		System.out.println("It's only a main for test propose, please, implement yours application and use this class.");
	}
	
	public static String readSequence(File sequenceFile) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(sequenceFile));
		StringBuilder sb = new StringBuilder();
		
		while (br.ready()) {
			String line = br.readLine().trim();
			line = line.replaceAll("[\n|\r]", "");
			sb.append(line);
		}
		return sb.toString();
	}

	/**
	 * Change a random base in the input sequence.
	 * 
	 * @param sequence
	 * @return a new sequence with the mutation.
	 */
	public static String mutateBase(String sequence) {
		if (sequence.length() == 0) {
			return sequence;
		}
		int pos = randomPos(sequence);
		char[] charArray = sequence.toCharArray();
		charArray[pos] = getRandomBase();

		return new String(charArray);
	}

	/**
	 * Delete a random sub-sequence.
	 * 
	 * @param sequence 
	 * @param maxSizeProportion max proportion of the sequence that will be removed.
	 * 
	 * @return a new sequence with the mutation.
	 */
	public static String mutateDelete(String sequence, int maxSizeProportion) {
		if (sequence.length() == 0) {
			return sequence;
		}
		assert maxSizeProportion >= 1;

		int deletationLength = randomLength(sequence, maxSizeProportion);
		int pos = randomPos(sequence.length() - deletationLength);

		char[] charArray = new char[sequence.length() - deletationLength];
		for (int i = 0; i < pos; i++) {
			charArray[i] = sequence.charAt(i);
		}
		for (int i = pos + deletationLength; i < sequence.length(); i++) {
			charArray[i - deletationLength] = sequence.charAt(i);
		}

		return new String(charArray);
	}

	/**
	 * Insert a random sub-sequence.
	 * 
	 * @param sequence 
	 * @param maxSizeProportion max proportion of the sequence that will be inserted.
	 * 
	 * @return a new sequence with the mutation.
	 */
	public static String mutateInsertion(String sequence, int maxSizeProportion) {
		assert maxSizeProportion >= 1;

		int pos = randomPos(sequence);
		int insertionLength = randomLength(sequence, maxSizeProportion);
		char[] insertionSequence = randomSequence(insertionLength).toCharArray();

		char[] charArray = insertSequence(sequence, pos, insertionSequence);

		return new String(charArray);
	}

	/**
	 * Duplicate a random sub-sequence and put into a random place in the sequence.
	 * 
	 * @param sequence 
	 * @param maxSizeProportion max proportion of the sequence that will be duplicated.
	 * 
	 * @return a new sequence with the mutation.
	 */
	public static String mutateDuplication(String sequence, int maxSizeProportion) {
		if (sequence.length() == 0) {
			return sequence;
		}

		assert maxSizeProportion >= 1;

		int insertionPos = randomPos(sequence);
		int duplicatePos = randomPos(sequence);
		int duplicationLength = randomLength(sequence, maxSizeProportion);

		if (duplicatePos + duplicationLength > sequence.length() - 1) {
			duplicatePos = sequence.length() - duplicationLength;
		}
		String duplicateSequence = sequence.substring(duplicatePos, duplicatePos + duplicationLength);

		char[] charArray = insertSequence(sequence, insertionPos, duplicateSequence.toCharArray());

		return new String(charArray);
	}

	/**
	 * Dislocate a random sub-sequence and put into a random place in the sequence.
	 * 
	 * @param sequence 
	 * @param maxSizeProportion max proportion of the sequence that will be dislocated.
	 * 
	 * @return a new sequence with the mutation.
	 */
	public static String mutateDislocation(String sequence, int maxSizeProportion) {
		if (sequence.length() == 0 || sequence.length() == 1) {
			return sequence;
		}
		assert maxSizeProportion >= 1;

		int remotionPos = randomPos(sequence);
		int dislocationLength = randomLength(sequence, maxSizeProportion);

		if (remotionPos + dislocationLength > sequence.length() - 1) {
			remotionPos = sequence.length() - dislocationLength;
		}
		String dislocationSequence = sequence.substring(remotionPos, remotionPos + dislocationLength);

		char[] charArray = new char[sequence.length() - dislocationLength];

		for (int i = 0; i < remotionPos; i++) {
			charArray[i] = sequence.charAt(i);
		}
		for (int i = remotionPos + dislocationLength; i < sequence.length(); i++) {
			charArray[i - dislocationLength] = sequence.charAt(i);
		}

		int destinationPos = randomPos(charArray.length);
		charArray = insertSequence(new String(charArray), destinationPos, dislocationSequence.toCharArray());

		return new String(charArray);
	}
	
	/**
	 * Invert a random sub-sequence.
	 * 
	 * @param sequence 
	 * @return a new sequence with the mutation.
	 */
	public static String mutateInvertation(String sequence) {
		if (sequence.length() <= 1) {
			return sequence;
		}

		char[] charArray = new char[sequence.length()];

		for (int i = 0; i < sequence.length(); i++) {
			charArray[sequence.length() - i - 1] = sequence.charAt(i);
		}

		return new String(charArray);
	}

	/**
	 * Dislocate and invert a random sub-sequence.
	 * 
	 * @param sequence 
	 * @param maxSizeProportion max proportion of the sequence that will be dislocate and inverted.
	 * 
	 * @return a new sequence with the mutation.
	 */
	public static String mutateDislocationInvertation(String sequence, int maxSizeProportion) {
		if (sequence.length() == 0 || sequence.length() == 1) {
			return sequence;
		}
		assert maxSizeProportion >= 1;

		int remotionPos = randomPos(sequence);
		int dislocationLength = randomLength(sequence, maxSizeProportion);

		if (remotionPos + dislocationLength > sequence.length() - 1) {
			remotionPos = sequence.length() - dislocationLength;
		}

		String dislocationSequence = sequence.substring(remotionPos, remotionPos + dislocationLength);
		dislocationSequence = mutateInvertation(dislocationSequence);

		char[] charArray = new char[sequence.length() - dislocationLength];

		for (int i = 0; i < remotionPos; i++) {
			charArray[i] = sequence.charAt(i);
		}
		for (int i = remotionPos + dislocationLength; i < sequence.length(); i++) {
			charArray[i - dislocationLength] = sequence.charAt(i);
		}

		int destinationPos = randomPos(charArray.length);
		charArray = insertSequence(new String(charArray), destinationPos, dislocationSequence.toCharArray());

		return new String(charArray);
	}

	private static char[] insertSequence(String sequence, int pos, char[] insertionSequence) {

		char[] charArray = new char[sequence.length() + insertionSequence.length];
		for (int i = 0; i < pos; i++) {
			charArray[i] = sequence.charAt(i);
		}
		for (int i = pos; i < pos + insertionSequence.length; i++) {
			charArray[i] = insertionSequence[i - pos];
		}
		for (int i = pos + insertionSequence.length; i < sequence.length() + insertionSequence.length; i++) {
			charArray[i] = sequence.charAt(i - insertionSequence.length);
		}
		return charArray;
	}

	private static int randomLength(String sequence, int maxSizeProportion) {
		assert maxSizeProportion >= 1;
		// random.next(1) will return always 0, so, the probability to return 1 should be calculed on maxSizeProportion
		if ((sequence.length() / maxSizeProportion) <= 1) {
			return calculateBonus(maxSizeProportion);
		}
		return randomPos(sequence.length() / maxSizeProportion);
	}

	private static int calculateBonus(int maxSizeProportion) {
		double change = ((1 / (double) maxSizeProportion) * 100);
		if (random.nextInt(100) <= change) {
			return 1;
		}
		return 0;
	}

	private static int randomPos(String sequence) {
		return randomPos(sequence.length());
	}

	private static int randomPos(int length) {
		if (length <= 1) {
			return 0;
		}
		return random.nextInt(length);
	}

	/**
	 * Create a random sequence.
	 * 
	 * @param length of the new sequence.
	 * 
	 * @return the generated sequence.
	 */
	public static String randomSequence(int length) {
		char[] charArray = new char[length];
		for (int i = 0; i < length; i++) {
			charArray[i] = getRandomBase();
		}
		return new String(charArray);
	}

	static char[] bases = new char[] { 'A', 'C', 'G', 'T' };

	/**
	 * Get a random DNA base.
	 * 
	 * @return a random DNA base.
	 */
	public static char getRandomBase() {
		return bases[random.nextInt(4)];
	}
}
