package bio.pih.util.mutation;

import java.util.Random;

import org.biojava.bio.symbol.IllegalSymbolException;

public class SequenceMutator {

	private static Random random = new Random();

	public static double DEFAULT_MUTATE_BASE_RATION = 0.05;
	public static double DEFAULT_DELETION_RATIO = 0.05;
	public static double DEFAULT_INSERTION_RATIO = 0.04;
	public static double DEFAULT_DUPLICATION_RATIO = 0.01;
	public static double DEFAULT_DESLOCATION_RATIO = 0.01;
	public static double DEFAULT_INVERSION_RATIO = 0.01;
	public static double DEFAULT_DESLOCATION_INVERSION_RATIO = 0.01;
	public static int DEFAULT_PROPORSION_SIZE = 4;

	static int PROBABILITIES_VECTOR_SIZE = 10000;
	public static int[] DEFAULT_PROBABILITY_VECTOR = fillProbabilitiesVector(DEFAULT_MUTATE_BASE_RATION, DEFAULT_DELETION_RATIO, DEFAULT_INSERTION_RATIO, DEFAULT_DUPLICATION_RATIO, DEFAULT_DESLOCATION_RATIO, DEFAULT_INVERSION_RATIO, DEFAULT_DESLOCATION_INVERSION_RATIO);

	// Informations to fill the proabibilities vector.
	static final int NOTHING = 0;
	static final int MUTATE_BASE = 1;
	static final int DELETION = 2;
	static final int INSERTION = 3;
	static final int DUPLICATION = 4;
	static final int DESLOCATION = 5;
	static final int INVERSION = 6;
	static final int DESLOCATION_INVERSION = 7;

	public static int[] fillProbabilitiesVector(double mutateBase, double deletion, double insertion, double duplication, double deslocation, double inversion, double deslocationWithInversion) {
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
		for (int i = pos; i < (deslocation * 1000) + pos; i++) {
			probabilitiesVector[i] = DESLOCATION;
		}

		pos += deslocation * 1000;
		for (int i = pos; i < (inversion * 1000) + pos; i++) {
			probabilitiesVector[i] = INVERSION;
		}

		pos += inversion * 1000;
		for (int i = pos; i < (deslocationWithInversion * 1000) + pos; i++) {
			probabilitiesVector[i] = DESLOCATION_INVERSION;
		}

		return probabilitiesVector;
	}

	/**
	 * @param sequence
	 * @param generations
	 * @param proporsionSize
	 * @param probabilitiesVector
	 * @return
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

			case DESLOCATION:
				sequence = mutateDeslocation(sequence, proporsionSize);
				break;

			case INVERSION:
				sequence = mutateInvertation(sequence);
				break;

			case DESLOCATION_INVERSION:
				sequence = mutateDeslocationInvertation(sequence, proporsionSize);
				break;
			}
		}

		return sequence;
	}

	public static void main(String[] args) throws IllegalSymbolException {
		String sequence = randomSequence(10);

		System.out.println(sequence + "(entrada)");
		String nSequence = mutateSequence(sequence, 100, 4, DEFAULT_PROBABILITY_VECTOR);
		System.out.println(nSequence + "(100 geracoes)");
		 nSequence = mutateSequence(sequence, 1000, 4, DEFAULT_PROBABILITY_VECTOR);
		System.out.println(nSequence + "(1000 geracoes)");
		 nSequence = mutateSequence(sequence, 10000, 4, DEFAULT_PROBABILITY_VECTOR);
		System.out.println(nSequence + "(10000 geracoes)");
	}

	public static String mutateBase(String sequence) {
		if (sequence.length() == 0) {
			return sequence;
		}
		int pos = randomPos(sequence);
		char[] charArray = sequence.toCharArray();
		charArray[pos] = getRandomBase();

		return new String(charArray);
	}

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

	public static String mutateInsertion(String sequence, int maxSizeProportion) {
		assert maxSizeProportion >= 1;

		int pos = randomPos(sequence);
		int insertionLength = randomLength(sequence, maxSizeProportion);
		char[] insertionSequence = randomSequence(insertionLength).toCharArray();

		char[] charArray = insertSequence(sequence, pos, insertionSequence);

		return new String(charArray);
	}

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

	public static String mutateDeslocation(String sequence, int maxSizeProportion) {
		if (sequence.length() == 0 || sequence.length() == 1) {
			return sequence;
		}
		assert maxSizeProportion >= 1;

		int remotionPos = randomPos(sequence);
		int deslocationLength = randomLength(sequence, maxSizeProportion);

		if (remotionPos + deslocationLength > sequence.length() - 1) {
			remotionPos = sequence.length() - deslocationLength;
		}
		String deslocationSequence = sequence.substring(remotionPos, remotionPos + deslocationLength);

		char[] charArray = new char[sequence.length() - deslocationLength];

		for (int i = 0; i < remotionPos; i++) {
			charArray[i] = sequence.charAt(i);
		}
		for (int i = remotionPos + deslocationLength; i < sequence.length(); i++) {
			charArray[i - deslocationLength] = sequence.charAt(i);
		}

		int destinationPos = randomPos(charArray.length);
		charArray = insertSequence(new String(charArray), destinationPos, deslocationSequence.toCharArray());

		return new String(charArray);
	}

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

	public static String mutateDeslocationInvertation(String sequence, int maxSizeProportion) {
		if (sequence.length() == 0 || sequence.length() == 1) {
			return sequence;
		}
		assert maxSizeProportion >= 1;

		int remotionPos = randomPos(sequence);
		int deslocationLength = randomLength(sequence, maxSizeProportion);

		if (remotionPos + deslocationLength > sequence.length() - 1) {
			remotionPos = sequence.length() - deslocationLength;
		}

		String deslocationSequence = sequence.substring(remotionPos, remotionPos + deslocationLength);
		deslocationSequence = mutateInvertation(deslocationSequence);

		char[] charArray = new char[sequence.length() - deslocationLength];

		for (int i = 0; i < remotionPos; i++) {
			charArray[i] = sequence.charAt(i);
		}
		for (int i = remotionPos + deslocationLength; i < sequence.length(); i++) {
			charArray[i - deslocationLength] = sequence.charAt(i);
		}

		int destinationPos = randomPos(charArray.length);
		charArray = insertSequence(new String(charArray), destinationPos, deslocationSequence.toCharArray());

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

	public static String randomSequence(int length) {
		char[] charArray = new char[length];
		for (int i = 0; i < length; i++) {
			charArray[i] = getRandomBase();
		}
		return new String(charArray);
	}

	static char[] bases = new char[] { 'A', 'C', 'G', 'T' };

	public static char getRandomBase() {
		return bases[random.nextInt(4)];
	}

	/**
	 * 1.0 = 100% 0.5 = 50%
	 * 
	 * @param probability
	 * @return <code>true</code> or <code>false</code> related with the given probability
	 */
	private boolean test(double probability) {
		if (random.nextDouble() <= probability) {
			return true;
		}
		return false;
	}
}
