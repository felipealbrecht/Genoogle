package bio.pih.tests.index;

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

import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.index.EncoderSubSequenceIndexInfo;
import bio.pih.index.MemorySubSequencesInvertedIndexInteger;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.SequenceDataBank;
import bio.pih.io.IndexedSequenceDataBank.StorageKind;
import bio.pih.seq.LightweightSymbolList;
import bio.pih.seq.generator.DNASequencesPopulator;

/**
 * Tests for the {@link MemorySubSequencesInvertedIndex}
 * 
 * @author albrecht
 */
public class SubSequencesArrayIndexTest extends TestCase {

	private static final int SUB_SEQUENCE_LENGTH = 8;
	private static final int SEQUENCE_OFFSET = SUB_SEQUENCE_LENGTH;
	MemorySubSequencesInvertedIndexInteger index;
	DNASequenceEncoderToInteger encoder;
	
	@Override
	protected void setUp() throws Exception {
		SequenceDataBank dataBank = new IndexedDNASequenceDataBank("dummy", null, null, StorageKind.MEMORY, 8, "11111111");
		index = new MemorySubSequencesInvertedIndexInteger (dataBank, SUB_SEQUENCE_LENGTH);
		encoder = DNASequenceEncoderToInteger.getEncoder(8);
	}
	
	@Override
	protected void tearDown() {
		index = null;
	}

	private void populateNonSoRandomSequences(MemorySubSequencesInvertedIndexInteger  index) throws IllegalSymbolException, IOException {
		index.constructIndex();
		String stringSequence = "CATGACTGGCATCAGTGCATGCATGCAGTCAGTATATATGACGC";
		SymbolList symbolList = LightweightSymbolList.createDNA(stringSequence);
		SimpleSequence ss = new SimpleSequence(symbolList, null, "Sequence 1", null);
		index.addSequence(1, encoder.encodeSymbolListToIntegerArray(ss), SEQUENCE_OFFSET);
		
		stringSequence = "ACATGCTCGATGTGTGTGTATCAGTACTGACCTAGCATGACTCAGTACACATGACGTCATCATGTAGCGTCTAGACTGACTACGTACGACTGCATACGACTATCAGACTGACTACGCATGACGTACGTGTACGTACTGATGACGTACTATCGTAGCATGACTACGTACGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence 2", null);
		index.addSequence(2, encoder.encodeSymbolListToIntegerArray(ss), SEQUENCE_OFFSET);
		
		stringSequence = "ATGCTAGCATTCAGTACGTACGCATGATGCTAGATCGCATGACTAGCACGTACTGCATCGTGTGTGTCATGTGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence 3", null);
		index.addSequence(3, encoder.encodeSymbolListToIntegerArray(ss), SEQUENCE_OFFSET);
		
		stringSequence = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence A", null);
		index.addSequence(4, encoder.encodeSymbolListToIntegerArray(ss), SEQUENCE_OFFSET);
		
		stringSequence = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence T", null);
		index.addSequence(5, encoder.encodeSymbolListToIntegerArray(ss), SEQUENCE_OFFSET);
		
		stringSequence = "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence C", null);
		index.addSequence(6, encoder.encodeSymbolListToIntegerArray(ss), SEQUENCE_OFFSET);
		
		stringSequence = "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence G", null);
		index.addSequence(7, encoder.encodeSymbolListToIntegerArray(ss), SEQUENCE_OFFSET);
		
		stringSequence = "ACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCA";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence ACTGGTCA", null);
		index.addSequence(8, encoder.encodeSymbolListToIntegerArray(ss), SEQUENCE_OFFSET);
		
		stringSequence = "ATCTGAGTCATGCGATCAGTGTTGGTCATGTCAGGTCAGTACTACGTAGCATGCATGCATACGATCGACTATATTGCATGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R1", null);
		index.addSequence(9, encoder.encodeSymbolListToIntegerArray(ss), SEQUENCE_OFFSET);
		
		stringSequence = "AAAAAAACAAAAAAAGAAAAAAATTTTTTTGCATCAGATTTTTTTTCAGTACTGCATGACTACTGTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R2", null);
		index.addSequence(10, encoder.encodeSymbolListToIntegerArray(ss), SEQUENCE_OFFSET);
		
		stringSequence = "TGCAGTACGTACGTGTTGAGTGCTATGCATGTTTAGGCGCGGCGCTAGCATGCATCAGACGCATACGTGTACGTACGTACTGATTCAGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R3", null);
		index.addSequence(11, encoder.encodeSymbolListToIntegerArray(ss), SEQUENCE_OFFSET);
		
		stringSequence = "ACGTAGCTTACTATTGATATGAGTCGTGACGACTGACTACGTACGTACGACTGACTACGTATCGTCAGCTGCGTCATGCATTACTGACTGACTGAGTCTGATCATGACTTGACTGACTGACTGGTACTACGTGTACTACGTGTACTACGTAGCTACGACGTACGTACTGGTACTGACTGACGTGTACGCTAGCATGCATCGATGACGTACGTGATCTACTGACTGTACTGACTGGTACGACTACGTACGACTGACTGACTGACTACGATGCTGACTGACGTTGACGTACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R4", null);
		index.addSequence(12, encoder.encodeSymbolListToIntegerArray(ss), SEQUENCE_OFFSET);
		index.finishConstruction();
	}
	
	/**
	 * @throws IllegalSymbolException
	 * @throws BioException
	 * @throws ValueOutOfBoundsException
	 * @throws IOException 
	 */
	//@Test
	public void testIfFindSubSequences() throws IllegalSymbolException, BioException, ValueOutOfBoundsException, IOException {
		populateNonSoRandomSequences(index);
		
		long[] matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("AAAAAAAA"));
		
		assertEquals(8, matchingSubSequence.length);
		assertEquals(4, EncoderSubSequenceIndexInfo.getSequenceId(matchingSubSequence[0]));
		assertEquals(0, EncoderSubSequenceIndexInfo.getStart(matchingSubSequence[0]));
		
		assertEquals(4, EncoderSubSequenceIndexInfo.getSequenceId(matchingSubSequence[1]));
		assertEquals(8, EncoderSubSequenceIndexInfo.getStart(matchingSubSequence[1]));
		
		assertEquals(4, EncoderSubSequenceIndexInfo.getSequenceId(matchingSubSequence[2]));
		assertEquals(16, EncoderSubSequenceIndexInfo.getStart(matchingSubSequence[2]));
		
		assertEquals(4, EncoderSubSequenceIndexInfo.getSequenceId(matchingSubSequence[3]));
		assertEquals(24, EncoderSubSequenceIndexInfo.getStart(matchingSubSequence[3]));
		
		assertEquals(4, EncoderSubSequenceIndexInfo.getSequenceId(matchingSubSequence[4]));
		assertEquals(32, EncoderSubSequenceIndexInfo.getStart(matchingSubSequence[4]));
		
		assertEquals(4, EncoderSubSequenceIndexInfo.getSequenceId(matchingSubSequence[5]));
		assertEquals(40, EncoderSubSequenceIndexInfo.getStart(matchingSubSequence[5]));
		
		assertEquals(4, EncoderSubSequenceIndexInfo.getSequenceId(matchingSubSequence[6]));
		assertEquals(48, EncoderSubSequenceIndexInfo.getStart(matchingSubSequence[6]));
		
		matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("GCATGCAT"));
		assertEquals(2, matchingSubSequence.length);
		
		assertEquals(1, EncoderSubSequenceIndexInfo.getSequenceId(matchingSubSequence[0]));
		assertEquals(16, EncoderSubSequenceIndexInfo.getStart(matchingSubSequence[0]));
		
		assertEquals(9, EncoderSubSequenceIndexInfo.getSequenceId(matchingSubSequence[1]));
		assertEquals(48, EncoderSubSequenceIndexInfo.getStart(matchingSubSequence[1]));
	}
	
	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ValueOutOfBoundsException 
	 * @throws BioException 
	 * @throws IllegalSymbolException 
	 */
	@Test
	public void test_DNA_1000_200_700_sequences() throws FileNotFoundException, IOException, ClassNotFoundException, IllegalSymbolException, BioException, ValueOutOfBoundsException {
		
		List<Sequence> population = DNASequencesPopulator.readPopulation("data" + File.separator + "populator" + File.separator + "test_sequences_dataset_dna_500_200_700.seqs" );
				
		int code = 0;
		index.constructIndex();
		for(Sequence sequence: population) {
			index.addSequence(code, encoder.encodeSymbolListToIntegerArray(sequence), SEQUENCE_OFFSET);
			code++;
		}
		index.finishConstruction();
		
		long[] matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("TCTTGCCC"));
		assertEquals(2, matchingSubSequence.length);
		assertEquals(132, EncoderSubSequenceIndexInfo.getSequenceId(matchingSubSequence[0]));
		assertEquals(152, EncoderSubSequenceIndexInfo.getStart(matchingSubSequence[0]));
		
		assertEquals(483, EncoderSubSequenceIndexInfo.getSequenceId(matchingSubSequence[1]));
		assertEquals(224, EncoderSubSequenceIndexInfo.getStart(matchingSubSequence[1]));
				
		matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("GAGAATAC"));
		assertEquals(1, matchingSubSequence.length);
		assertEquals(0, EncoderSubSequenceIndexInfo.getSequenceId(matchingSubSequence[0]));
		assertEquals(0, EncoderSubSequenceIndexInfo.getStart(matchingSubSequence[0]));
		
		matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("TCTTGCCG"));
		assertEquals(0, matchingSubSequence.length);	
	}
	
}
