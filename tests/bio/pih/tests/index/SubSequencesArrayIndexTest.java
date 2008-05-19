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

import bio.pih.index.EncoderSubSequenceIndexInfo;
import bio.pih.index.MemorySubSequencesInvertedIndex;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.SequenceDataBank;
import bio.pih.seq.LightweightSymbolList;
import bio.pih.seq.generator.DNASequencesPopulator;

/**
 * Tests for the {@link MemorySubSequencesInvertedIndex}
 * 
 * @author albrecht
 */
public class SubSequencesArrayIndexTest extends TestCase {

	MemorySubSequencesInvertedIndex index;
	
	@Override
	protected void setUp() throws Exception {
		SequenceDataBank dataBank = new IndexedDNASequenceDataBank("dummy", null, null);
		index = new MemorySubSequencesInvertedIndex (dataBank, 8);		
	}
	
	@Override
	protected void tearDown() {
		index = null;
	}

	private void populateNonSoRandomSequences(MemorySubSequencesInvertedIndex  index) throws IllegalSymbolException {			
		String stringSequence = "CATGACTGGCATCAGTGCATGCATGCAGTCAGTATATATGACGC";
		SymbolList symbolList = LightweightSymbolList.createDNA(stringSequence);
		SimpleSequence ss = new SimpleSequence(symbolList, null, "Sequence 1", null);
		index.addSequence(1, ss);
		
		stringSequence = "ACATGCTCGATGTGTGTGTATCAGTACTGACCTAGCATGACTCAGTACACATGACGTCATCATGTAGCGTCTAGACTGACTACGTACGACTGCATACGACTATCAGACTGACTACGCATGACGTACGTGTACGTACTGATGACGTACTATCGTAGCATGACTACGTACGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence 2", null);
		index.addSequence(2, ss);
		
		stringSequence = "ATGCTAGCATTCAGTACGTACGCATGATGCTAGATCGCATGACTAGCACGTACTGCATCGTGTGTGTCATGTGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence 3", null);
		index.addSequence(3, ss);
		
		stringSequence = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence A", null);
		index.addSequence(4, ss);
		
		stringSequence = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence T", null);
		index.addSequence(5, ss);
		
		stringSequence = "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence C", null);
		index.addSequence(6, ss);
		
		stringSequence = "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence G", null);
		index.addSequence(7, ss);
		
		stringSequence = "ACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCA";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence ACTGGTCA", null);
		index.addSequence(8, ss);
		
		stringSequence = "ATCTGAGTCATGCGATCAGTGTTGGTCATGTCAGGTCAGTACTACGTAGCATGCATGCATACGATCGACTATATTGCATGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R1", null);
		index.addSequence(9, ss);
		
		stringSequence = "AAAAAAACAAAAAAAGAAAAAAATTTTTTTGCATCAGATTTTTTTTCAGTACTGCATGACTACTGTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R2", null);
		index.addSequence(10, ss);
		
		stringSequence = "TGCAGTACGTACGTGTTGAGTGCTATGCATGTTTAGGCGCGGCGCTAGCATGCATCAGACGCATACGTGTACGTACGTACTGATTCAGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R3", null);
		index.addSequence(11, ss);
		
		stringSequence = "ACGTAGCTTACTATTGATATGAGTCGTGACGACTGACTACGTACGTACGACTGACTACGTATCGTCAGCTGCGTCATGCATTACTGACTGACTGAGTCTGATCATGACTTGACTGACTGACTGGTACTACGTGTACTACGTGTACTACGTAGCTACGACGTACGTACTGGTACTGACTGACGTGTACGCTAGCATGCATCGATGACGTACGTGATCTACTGACTGTACTGACTGGTACGACTACGTACGACTGACTGACTGACTACGATGCTGACTGACGTTGACGTACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R4", null);
		index.addSequence(12, ss);		
	}
	
	/**
	 * @throws IllegalSymbolException
	 * @throws BioException
	 * @throws ValueOutOfBoundsException
	 */
	//@Test
	public void testIfFindSubSequences() throws IllegalSymbolException, BioException, ValueOutOfBoundsException {
		populateNonSoRandomSequences(index);
		
		int[] matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("AAAAAAAA"));
		
		assertEquals(7, matchingSubSequence.length);
		assertEquals(4, EncoderSubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		assertEquals(0, EncoderSubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		
		assertEquals(4, EncoderSubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[1]));
		assertEquals(8, EncoderSubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[1]));
		
		assertEquals(4, EncoderSubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[2]));
		assertEquals(16, EncoderSubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[2]));
		
		assertEquals(4, EncoderSubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[3]));
		assertEquals(24, EncoderSubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[3]));
		
		assertEquals(4, EncoderSubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[4]));
		assertEquals(32, EncoderSubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[4]));
		
		assertEquals(4, EncoderSubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[5]));
		assertEquals(40, EncoderSubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[5]));
		
		assertEquals(4, EncoderSubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[6]));
		assertEquals(48, EncoderSubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[6]));
		
		matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("GCATGCAT"));
		assertEquals(2, matchingSubSequence.length);
		
		assertEquals(1, EncoderSubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		assertEquals(16, EncoderSubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		
		assertEquals(9, EncoderSubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[1]));
		assertEquals(48, EncoderSubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[1]));
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
		for(Sequence sequence: population) {
			index.addSequence(code, sequence);
			code++;
		}
		
		int[] matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("TCTTGCCC"));
		assertEquals(2, matchingSubSequence.length);
		assertEquals(132, EncoderSubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		assertEquals(152, EncoderSubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		
		assertEquals(483, EncoderSubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[1]));
		assertEquals(224, EncoderSubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[1]));
				
		matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("GAGAATAC"));
		assertEquals(1, matchingSubSequence.length);
		assertEquals(0, EncoderSubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		assertEquals(0, EncoderSubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		
		matchingSubSequence = index.getMatchingSubSequence(LightweightSymbolList.createDNA("TCTTGCCG"));
		assertEquals(0, matchingSubSequence.length);	
	}
	
}
