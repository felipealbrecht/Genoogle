package bio.pih.tests.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.junit.Test;

import bio.pih.index.EncodedSubSequencesIndex;
import bio.pih.index.SubSequenceIndexInfo;
import bio.pih.index.SubSequencesArrayIndex;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.seq.LightweightSymbolList;
import bio.pih.seq.generator.DNASequencesPopulator;

/**
 * Tests for the {@link SubSequencesArrayIndex}
 * 
 * @author albrecht
 */
public class SubSequencesArrayIndexTest extends TestCase {

	EncodedSubSequencesIndex index;
	
	@Override
	protected void setUp() throws Exception {
		index = new SubSequencesArrayIndex(8, DNATools.getDNA());		
	}
	
	@Override
	protected void tearDown() {
		index = null;
	}

	private void populateNonSoRandomSequences(EncodedSubSequencesIndex index) throws IllegalSymbolException {			
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
		
		long[] matchingSubSequence = index.getMatchingSubSequence("AAAAAAAA");
		
		assertEquals(7, matchingSubSequence.length);
		assertEquals(4, SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		assertEquals(0, SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		
		assertEquals(4, SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[1]));
		assertEquals(8, SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[1]));
		
		assertEquals(4, SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[2]));
		assertEquals(16, SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[2]));
		
		assertEquals(4, SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[3]));
		assertEquals(24, SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[3]));
		
		assertEquals(4, SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[4]));
		assertEquals(32, SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[4]));
		
		assertEquals(4, SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[5]));
		assertEquals(40, SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[5]));
		
		assertEquals(4, SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[6]));
		assertEquals(48, SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[6]));
		
		matchingSubSequence = index.getMatchingSubSequence("GCATGCAT");
		assertEquals(2, matchingSubSequence.length);
		
		assertEquals(1, SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		assertEquals(16, SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		
		assertEquals(9, SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[1]));
		assertEquals(48, SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[1]));
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
		
		long[] matchingSubSequence = index.getMatchingSubSequence("TCTTGCCC");
		assertEquals(2, matchingSubSequence.length);
		assertEquals(132, SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		assertEquals(152, SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		
		assertEquals(483, SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[1]));
		assertEquals(224, SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[1]));
				
		matchingSubSequence = index.getMatchingSubSequence("GAGAATAC");
		assertEquals(1, matchingSubSequence.length);
		assertEquals(0, SubSequenceIndexInfo.getSequenceIdFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		assertEquals(0, SubSequenceIndexInfo.getStartFromSubSequenceInfoIntRepresentation(matchingSubSequence[0]));
		
		matchingSubSequence = index.getMatchingSubSequence("TCTTGCCG");
		assertNull(matchingSubSequence);	
	}
	
}
