/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.tests.index;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.encoder.SequenceEncoderFactory;
import bio.pih.genoogle.index.IndexConstructionException;
import bio.pih.genoogle.index.MemoryInvertedIndex;
import bio.pih.genoogle.index.SubSequenceIndexInfo;
import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.index.builder.InvertedIndexBuilder;
import bio.pih.genoogle.io.IndexedSequenceDataBank;
import bio.pih.genoogle.seq.DNAAlphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.seq.Sequence;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.util.SymbolListWindowIterator;
import bio.pih.genoogle.util.SymbolListWindowIteratorFactory;

/**
 * Tests for the {@link MemorySubSequencesInvertedIndex}
 * 
 * @author albrecht
 */
public class SubSequencesArrayIndexTest_8 extends TestCase {

	private static final int SUB_SEQUENCE_LENGTH = 8;
	IndexedSequenceDataBank dataBank;
	SequenceEncoder encoder;

	@Override
	protected void setUp() throws Exception {
		this.dataBank = new IndexedSequenceDataBank("TestDB", DNAAlphabet.SINGLETON, SUB_SEQUENCE_LENGTH, "11111111", File.createTempFile(
				this.getName(), ".tmp"), null);
		encoder = SequenceEncoderFactory.getEncoder(DNAAlphabet.SINGLETON, 8);
	}

	@Override
	protected void tearDown() {
		this.dataBank = null;
	}

	private void populateNonSoRandomSequences(IndexedSequenceDataBank dataBank) throws IllegalSymbolException,
			IOException, IndexConstructionException {
		InvertedIndexBuilder indexBuilder = new InvertedIndexBuilder(dataBank);

		indexBuilder.constructIndex();
		String stringSequence = "CATGACTGGCATCAGTGCATGCATGCAGTCAGTATATATGACGC";
		Sequence ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 1");
		indexBuilder.addSequence(1, encoder.encodeSymbolListToIntegerArray(ss));

		stringSequence = "ACATGCTCGATGTGTGTGTATCAGTACTGACCTAGCATGACTCAGTACACATGACGTCATCATGTAGCGTCTAGACTGACTACGTACGACTGCATACGACTATCAGACTGACTACGCATGACGTACGTGTACGTACTGATGACGTACTATCGTAGCATGACTACGTACGACTGAC";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 1");
		indexBuilder.addSequence(2, encoder.encodeSymbolListToIntegerArray(ss));

		stringSequence = "ATGCTAGCATTCAGTACGTACGCATGATGCTAGATCGCATGACTAGCACGTACTGCATCGTGTGTGTCATGTGACTGAC";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 2");
		indexBuilder.addSequence(3, encoder.encodeSymbolListToIntegerArray(ss));

		stringSequence = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 3");
		indexBuilder.addSequence(4, encoder.encodeSymbolListToIntegerArray(ss));

		stringSequence = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 4");
		indexBuilder.addSequence(5, encoder.encodeSymbolListToIntegerArray(ss));

		stringSequence = "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 5");
		indexBuilder.addSequence(6, encoder.encodeSymbolListToIntegerArray(ss));

		stringSequence = "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 6");
		indexBuilder.addSequence(7, encoder.encodeSymbolListToIntegerArray(ss));

		stringSequence = "ACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCA";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 7");
		indexBuilder.addSequence(8, encoder.encodeSymbolListToIntegerArray(ss));

		stringSequence = "ATCTGAGTCATGCGATCAGTGTTGGTCATGTCAGGTCAGTACTACGTAGCATGCATGCATACGATCGACTATATTGCATGAC";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 8");
		indexBuilder.addSequence(9, encoder.encodeSymbolListToIntegerArray(ss));

		stringSequence = "AAAAAAACAAAAAAAGAAAAAAATTTTTTTGCATCAGATTTTTTTTCAGTACTGCATGACTACTGTGAC";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 9");
		indexBuilder.addSequence(10, encoder.encodeSymbolListToIntegerArray(ss));

		stringSequence = "TGCAGTACGTACGTGTTGAGTGCTATGCATGTTTAGGCGCGGCGCTAGCATGCATCAGACGCATACGTGTACGTACGTACTGATTCAGACTGAC";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 10");
		indexBuilder.addSequence(11, encoder.encodeSymbolListToIntegerArray(ss));

		stringSequence = "ACGTAGCTTACTATTGATATGAGTCGTGACGACTGACTACGTACGTACGACTGACTACGTATCGTCAGCTGCGTCATGCATTACTGACTGACTGAGTCTGATCATGACTTGACTGACTGACTGGTACTACGTGTACTACGTGTACTACGTAGCTACGACGTACGTACTGGTACTGACTGACGTGTACGCTAGCATGCATCGATGACGTACGTGATCTACTGACTGTACTGACTGGTACGACTACGTACGACTGACTGACTGACTACGATGCTGACTGACGTTGACGTACTGAC";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 11");
		indexBuilder.addSequence(12, encoder.encodeSymbolListToIntegerArray(ss));

		stringSequence = "GGTTAATAAACGCAACGACAGTAATCCCCCGCTGCCATAGTGACAGACCGAGAGAAGCGAGCGGAGAAACCATAATATAATTTACCACTTACCTATTCATTTATCTACAGAAACAATGGACAACTCCGGCAAAGAAAAGGAGGCTATTCAGCTCATGGCTGAAGCCGACAAGAAAGTGAAGTCTTCCGGCTCTTTTTTAGGAGGAATGTTTGGAGGAAATCACAAAGTGGAGGAGGCTTGTGAGATGTACGCCAGAGCCGCCAACATGTTCAAAATGGCCAAGAACTGGAGTGCTGCAGGCAATGCTTTCTGTCAGGCAGCCAGAATTCATATGCAGCTTCAGAATAAACACGATTCTGCCACCAGCTACGTTGATGCTGGAAACGCCTTCAAGAAAGCAGATCCCAAGAGGCTATCAAGTGCTTAAACGCAGCAATTGATATTTACACAGACATGGTAAGATGTTTTTGTAGCTGTCAAAATCATATAATGTTGAGCCAGGCTGTTCTATTCCTGTACTGTGTTTGATCTGTGAACATTTTAAACGGCTACACA";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "NM_001045156.1");
		indexBuilder.addSequence(13, encoder.encodeSymbolListToIntegerArray(ss));

		indexBuilder.finishConstruction();
		MemoryInvertedIndex index = dataBank.getIndex();
		index.loadFromFile();
	}

	public void testIfFindSubSequences() throws IllegalSymbolException, ValueOutOfBoundsException, IOException,
			IndexConstructionException {
		populateNonSoRandomSequences(dataBank);

		MemoryInvertedIndex index = dataBank.getIndex();

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

		String stringSequence = "GGTTAATAAACGCAACGACAGTAATCCCCCGCTGCCATAGTGACAGACCGAGAGAAGCGAGCGGAGAAACCATAATATAATTTACCACTTACCTATTCATTTATCTACAGAAACAATGGACAACTCCGGCAAAGAAAAGGAGGCTATTCAGCTCATGGCTGAAGCCGACAAGAAAGTGAAGTCTTCCGGCTCTTTTTTAGGAGGAATGTTTGGAGGAAATCACAAAGTGGAGGAGGCTTGTGAGATGTACGCCAGAGCCGCCAACATGTTCAAAATGGCCAAGAACTGGAGTGCTGCAGGCAATGCTTTCTGTCAGGCAGCCAGAATTCATATGCAGCTTCAGAATAAACACGATTCTGCCACCAGCTACGTTGATGCTGGAAACGCCTTCAAGAAAGCAGATCCCAAGAGGCTATCAAGTGCTTAAACGCAGCAATTGATATTTACACAGACATGGTAAGATGTTTTTGTAGCTGTCAAAATCATATAATGTTGAGCCAGGCTGTTCTATTCCTGTACTGTGTTTGATCTGTGAACATTTTAAACGGCTACACA";
		Sequence ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "NM_001045156.1");
		SymbolListWindowIterator iterator = SymbolListWindowIteratorFactory.getNotOverlappedFactory().newSymbolListWindowIterator(
				ss, 8);

		int pos = 0;
		while (iterator.hasNext()) {
			SymbolList symbolList = iterator.next();
			int encodedSubSequence = encoder.encodeSubSequenceToInteger(symbolList);
			matchingSubSequence = index.getMatchingSubSequence(encodedSubSequence);
			assertTrue(matchingSubSequence.length > 0);
			int sequenceId = SubSequenceIndexInfo.getSequenceId(matchingSubSequence[0]);
			int start = SubSequenceIndexInfo.getStart(matchingSubSequence[0]);
			assertEquals(pos, start);
			assertEquals(13, sequenceId);
			pos += SUB_SEQUENCE_LENGTH;
		}

	}
}
