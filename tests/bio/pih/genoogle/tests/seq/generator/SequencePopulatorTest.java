/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.tests.seq.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.junit.AfterClass;
import org.junit.Test;

import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.seq.generator.DNASequencesPopulator;
import bio.pih.genoogle.seq.generator.RandomSequenceGenerator;
import bio.pih.genoogle.seq.op.LightweightIOTools;
import bio.pih.genoogle.seq.op.LightweightStreamReader;

/**
 * @author albrecht
 *
 */
public class SequencePopulatorTest extends TestCase {

	
	private static final String sequencePopulationTestFile = "data" + File.separator + "populator" + File.separator + "sequencePopulatorTest.seqs";
	
	@Override
	@AfterClass
	public void tearDown() {
		try {
			removeIfExistFile(sequencePopulationTestFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void removeIfExistFile(String filePath) throws IOException {
		File serializableSequencePopulationFile = new File(filePath);
		if (serializableSequencePopulationFile.exists()) {
			if (serializableSequencePopulationFile.delete() == false) {
				throw new IOException("Was not possible to delete " + serializableSequencePopulationFile);
			}
		}		
	}
	
	/**
	 * Test if the length and {@link Alphabet} of sequence generated are correct.  
	 */
	//@Test
	public void testSequenceGenerator() {
		int length = 1;
		RandomSequenceGenerator randomSequenceGenerator = new RandomSequenceGenerator(DNATools.getDNA(), length);
		Sequence sequence = randomSequenceGenerator.generateSequence();
		assertEquals(1, sequence.length());
		assertEquals(DNATools.getDNA(), sequence.getAlphabet());
		
		length = 10;
		randomSequenceGenerator = new RandomSequenceGenerator(DNATools.getDNA(), length);
		sequence = randomSequenceGenerator.generateSequence();
		assertEquals(length, sequence.length());
		assertEquals(DNATools.getDNA(), sequence.getAlphabet());
		
		length = 100;
		randomSequenceGenerator = new RandomSequenceGenerator(DNATools.getDNA(), length);
		sequence = randomSequenceGenerator.generateSequence();
		assertEquals(length, sequence.length());
		assertEquals(DNATools.getDNA(), sequence.getAlphabet());
		
		length = 250;
		randomSequenceGenerator = new RandomSequenceGenerator(DNATools.getDNA(), length);
		sequence = randomSequenceGenerator.generateSequence();
		assertEquals(length, sequence.length());
		assertEquals(DNATools.getDNA(), sequence.getAlphabet());
		
		length = 1000;
		randomSequenceGenerator = new RandomSequenceGenerator(DNATools.getDNA(), length);
		sequence = randomSequenceGenerator.generateSequence();
		assertEquals(length, sequence.length());
		assertEquals(DNATools.getDNA(), sequence.getAlphabet());
		
		length = 10000;
		randomSequenceGenerator = new RandomSequenceGenerator(DNATools.getDNA(), length);
		sequence = randomSequenceGenerator.generateSequence();
		assertEquals(length, sequence.length());
		assertEquals(DNATools.getDNA(), sequence.getAlphabet());		
	}
	
	/**
	 * Test if the length of the generated sequences are correct
	 */
	//@Test
	public void testDNASequencesPopulator() {
		int from = 0;
		int to = 3;
		List<Sequence> sequences = DNASequencesPopulator.populateSequences(100, from, to);
		for(Sequence sequence: sequences) {
			assertTrue(sequence.length() >= from);
			assertTrue(sequence.length() <= to);
		}
		
		from = 0;
		to = 100;
		sequences = DNASequencesPopulator.populateSequences(100, from, to);
		for(Sequence sequence: sequences) {
			assertTrue(sequence.length() >= from);
			assertTrue(sequence.length() <= to);
		}
		
		from = 99;
		to = 100;
		sequences = DNASequencesPopulator.populateSequences(100, from, to);
		for(Sequence sequence: sequences) {
			assertTrue(sequence.length() >= from);
			assertTrue(sequence.length() <= to);
		}
		
		from = 0;
		to = 1000;
		sequences = DNASequencesPopulator.populateSequences(100, from, to);
		for(Sequence sequence: sequences) {
			assertTrue(sequence.length() >= from);
			assertTrue(sequence.length() <= to);
		}
		
		from = 999;
		to = 1000;
		sequences = DNASequencesPopulator.populateSequences(100, from, to);
		for(Sequence sequence: sequences) {
			assertTrue(sequence.length() >= from);
			assertTrue(sequence.length() <= to);
		}
		
		from = 1;
		to = 2;
		sequences = DNASequencesPopulator.populateSequences(100, from, to);
		for(Sequence sequence: sequences) {
			assertTrue(sequence.length() >= from);
			assertTrue(sequence.length() <= to);
		}	
	}
	
	/**
	 * Test if the save and load sequence population from a file is working
	 * @throws IllegalSymbolException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	//@Test
	public void testCreateSaveAndLoadSequencePopulation() throws IllegalSymbolException, FileNotFoundException, IOException, ClassNotFoundException {
		List<Sequence> sequences = new LinkedList<Sequence>();
		
		String stringSequence = "CATGACTGGCATCAGTGCATGCATGCAGTCAGTATATATGACGC";
		SymbolList symbolList = LightweightSymbolList.createDNA(stringSequence);
		Sequence ss = new SimpleSequence(symbolList, null, "Sequence 1", null);
		sequences.add(ss);
		
		stringSequence = "ACATGCTCGATGTGTGTGTATCAGTACTGACCTAGCATGACTCAGTACACATGACGTCATCATGTAGCGTCTAGACTGACTACGTACGACTGCATACGACTATCAGACTGACTACGCATGACGTACGTGTACGTACTGATGACGTACTATCGTAGCATGACTACGTACGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence 2", null);
		sequences.add(ss);
		
		stringSequence = "ATGCTAGCATTCAGTACGTACGCATGATGCTAGATCGCATGACTAGCACGTACTGCATCGTGTGTGTCATGTGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence 3", null);
		sequences.add(ss);
		
		stringSequence = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence A", null);
		sequences.add(ss);
		
		stringSequence = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence T", null);
		sequences.add(ss);
		
		stringSequence = "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence C", null);
		sequences.add(ss);
		
		stringSequence = "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence G", null);
		sequences.add(ss);
		
		stringSequence = "ACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCA";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence ACTGGTCA", null);
		sequences.add(ss);
		
		stringSequence = "ATCTGAGTCATGCGATCAGTGTTGGTCATGTCAGGTCAGTACTACGTAGCATGCATGCATACGATCGACTATATTGCATGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R1", null);
		sequences.add(ss);
		
		stringSequence = "AAAAAAACAAAAAAAGAAAAAAATTTTTTTGCATCAGATTTTTTTTCAGTACTGCATGACTACTGTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R2", null);
		sequences.add(ss);
		
		stringSequence = "TGCAGTACGTACGTGTTGAGTGCTATGCATGTTTAGGCGCGGCGCTAGCATGCATCAGACGCATACGTGTACGTACGTACTGATTCAGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R2", null);
		sequences.add(ss);
				
		removeIfExistFile(sequencePopulationTestFile);
		DNASequencesPopulator.writePopulation(sequences, sequencePopulationTestFile);
		
		List<Sequence> readSequences = DNASequencesPopulator.readPopulation(sequencePopulationTestFile);
		Iterator<Sequence> iterator = readSequences.iterator();
		assertEquals(iterator.next().seqString().toUpperCase(), "CATGACTGGCATCAGTGCATGCATGCAGTCAGTATATATGACGC");
		assertEquals(iterator.next().seqString().toUpperCase(), "ACATGCTCGATGTGTGTGTATCAGTACTGACCTAGCATGACTCAGTACACATGACGTCATCATGTAGCGTCTAGACTGACTACGTACGACTGCATACGACTATCAGACTGACTACGCATGACGTACGTGTACGTACTGATGACGTACTATCGTAGCATGACTACGTACGACTGAC");
		assertEquals(iterator.next().seqString().toUpperCase(), "ATGCTAGCATTCAGTACGTACGCATGATGCTAGATCGCATGACTAGCACGTACTGCATCGTGTGTGTCATGTGACTGAC");
		assertEquals(iterator.next().seqString().toUpperCase(), "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		assertEquals(iterator.next().seqString().toUpperCase(), "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
		assertEquals(iterator.next().seqString().toUpperCase(), "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
		assertEquals(iterator.next().seqString().toUpperCase(), "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
		assertEquals(iterator.next().seqString().toUpperCase(), "ACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCA");
		assertEquals(iterator.next().seqString().toUpperCase(), "ATCTGAGTCATGCGATCAGTGTTGGTCATGTCAGGTCAGTACTACGTAGCATGCATGCATACGATCGACTATATTGCATGAC");
		assertEquals(iterator.next().seqString().toUpperCase(), "AAAAAAACAAAAAAAGAAAAAAATTTTTTTGCATCAGATTTTTTTTCAGTACTGCATGACTACTGTGAC");
		assertEquals(iterator.next().seqString().toUpperCase(), "TGCAGTACGTACGTGTTGAGTGCTATGCATGTTTAGGCGCGGCGCTAGCATGCATCAGACGCATACGTGTACGTACGTACTGATTCAGACTGAC");
		assertFalse(iterator.hasNext());
		removeIfExistFile(sequencePopulationTestFile);
	}
		
	/**
	 * What is enough? May be 1k sequences is enough ? May be one million is huge, but 1k is enough for this test, I think...
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
	@Test
	public void testCreateSaveAndLoadRandomSequencePopulation() throws FileNotFoundException, IOException, ClassNotFoundException {
		int stepSize = 10;
		int maxSequences = 1000;
		int lengthFrom = 20;
		int lengthTo = 700;
		
		List<List<Sequence>> populations = new LinkedList<List<Sequence>>();
		for (int sequenceQuantity = stepSize; sequenceQuantity <= maxSequences; sequenceQuantity *= stepSize) {
			System.out.println("Creating population: " + sequenceQuantity + " sequences");
			String populationPath = sequencePopulationTestFile + "_" + sequenceQuantity;
			List<Sequence> populateSequences = DNASequencesPopulator.populateSequences(sequenceQuantity, lengthFrom, lengthTo);
			populations.add(populateSequences);
			removeIfExistFile(populationPath);
			DNASequencesPopulator.writePopulation(populateSequences, populationPath);
		}
		
		int listCreatedPos = 0;
		for (int sequenceQuantity = stepSize; sequenceQuantity <= maxSequences; sequenceQuantity *= stepSize) {
			String populationPath = sequencePopulationTestFile + "_" + sequenceQuantity;
			System.out.println("Testing population: " + sequenceQuantity + " sequences");
			Iterator<Sequence> storedPopulationIterator = DNASequencesPopulator.readPopulation(populationPath).iterator();
			Iterator<Sequence> createdPopulationIterator = populations.get(listCreatedPos).iterator();
			while (storedPopulationIterator.hasNext() && createdPopulationIterator.hasNext()) {
				Sequence nextStored = storedPopulationIterator.next();
				Sequence nextCreated = createdPopulationIterator.next();
				checkSequenceEquals(nextStored, nextCreated);
			}
			assertFalse(storedPopulationIterator.hasNext());
			assertFalse(createdPopulationIterator.hasNext());
			removeIfExistFile(populationPath);
			listCreatedPos++;
		}
	}
	
	private static void checkSequenceEquals(Sequence seq1, Sequence seq2) {
		assertEquals(seq1.getAlphabet(), seq2.getAlphabet());
		assertEquals(seq1.getName(), seq2.getName());
		assertEquals(seq1.length(), seq2.length());
		assertEquals(seq1.subList(1, seq1.length()), seq2.subList(1, seq2.length()));;
	}
	
	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws BioException
	 */
	@Test
	public static void testReadFormatedEqualsReadFasta() throws FileNotFoundException, IOException, ClassNotFoundException, BioException {
		List<Sequence> population = DNASequencesPopulator.readPopulation("data" + File.separator + "populator" + File.separator + "test_sequences_dataset_dna_500_200_700.seqs" );
		Iterator<Sequence> populationIterator = population.iterator();
		
		BufferedReader br = new BufferedReader(new FileReader("data" + File.separator + "populator" + File.separator + "test_sequences_dataset_dna_500_200_700.fasta"));
		LightweightStreamReader readFasta = LightweightIOTools.readFastaDNA(br, null);	
		
		Sequence nextFastaSequence;
		Sequence nextPopulationSequence;
		while(readFasta.hasNext()) {
			nextFastaSequence = readFasta.nextSequence();
			nextPopulationSequence = populationIterator.next();
			assertEquals(nextFastaSequence.length(), nextPopulationSequence.length());
			assertEquals(nextFastaSequence.seqString(), nextPopulationSequence.seqString());
						
			SymbolList fastaSubList = nextFastaSequence.subList(1, nextPopulationSequence.length());
			SymbolList populationSubList = nextPopulationSequence.subList(1, nextPopulationSequence.length());
			
			assertEquals(fastaSubList, populationSubList);
			assertEquals(nextFastaSequence.getName(), nextPopulationSequence.getName());
		}
		
		assertFalse(populationIterator.hasNext());
		assertFalse(readFasta.hasNext());
	}
}
