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
import bio.pih.genoogle.encoder.MaskEncoder;
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
import bio.pih.genoogle.seq.Sequence;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.util.SymbolListWindowIterator;
import bio.pih.genoogle.util.SymbolListWindowIteratorFactory;

/**
 * Tests for the {@link MemorySubSequencesInvertedIndex}
 * 
 * @author albrecht
 */
public class SubSequencesArrayIndexTest_11Masked extends TestCase {

	private static final String mask = "111010010100110111";
	private static final int SUB_SEQUENCE_LENGTH = 11;
	IndexedSequenceDataBank dataBank;
	SequenceEncoder encoder;

	@Override
	protected void setUp() throws Exception {
		this.dataBank = new IndexedSequenceDataBank("TestDB", DNAAlphabet.SINGLETON, SUB_SEQUENCE_LENGTH, mask, File.createTempFile(
				this.getName(), ".tmp"), null);		
		encoder = SequenceEncoderFactory.getEncoder(DNAAlphabet.SINGLETON, SUB_SEQUENCE_LENGTH);
	}

	@Override
	protected void tearDown() {
		this.dataBank = null;
	}

	private void populateNonSoRandomSequences(IndexedSequenceDataBank dataBank) throws IllegalSymbolException, IOException,
			IndexConstructionException {
		InvertedIndexBuilder indexBuilder = new InvertedIndexBuilder(dataBank);
		MaskEncoder maskEncoder = dataBank.getMaskEncoder();
		
		indexBuilder.constructIndex();
		String stringSequence = "CATGACTGGCATCAGTGCATGCATGCAGTCAGTATATATGACGC";
		Sequence ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 1");
		int[] filteredSequence = maskEncoder.applySequenceMask(ss);
		indexBuilder.addSequence(1, filteredSequence);

		stringSequence = "ACATGCTCGATGTGTGTGTATCAGTACTGACCTAGCATGACTCAGTACACATGACGTCATCATGTAGCGTCTAGACTGACTACGTACGACTGCATACGACTATCAGACTGACTACGCATGACGTACGTGTACGTACTGATGACGTACTATCGTAGCATGACTACGTACGACTGAC";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 1");
		filteredSequence = maskEncoder.applySequenceMask(ss);
		indexBuilder.addSequence(2, filteredSequence);

		stringSequence = "ATGCTAGCATTCAGTACGTACGCATGATGCTAGATCGCATGACTAGCACGTACTGCATCGTGTGTGTCATGTGACTGAC";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 2");
		filteredSequence = maskEncoder.applySequenceMask(ss);
		indexBuilder.addSequence(3, filteredSequence);

		stringSequence = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 3");
		filteredSequence = maskEncoder.applySequenceMask(ss);
		indexBuilder.addSequence(4, filteredSequence);

		stringSequence = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 4");
		filteredSequence = maskEncoder.applySequenceMask(ss);
		indexBuilder.addSequence(5, filteredSequence);

		stringSequence = "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 5");
		filteredSequence = maskEncoder.applySequenceMask(ss);
		indexBuilder.addSequence(6, filteredSequence);

		stringSequence = "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 6");
		filteredSequence = maskEncoder.applySequenceMask(ss);
		indexBuilder.addSequence(7, filteredSequence);

		stringSequence = "ACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCA";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 7");
		filteredSequence = maskEncoder.applySequenceMask(ss);
		indexBuilder.addSequence(8, filteredSequence);

		stringSequence = "ATCTGAGTCATGCGATCAGTGTTGGTCATGTCAGGTCAGTACTACGTAGCATGCATGCATACGATCGACTATATTGCATGAC";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 8");
		filteredSequence = maskEncoder.applySequenceMask(ss);
		indexBuilder.addSequence(9, filteredSequence);

		stringSequence = "AAAAAAACAAATCAGTGCATGCAAAAGAAAAAAATTTTTTTGCATCAGATTTTTTTTCAGTACTGCATGACTACTGTGAC";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 9");
		filteredSequence = maskEncoder.applySequenceMask(ss);
		indexBuilder.addSequence(10, filteredSequence);

		stringSequence = "TGCAGTACGTACGTGTTGAGTGCTATGCATGTTTAGGCGCGGCGCTAGCATGCATCAGACGCATACGTGTACGTACGTACTGATTCAGACTGAC";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 10");
		filteredSequence = maskEncoder.applySequenceMask(ss);
		indexBuilder.addSequence(11, filteredSequence);

		stringSequence = "ACGTAGCTTACTATTGATATGAGTCGTGACGACTGTATGCAAAAGAAAAAAATTCAGTGCATGCAAAAGAACTACGTACGTACGACTGACTACGTATCGTCAGCTGCGTCATGCATTACTGACTGACTGAGTCTGATCATGACTTGACTGACTGACTGGTACTACGTGTACTACGTGTACTACGTAGCTACGACGTACGTACTGGTACTGACTGACGTGTACGCTAGCATGCATCGATGACGTACGTGATCTACTGACTGTACTGACTGGTACGACTACGTACGACTGACTGACTGACTACGATGCTGACTGACGTTGACGTACTGAC";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "Sequence 11");
		filteredSequence = maskEncoder.applySequenceMask(ss);
		indexBuilder.addSequence(12, filteredSequence);
		
		stringSequence = "GGTTAATAAACGCAACGACAGTAATCCCCCGCTGCCATAGTGACAGACCGAGAGAAGCGAGCGGAGAAACCATAATATAATTTACCACTTACCTATTCATTTATCTACAGAAACAATGGACAACTCCGGCAAAGAAAAGGAGGCTATTCAGCTCATGGCTGAAGCCGACAAGAAAGTGAAGTCTTCCGGCTCTTTTTTAGGAGGAATGTTTGGAGGAAATCACAAAGTGGAGGAGGCTTGTGAGATGTACGCCAGAGCCGCCAACATGTTCAAAATGGCCAAGAACTGGAGTGCTGCAGGCAATGCTTTCTGTCAGGCAGCCAGAATTCATATGCAGCTTCAGAATAAACACGATTCTGCCACCAGCTACGTTGATGCTGGAAACGCCTTCAAGAAAGCAGATCCCAAGAGGCTATCAAGTGCTTAAACGCAGCAATTGATATTTACACAGACATGGTAAGATGTTTTTGTAGCTGTCAAAATCATATAATGTTGAGCCAGGCTGTTCTATTCCTGTACTGTGTTTGATCTGTGAACATTTTAAACGGCTACACA";
		ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "NM_001045156.1");
		filteredSequence = maskEncoder.applySequenceMask(ss);
		indexBuilder.addSequence(13, filteredSequence);
				
		indexBuilder.finishConstruction();
		MemoryInvertedIndex index = dataBank.getIndex();		
		index.loadFromFile();
	}

	public void testIfFindSubSequences() throws IllegalSymbolException, ValueOutOfBoundsException, IOException,
			IndexConstructionException {
		populateNonSoRandomSequences(dataBank);
		MaskEncoder maskEncoder = dataBank.getMaskEncoder();
		
		MemoryInvertedIndex index = dataBank.getIndex();

		int query = maskEncoder.applyMask("AAAAAAAAAAAAAAAAA");
		
		long[] matchingSubSequence = index.getMatchingSubSequence(query);

		assertEquals(3, matchingSubSequence.length);
		
		assertEquals(4, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[0]));
		assertEquals(0, SubSequenceIndexInfo.getStart(matchingSubSequence[0]));

		assertEquals(4, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[1]));
		assertEquals(18, SubSequenceIndexInfo.getStart(matchingSubSequence[1]));

		assertEquals(4, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[2]));
		assertEquals(36, SubSequenceIndexInfo.getStart(matchingSubSequence[2]));

		
		query = maskEncoder.applyMask("ATGCAAAAGAAAAAAATT");
		matchingSubSequence = index.getMatchingSubSequence(query);
		assertEquals(2, matchingSubSequence.length);

		assertEquals(10, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[0]));
		assertEquals(18, SubSequenceIndexInfo.getStart(matchingSubSequence[0]));

		assertEquals(12, SubSequenceIndexInfo.getSequenceId(matchingSubSequence[1]));
		assertEquals(36, SubSequenceIndexInfo.getStart(matchingSubSequence[1]));
		
		
		String stringSequence = "GGTTAATAAACGCAACGACAGTAATCCCCCGCTGCCATAGTGACAGACCGAGAGAAGCGAGCGGAGAAACCATAATATAATTTACCACTTACCTATTCATTTATCTACAGAAACAATGGACAACTCCGGCAAAGAAAAGGAGGCTATTCAGCTCATGGCTGAAGCCGACAAGAAAGTGAAGTCTTCCGGCTCTTTTTTAGGAGGAATGTTTGGAGGAAATCACAAAGTGGAGGAGGCTTGTGAGATGTACGCCAGAGCCGCCAACATGTTCAAAATGGCCAAGAACTGGAGTGCTGCAGGCAATGCTTTCTGTCAGGCAGCCAGAATTCATATGCAGCTTCAGAATAAACACGATTCTGCCACCAGCTACGTTGATGCTGGAAACGCCTTCAAGAAAGCAGATCCCAAGAGGCTATCAAGTGCTTAAACGCAGCAATTGATATTTACACAGACATGGTAAGATGTTTTTGTAGCTGTCAAAATCATATAATGTTGAGCCAGGCTGTTCTATTCCTGTACTGTGTTTGATCTGTGAACATTTTAAACGGCTACACA";
		Sequence ss = new Sequence(DNAAlphabet.SINGLETON, stringSequence, "NM_001045156.1");
		SymbolListWindowIterator iterator = SymbolListWindowIteratorFactory.getNotOverlappedFactory().newSymbolListWindowIterator(ss, mask.length());
	
		int pos = 0;
		while (iterator.hasNext()) {
			SymbolList symbolList = iterator.next();
			int encodedSubSequence = maskEncoder.applyMask(symbolList);
			matchingSubSequence = index.getMatchingSubSequence(encodedSubSequence);		
			assertTrue(matchingSubSequence.length > 0);
			int sequenceId = SubSequenceIndexInfo.getSequenceId(matchingSubSequence[0]);
			int start = SubSequenceIndexInfo.getStart(matchingSubSequence[0]);
			assertEquals(pos, start);
			assertEquals(13, sequenceId);
			pos += dataBank.getSubSequencesOffset();
		}
				
	}
}