package bio.pih.tests.index;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.easymock.EasyMock;
import org.junit.Test;

import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.index.MemoryInvertedIndex;
import bio.pih.index.builder.InvertedIndexBuilder;
import bio.pih.io.SequenceDataBank;
import bio.pih.seq.LightweightSymbolList;

public class InvertedIndexBuilderTest extends TestCase {

	private static int SUB_SEQUENCE_LENGTH = 10;
	private static DNASequenceEncoderToInteger ENCODER = DNASequenceEncoderToInteger.getEncoder(SUB_SEQUENCE_LENGTH);

	private SequenceDataBank createSequenceDatabankMock(DNASequenceEncoderToInteger encoder) throws IOException {
		SequenceDataBank sequenceDataBank = EasyMock.createMock(SequenceDataBank.class);
		EasyMock.expect(sequenceDataBank.getAlphabet()).andReturn(DNATools.getDNA()).anyTimes();
		EasyMock.expect(sequenceDataBank.getEncoder()).andReturn(encoder).anyTimes();
		EasyMock.expect(sequenceDataBank.getFullPath()).andReturn(new File("/tmp", this.getClass().getName())).anyTimes();
		EasyMock.replay(sequenceDataBank);
		return sequenceDataBank;
	}

	@Test
	public void testBeginEnd() throws IllegalSymbolException, IOException {
		SequenceDataBank sequenceDataBank = createSequenceDatabankMock(ENCODER);

		MemoryInvertedIndex memoryInvertedIndex = new MemoryInvertedIndex(sequenceDataBank, SUB_SEQUENCE_LENGTH);
		InvertedIndexBuilder index = new InvertedIndexBuilder(memoryInvertedIndex);
		index.constructIndex();
		index.finishConstruction();
	}

	@Test
	public void testBeginInsertOneSmallSequenceEnd() throws IllegalSymbolException, IOException {
		SequenceDataBank sequenceDataBank = createSequenceDatabankMock(ENCODER);
		
		MemoryInvertedIndex memoryInvertedIndex = new MemoryInvertedIndex(sequenceDataBank, SUB_SEQUENCE_LENGTH);
		InvertedIndexBuilder index = new InvertedIndexBuilder(memoryInvertedIndex);
		index.constructIndex();

		SymbolList seq = LightweightSymbolList.createDNA("ACTGCCAGTAACTGCCAGTAACTGCCAGTAACTGCCAGTAACTGCCAGTA");
		int[] encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(0, encodedSequence, SUB_SEQUENCE_LENGTH);

		seq = LightweightSymbolList.createDNA("AAAAAAAAAAAAAAAAAAACAAAAAAAAAAAAAAAAAATAAAATAAAAAAAACCCCCCCCCC");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(1, encodedSequence, SUB_SEQUENCE_LENGTH);

		seq = LightweightSymbolList.createDNA("TTTTTTCTTTTTTTATTTTTTTTATTTTTTATTTTTCTTTTTTGTTCTTTTTTTTTTTTTT");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(2, encodedSequence, SUB_SEQUENCE_LENGTH);

		seq = LightweightSymbolList.createDNA("TACGCAGACTGACTGACTGCATGACTGATGCATACGTACTGACTGTAGTGTGACTGATGC");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(2, encodedSequence, SUB_SEQUENCE_LENGTH);

		seq = LightweightSymbolList.createDNA("ACTGCCAGTAACTGCCAGTAACTGCCAGTAACTGCCAGTAACTGCCAGTA");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(3, encodedSequence, SUB_SEQUENCE_LENGTH);

		seq = LightweightSymbolList.createDNA("AAAAAAAAAAAAAAAAAAACAAAAAAAAAAAAAAAAAATAAAATAAAAAAAACCCCCCCCCC");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(4, encodedSequence, SUB_SEQUENCE_LENGTH);

		seq = LightweightSymbolList.createDNA("TTTTTTCTTTTTTTATTTTTTTTATTTTTTATTTTTCTTTTTTGTTCTTTTTTTTTTTTTT");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(5, encodedSequence, SUB_SEQUENCE_LENGTH);

		seq = LightweightSymbolList.createDNA("TACGCAGACTGACTGACTGCATGACTGATGCATACGTACTGACTGTAGTGTGACTGATGCTTTTTTCTTT");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(6, encodedSequence, SUB_SEQUENCE_LENGTH);

		seq = LightweightSymbolList.createDNA("ACTGCCAGTAACTGCCAGTAACTGCCAGTAACTGCCAGTAACTGCCAGTATTTTTTGTTT");
		encodedSequence = ENCODER.encodeSymbolListToIntegerArray(seq);
		index.addSequence(7, encodedSequence, SUB_SEQUENCE_LENGTH);

		// index.getMatchingSubSequence("ACTGCCAGTA");

		index.setTotalSortMemory(200);
		index.finishConstruction();
	}

	// TODO too small sort size.
}
