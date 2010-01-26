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

import org.junit.Test;

import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.encoder.SequenceEncoderFactory;
import bio.pih.genoogle.index.IndexConstructionException;
import bio.pih.genoogle.index.builder.InvertedIndexBuilder;
import bio.pih.genoogle.io.IndexedSequenceDataBank;
import bio.pih.genoogle.seq.DNAAlphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.seq.SymbolList;

public class InvertedIndexBuilderTest extends TestCase {

	private static int SUB_SEQUENCE_LENGTH = 10;
	private static SequenceEncoder ENCODER = SequenceEncoderFactory.getEncoder(DNAAlphabet.SINGLETON,
			SUB_SEQUENCE_LENGTH);

	private IndexedSequenceDataBank createSequenceDatabankMock(SequenceEncoder encoder) throws IOException,
			SecurityException, NoSuchMethodException {
		return new IndexedSequenceDataBank("TestDB", DNAAlphabet.SINGLETON, SUB_SEQUENCE_LENGTH, "1111111111", File.createTempFile(
				this.getName(), ".tmp"), null, -1);
	}

	@Test
	public void testBeginEnd() throws IllegalSymbolException, IOException, IndexConstructionException,
			SecurityException, NoSuchMethodException {
		IndexedSequenceDataBank sequenceDataBank = createSequenceDatabankMock(ENCODER);

		InvertedIndexBuilder index = new InvertedIndexBuilder(sequenceDataBank);
		index.constructIndex();
		index.finishConstruction();
	}

	@Test
	public void testBeginInsertOneSmallSequenceEnd() throws IllegalSymbolException, IOException,
			IndexConstructionException, SecurityException, NoSuchMethodException {
		IndexedSequenceDataBank sequenceDataBank = createSequenceDatabankMock(ENCODER);

		InvertedIndexBuilder index = new InvertedIndexBuilder(sequenceDataBank);
		index.constructIndex();

		SymbolList seq = LightweightSymbolList.createDNA("ACTGCCAGTAACTGCCAGTAACTGCCAGTAACTGCCAGTAACTGCCAGTA");
		int[] encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(0, encodedSequence);

		seq = LightweightSymbolList.createDNA("AAAAAAAAAAAAAAAAAAACAAAAAAAAAAAAAAAAAATAAAATAAAAAAAACCCCCCCCCC");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(1, encodedSequence);

		seq = LightweightSymbolList.createDNA("TTTTTTCTTTTTTTATTTTTTTTATTTTTTATTTTTCTTTTTTGTTCTTTTTTTTTTTTTT");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(2, encodedSequence);

		seq = LightweightSymbolList.createDNA("TACGCAGACTGACTGACTGCATGACTGATGCATACGTACTGACTGTAGTGTGACTGATGC");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(2, encodedSequence);

		seq = LightweightSymbolList.createDNA("ACTGCCAGTAACTGCCAGTAACTGCCAGTAACTGCCAGTAACTGCCAGTA");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(3, encodedSequence);

		seq = LightweightSymbolList.createDNA("AAAAAAAAAAAAAAAAAAACAAAAAAAAAAAAAAAAAATAAAATAAAAAAAACCCCCCCCCC");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(4, encodedSequence);

		seq = LightweightSymbolList.createDNA("TTTTTTCTTTTTTTATTTTTTTTATTTTTTATTTTTCTTTTTTGTTCTTTTTTTTTTTTTT");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(5, encodedSequence);

		seq = LightweightSymbolList.createDNA("TACGCAGACTGACTGACTGCATGACTGATGCATACGTACTGACTGTAGTGTGACTGATGCTTTTTTCTTT");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(6, encodedSequence);

		seq = LightweightSymbolList.createDNA("ACTGCCAGTAACTGCCAGTAACTGCCAGTAACTGCCAGTAACTGCCAGTATTTTTTGTTT");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(7, encodedSequence);

		seq = LightweightSymbolList.createDNA("GGTTAATAAACGCAACGACAGTAATCCCCCGCTGCCATAGTGACAGACCGAGAGAAGCGAGCGGAGAAACCATAATATAATTTACCACTTACCTATTCATTTATCTACAGAAACAATGGACAACTCCGGCAAAGAAAAGGAGGCTATTCAGCTCATGGCTGAAGCCGACAAGAAAGTGAAGTCTTCCGGCTCTTTTTTAGGAGGAATGTTTGGAGGAAATCACAAAGTGGAGGAGGCTTGTGAGATGTACGCCAGAGCCGCCAACATGTTCAAAATGGCCAAGAACTGGAGTGCTGCAGGCAATGCTTTCTGTCAGGCAGCCAGAATTCATATGCAGCTTCAGAATAAACACGATTCTGCCACCAGCTACGTTGATGCTGGAAACGCCTTCAAGAAAGCAGATCCCAAGAGGCTATCAAGTGCTTAAACGCAGCAATTGATATTTACACAGACATGGTAAGATGTTTTTGTAGCTGTCAAAATCATATAATGTTGAGCCAGGCTGTTCTATTCCTGTACTGTGTTTGATCTGTGAACATTTTAAACGGCTACACA");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(8, encodedSequence);

		// index.getMatchingSubSequence("ACTGCCAGTA");

		index.setTotalSortMemory(240);
		index.finishConstruction();
	}

	// TODO too small sort size.
}
