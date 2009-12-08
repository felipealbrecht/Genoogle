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

import org.junit.Test;

import bio.pih.genoogle.encoder.DNASequenceEncoderToInteger;
import bio.pih.genoogle.index.IndexConstructionException;
import bio.pih.genoogle.index.MemoryInvertedIndex;
import bio.pih.genoogle.index.SubSequenceIndexInfo;
import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.index.builder.InvertedIndexBuilder;
import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.io.IndexedDNASequenceDataBank;
import bio.pih.genoogle.seq.DNAAlphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.seq.Sequence;
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
		AbstractSequenceDataBank dataBank = new IndexedDNASequenceDataBank("TestDB", 8, "11111111", File.createTempFile(
				this.getName(), ".tmp"), null, -1);
		index = new MemoryInvertedIndex(dataBank, SUB_SEQUENCE_LENGTH);
		encoder = DNASequenceEncoderToInteger.getEncoder(8);
	}

	@Override
	protected void tearDown() {
		index = null;
	}

	private void populateNonSoRandomSequences(MemoryInvertedIndex index) throws IllegalSymbolException, IOException,
			IndexConstructionException {
		InvertedIndexBuilder indexBuilder = new InvertedIndexBuilder(index);

		indexBuilder.constructIndex();
		String stringSequence = "CATGACTGGCATCAGTGCATGCATGCAGTCAGTATATATGACGC";
		Sequence ss = new Sequence("Sequence_1", DNAAlphabet.SINGLETON, stringSequence);
		indexBuilder.addSequence(1, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);

		stringSequence = "ACATGCTCGATGTGTGTGTATCAGTACTGACCTAGCATGACTCAGTACACATGACGTCATCATGTAGCGTCTAGACTGACTACGTACGACTGCATACGACTATCAGACTGACTACGCATGACGTACGTGTACGTACTGATGACGTACTATCGTAGCATGACTACGTACGACTGAC";
		ss = new Sequence("Sequence_1", DNAAlphabet.SINGLETON, stringSequence);
		indexBuilder.addSequence(2, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);

		stringSequence = "ATGCTAGCATTCAGTACGTACGCATGATGCTAGATCGCATGACTAGCACGTACTGCATCGTGTGTGTCATGTGACTGAC";
		ss = new Sequence("Sequence_1", DNAAlphabet.SINGLETON, stringSequence);
		indexBuilder.addSequence(3, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);

		stringSequence = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		ss = new Sequence("Sequence_1", DNAAlphabet.SINGLETON, stringSequence);
		indexBuilder.addSequence(4, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);

		stringSequence = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT";
		ss = new Sequence("Sequence_1", DNAAlphabet.SINGLETON, stringSequence);
		indexBuilder.addSequence(5, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);

		stringSequence = "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
		ss = new Sequence("Sequence_1", DNAAlphabet.SINGLETON, stringSequence);
		indexBuilder.addSequence(6, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);

		stringSequence = "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG";
		ss = new Sequence("Sequence_1", DNAAlphabet.SINGLETON, stringSequence);
		indexBuilder.addSequence(7, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);

		stringSequence = "ACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCA";
		ss = new Sequence("Sequence_1", DNAAlphabet.SINGLETON, stringSequence);
		indexBuilder.addSequence(8, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);

		stringSequence = "ATCTGAGTCATGCGATCAGTGTTGGTCATGTCAGGTCAGTACTACGTAGCATGCATGCATACGATCGACTATATTGCATGAC";
		ss = new Sequence("Sequence_1", DNAAlphabet.SINGLETON, stringSequence);
		indexBuilder.addSequence(9, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);

		stringSequence = "AAAAAAACAAAAAAAGAAAAAAATTTTTTTGCATCAGATTTTTTTTCAGTACTGCATGACTACTGTGAC";
		ss = new Sequence("Sequence_1", DNAAlphabet.SINGLETON, stringSequence);
		indexBuilder.addSequence(10, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);

		stringSequence = "TGCAGTACGTACGTGTTGAGTGCTATGCATGTTTAGGCGCGGCGCTAGCATGCATCAGACGCATACGTGTACGTACGTACTGATTCAGACTGAC";
		ss = new Sequence("Sequence_1", DNAAlphabet.SINGLETON, stringSequence);
		indexBuilder.addSequence(11, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);

		stringSequence = "ACGTAGCTTACTATTGATATGAGTCGTGACGACTGACTACGTACGTACGACTGACTACGTATCGTCAGCTGCGTCATGCATTACTGACTGACTGAGTCTGATCATGACTTGACTGACTGACTGGTACTACGTGTACTACGTGTACTACGTAGCTACGACGTACGTACTGGTACTGACTGACGTGTACGCTAGCATGCATCGATGACGTACGTGATCTACTGACTGTACTGACTGGTACGACTACGTACGACTGACTGACTGACTACGATGCTGACTGACGTTGACGTACTGAC";
		ss = new Sequence("Sequence_1", DNAAlphabet.SINGLETON, stringSequence);
		indexBuilder.addSequence(12, encoder.encodeSymbolListToIntegerArray(ss), SUB_SEQUENCE_LENGTH);
		indexBuilder.finishConstruction();
		index.loadFromFile();
	}

	public void testIfFindSubSequences() throws IllegalSymbolException, ValueOutOfBoundsException, IOException,
			IndexConstructionException {
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

	@Test
	public void test_DNA_1000_200_700_sequences() throws FileNotFoundException, IOException, ClassNotFoundException,
			IllegalSymbolException, ValueOutOfBoundsException, IndexConstructionException {

		List<Sequence> population = DNASequencesPopulator.readPopulation("data" + File.separator + "populator"
				+ File.separator + "test_sequences_dataset_dna_500_200_700.seqs");

		InvertedIndexBuilder indexBuilder = new InvertedIndexBuilder(index);

		int code = 0;
		indexBuilder.constructIndex();
		for (Sequence sequence : population) {
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
