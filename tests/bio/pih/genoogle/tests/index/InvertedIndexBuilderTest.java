/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.tests.index;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.easymock.classextension.EasyMock;
import org.junit.Test;

import bio.pih.genoogle.encoder.DNASequenceEncoderToInteger;
import bio.pih.genoogle.index.IndexConstructionException;
import bio.pih.genoogle.index.MemoryInvertedIndex;
import bio.pih.genoogle.index.builder.InvertedIndexBuilder;
import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.seq.LightweightSymbolList;

public class InvertedIndexBuilderTest extends TestCase {

	private static int SUB_SEQUENCE_LENGTH = 10;
	private static DNASequenceEncoderToInteger ENCODER = DNASequenceEncoderToInteger.getEncoder(SUB_SEQUENCE_LENGTH);

	private AbstractSequenceDataBank createSequenceDatabankMock(DNASequenceEncoderToInteger encoder) throws IOException, SecurityException, NoSuchMethodException {
		AbstractSequenceDataBank sequenceDataBank = EasyMock.createMock(AbstractSequenceDataBank.class);
		
		EasyMock.createMock(AbstractSequenceDataBank.class, new Method[] 
				 {AbstractSequenceDataBank.class.getMethod("getAlphabet"),
				  AbstractSequenceDataBank.class.getMethod("getEncoder"),
				  AbstractSequenceDataBank.class.getMethod("getFullPath")});
				 
				
		EasyMock.expect(sequenceDataBank.getLowComplexityFilter()).andReturn(-1).anyTimes();
		EasyMock.expect(sequenceDataBank.getAlphabet()).andReturn(DNATools.getDNA()).anyTimes();
		EasyMock.expect(sequenceDataBank.getEncoder()).andReturn(encoder).anyTimes();
		EasyMock.expect(sequenceDataBank.getFullPath()).andReturn(new File("/tmp", this.getClass().getName())).anyTimes();
		EasyMock.replay(sequenceDataBank);
		return sequenceDataBank;
	}

	@Test
	public void testBeginEnd() throws IllegalSymbolException, IOException, IndexConstructionException, SecurityException, NoSuchMethodException {
		AbstractSequenceDataBank sequenceDataBank = createSequenceDatabankMock(ENCODER);

		MemoryInvertedIndex memoryInvertedIndex = new MemoryInvertedIndex(sequenceDataBank, SUB_SEQUENCE_LENGTH);
		InvertedIndexBuilder index = new InvertedIndexBuilder(memoryInvertedIndex);
		index.constructIndex();
		index.finishConstruction();
	}

	@Test
	public void testBeginInsertOneSmallSequenceEnd() throws IllegalSymbolException, IOException, IndexConstructionException, SecurityException, NoSuchMethodException {
		AbstractSequenceDataBank sequenceDataBank = createSequenceDatabankMock(ENCODER);
		
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
