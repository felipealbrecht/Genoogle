/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import bio.pih.genoogle.seq.DNAAlphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.Sequence;

/**
 * @author Albrecht
 * 
 * A simple sequence populator.
 */
public class DNASequencesPopulator {

	/**
	 * Create a random sequence population
	 * 
	 * @param sequenceQuantity
	 * @param sizeFrom
	 * @param sizeTo
	 * @return a {@link List} containing random sequences
	 */
	public static List<Sequence> populateSequences(int sequenceQuantity, int sizeFrom, int sizeTo) throws IllegalSymbolException {

		List<Sequence> sequences = new LinkedList<Sequence>();
		RandomSequenceGenerator randomSequenceGenerator = new RandomSequenceGenerator(DNAAlphabet.SINGLETON, sizeFrom, sizeTo);
		for (int i = 0; i < sequenceQuantity; i++) {
			sequences.add(randomSequenceGenerator.generateSequence());
		}

		return sequences;
	}

	/**
	 * Write a sequence population to a file.
	 * 
	 * @param sequences
	 * @param path
	 * @return <code>true</code> if the operation was done successfully
	 */
	public static boolean writePopulation(List<Sequence> sequences, String path) throws FileNotFoundException, IOException {
		File file = new File(path);
		if (file.exists()) {
			return false;
		}

		new ObjectOutputStream(new FileOutputStream(file)).writeObject(sequences);
		return true;
	}

	/**
	 * Read a sequence population from a given path
	 * 
	 * @param path
	 * @return a {@link List} containing {@link Sequence}
	 */
	@SuppressWarnings("unchecked")
	public static List<Sequence> readPopulation(String path) throws FileNotFoundException, IOException, ClassNotFoundException {
		File file = new File(path);
		if (file.exists()) {
			List<Sequence> sequences = (List<Sequence>) new ObjectInputStream(new FileInputStream(file)).readObject();
			return sequences;
		}
		return null;

	}

	/**
	 * @param sequences
	 * @param path
	 */
	public static void writePopulationAsFasta(List<Sequence> sequences, String path) throws IOException {
//		File file = new File(path);
//		FileOutputStream fos = new FileOutputStream(file);                      
		
		// TODO: write to a file.
		for (Sequence sequence : sequences) {
			System.out.println(sequence);
		}
	}

}
