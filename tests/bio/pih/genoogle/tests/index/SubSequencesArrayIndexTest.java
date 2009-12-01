/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.tests.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.junit.Test;

import bio.pih.genoogle.encoder.DNASequenceEncoderToInteger;
import bio.pih.genoogle.index.IndexConstructionException;
import bio.pih.genoogle.index.MemoryInvertedIndex;
import bio.pih.genoogle.index.SubSequenceIndexInfo;
import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.index.builder.InvertedIndexBuilder;
import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.io.IndexedDNASequenceDataBank;
import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.seq.generator.DNASequencesPopulator;

/**
 * Tests for the {@link MemorySubSequencesInvertedIndex}
 * 
 * @author albrecht
 */
public class SubSequencesArrayIndexTest extends TestCase {

	private static final int SUB_SEQUENCE_LENGTH = 8;
	MemoryInvertedIndex index;
	DNASequenceEncoderToInteger encoder;
	
	@Override
	protected void setUp() throws Exception {
		AbstractSequenceDataBank dataBank = new IndexedDNASequenceDataBank("TestDB", 8, "11111111", File.createTempFile(this.getName(), ".tmp"), null, -1);
		index = new MemoryInvertedIndex (dataBank, SUB_SEQUENCE_LENGTH);
		encoder = DNASequenceEncoderToInteger.getEncoder(8);
	}
	
	@Override
	protected void tearDown() {
		index = null;
	}

	private void populateNonSoRandomSequences(MemoryInvertedIndex  index) throws IllegalSymbolException, IOException, IndexConstructionException {
		InvertedIndexBuilder indexBuilder = new InvertedIndexBuilder(index);
		
		indexBuilder.constructIndex();
		String stringSequence = "CATGACTGGCATCAGTGCATGCATGCAGTCAGTATATATGACGC";
		SymbolList symbolList = LightweightSymbolList.createDNA(stringSequence);
		SimpleSequence ss = new SimpleSequence(symbolList, null, "Sequence 1", null);
		indexBuilder.addSequence(1, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);
		
		stringSequence = "ACATGCTCGATGTGTGTGTATCAGTACTGACCTAGCATGACTCAGTACACATGACGTCATCATGTAGCGTCTAGACTGACTACGTACGACTGCATACGACTATCAGACTGACTACGCATGACGTACGTGTACGTACTGATGACGTACTATCGTAGCATGACTACGTACGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence 2", null);
		indexBuilder.addSequence(2, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);
		
		stringSequence = "ATGCTAGCATTCAGTACGTACGCATGATGCTAGATCGCATGACTAGCACGTACTGCATCGTGTGTGTCATGTGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence 3", null);
		indexBuilder.addSequence(3, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);
		
		stringSequence = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence A", null);
		indexBuilder.addSequence(4, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);
		
		stringSequence = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence T", null);
		indexBuilder.addSequence(5, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);
		
		stringSequence = "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence C", null);
		indexBuilder.addSequence(6, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);
		
		stringSequence = "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence G", null);
		indexBuilder.addSequence(7, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);
		
		stringSequence = "ACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCA";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence ACTGGTCA", null);
		indexBuilder.addSequence(8, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);
		
		stringSequence = "ATCTGAGTCATGCGATCAGTGTTGGTCATGTCAGGTCAGTACTACGTAGCATGCATGCATACGATCGACTATATTGCATGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R1", null);
		indexBuilder.addSequence(9, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);
		
		stringSequence = "AAAAAAACAAAAAAAGAAAAAAATTTTTTTGCATCAGATTTTTTTTCAGTACTGCATGACTACTGTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R2", null);
		indexBuilder.addSequence(10, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);
		
		stringSequence = "TGCAGTACGTACGTGTTGAGTGCTATGCATGTTTAGGCGCGGCGCTAGCATGCATCAGACGCATACGTGTACGTACGTACTGATTCAGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R3", null);
		indexBuilder.addSequence(11, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);
		
		stringSequence = "ACGTAGCTTACTATTGATATGAGTCGTGACGACTGACTACGTACGTACGACTGACTACGTATCGTCAGCTGCGTCATGCATTACTGACTGACTGAGTCTGATCATGACTTGACTGACTGACTGGTACTACGTGTACTACGTGTACTACGTAGCTACGACGTACGTACTGGTACTGACTGACGTGTACGCTAGCATGCATCGATGACGTACGTGATCTACTGACTGTACTGACTGGTACGACTACGTACGACTGACTGACTGACTACGATGCTGACTGACGTTGACGTACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R4", null);
		indexBuilder.addSequence(12, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);
		indexBuilder.finishConstruction();
		index.loadFromFile();
	}
	
	/**
	 * @throws IllegalSymbolException
	 * @throws BioException
	 * @throws ValueOutOfBoundsException
	 * @throws IOException 
	 * @throws IndexConstructionException 
	 */
	//@Test
	public void testIfFindSubSequences() throws IllegalSymbolException, BioException, ValueOutOfBoundsException, IOException, IndexConstructionException {
		populateNonSoRandomSequences(index);
		
		long[] matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("AAAAAAAA"));
		
		assertEquals(8, matchingSubSequence.length);
		assertEquals(4, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[0]));
		assertEquals(0, SubSequenceIndexInfo.getStart(matchingSubSequence[0]));
		
		assertEquals(4, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[1]));
		assertEquals(8, SubSequenceIndexInfo.getStart(matchingSubSequence[1]));
		
		assertEquals(4, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[2]));
		assertEquals(16, SubSequenceIndexInfo.getStart(matchingSubSequence[2]));
		
		assertEquals(4, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[3]));
		assertEquals(24, SubSequenceIndexInfo.getStart(matchingSubSequence[3]));
		
		assertEquals(4, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[4]));
		assertEquals(32, SubSequenceIndexInfo.getStart(matchingSubSequence[4]));
		
		assertEquals(4, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[5]));
		assertEquals(40, SubSequenceIndexInfo.getStart(matchingSubSequence[5]));
		
		assertEquals(4, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[6]));
		assertEquals(48, SubSequenceIndexInfo.getStart(matchingSubSequence[6]));
		
		matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("GCATGCAT"));
		assertEquals(2, matchingSubSequence.length);
		
		assertEquals(1, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[0]));
		assertEquals(16, SubSequenceIndexInfo.getStart(matchingSubSequence[0]));
		
		assertEquals(9, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[1]));
		assertEquals(48, SubSequenceIndexInfo.getStart(matchingSubSequence[1]));
	}
	
	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ValueOutOfBoundsException 
	 * @throws BioException 
	 * @throws IllegalSymbolException 
	 * @throws IndexConstructionException 
	 */
	@Test
	public void test_DNA_1000_200_700_sequences() throws FileNotFoundException, IOException, ClassNotFoundException, IllegalSymbolException, BioException, ValueOutOfBoundsException, IndexConstructionException {
		
		List<Sequence> population = DNASequencesPopulator.readPopulation("data" + File.separator + "populator" + File.separator + "test_sequences_dataset_dna_500_200_700.seqs" );
				
		InvertedIndexBuilder indexBuilder = new InvertedIndexBuilder(index);
		
		int code = 0;
		indexBuilder.constructIndex();
		for(Sequence sequence: population) {
			indexBuilder.addSequence(code, encoder.encodeSymbolListToIntegerArray(sequence), SUB_SEQUENCE_LENGTH);
			code++;
		}
		indexBuilder.finishConstruction();
		index.loadFromFile();
		
		long[] matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("TCTTGCCC"));
		assertEquals(2, matchingSubSequence.length);
		assertEquals(132, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[0]));
		assertEquals(152, SubSequenceIndexInfo.getStart(matchingSubSequence[0]));
		
		assertEquals(483, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[1]));
		assertEquals(224, SubSequenceIndexInfo.getStart(matchingSubSequence[1]));
				
		matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("GAGAATAC"));
		assertEquals(1, matchingSubSequence.length);
		assertEquals(0, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[0]));
		assertEquals(0, SubSequenceIndexInfo.getStart(matchingSubSequence[0]));
		
		matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("TCTTGCCG"));
		assertEquals(0, matchingSubSequence.length);	
	}
	
}
