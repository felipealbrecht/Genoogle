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

import bio.pih.index.SubSequenceInfo;
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

	SubSequencesArrayIndex index;
	
	@Override
	protected void setUp() throws Exception {
		index = new SubSequencesArrayIndex(8, DNATools.getDNA());
		populateNonSoRandomSequences(index);
	}
	
	@Override
	protected void tearDown() {
		index = null;
	}

	private void populateNonSoRandomSequences(SubSequencesArrayIndex index) throws IllegalSymbolException {			
		String stringSequence = "CATGACTGGCATCAGTGCATGCATGCAGTCAGTATATATGACGC";
		SymbolList symbolList = LightweightSymbolList.createDNA(stringSequence);
		SimpleSequence ss = new SimpleSequence(symbolList, null, "Sequence 1", null);
		index.addSequence(ss);
		
		stringSequence = "ACATGCTCGATGTGTGTGTATCAGTACTGACCTAGCATGACTCAGTACACATGACGTCATCATGTAGCGTCTAGACTGACTACGTACGACTGCATACGACTATCAGACTGACTACGCATGACGTACGTGTACGTACTGATGACGTACTATCGTAGCATGACTACGTACGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence 2", null);
		index.addSequence(ss);
		
		stringSequence = "ATGCTAGCATTCAGTACGTACGCATGATGCTAGATCGCATGACTAGCACGTACTGCATCGTGTGTGTCATGTGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence 3", null);
		index.addSequence(ss);
		
		stringSequence = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence A", null);
		index.addSequence(ss);
		
		stringSequence = "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence T", null);
		index.addSequence(ss);
		
		stringSequence = "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence C", null);
		index.addSequence(ss);
		
		stringSequence = "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence G", null);
		index.addSequence(ss);
		
		stringSequence = "ACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCAACTGGTCA";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence ACTGGTCA", null);
		index.addSequence(ss);
		
		stringSequence = "ATCTGAGTCATGCGATCAGTGTTGGTCATGTCAGGTCAGTACTACGTAGCATGCATGCATACGATCGACTATATTGCATGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R1", null);
		index.addSequence(ss);
		
		stringSequence = "AAAAAAACAAAAAAAGAAAAAAATTTTTTTGCATCAGATTTTTTTTCAGTACTGCATGACTACTGTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R2", null);
		index.addSequence(ss);
		
		stringSequence = "TGCAGTACGTACGTGTTGAGTGCTATGCATGTTTAGGCGCGGCGCTAGCATGCATCAGACGCATACGTGTACGTACGTACTGATTCAGACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R2", null);
		index.addSequence(ss);
		
		stringSequence = "ACGTAGCTTACTATTGATATGAGTCGTGACGACTGACTACGTACGTACGACTGACTACGTATCGTCAGCTGCGTCATGCATTACTGACTGACTGAGTCTGATCATGACTTGACTGACTGACTGGTACTACGTGTACTACGTGTACTACGTAGCTACGACGTACGTACTGGTACTGACTGACGTGTACGCTAGCATGCATCGATGACGTACGTGATCTACTGACTGTACTGACTGGTACGACTACGTACGACTGACTGACTGACTACGATGCTGACTGACGTTGACGTACTGAC";
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		ss = new SimpleSequence(symbolList, null, "Sequence R3", null);
		index.addSequence(ss);		
	}
	
	/**
	 * @throws IllegalSymbolException
	 * @throws BioException
	 * @throws ValueOutOfBoundsException
	 */
	//@Test
	public void testIfFindSubSequences() throws IllegalSymbolException, BioException, ValueOutOfBoundsException {
		List<SubSequenceInfo> matchingSubSequence = index.getMatchingSubSequence("AAAAAAAA");
		
		assertEquals(7, matchingSubSequence.size());
		assertEquals("Sequence A", matchingSubSequence.get(0).getSequence().getName());
		assertEquals(0, matchingSubSequence.get(0).getStart());
		
		assertEquals("Sequence A", matchingSubSequence.get(1).getSequence().getName());
		assertEquals(8, matchingSubSequence.get(1).getStart());
		
		assertEquals("Sequence A", matchingSubSequence.get(2).getSequence().getName());
		assertEquals(16, matchingSubSequence.get(2).getStart());
		
		assertEquals("Sequence A", matchingSubSequence.get(3).getSequence().getName());
		assertEquals(24, matchingSubSequence.get(3).getStart());
		
		assertEquals("Sequence A", matchingSubSequence.get(4).getSequence().getName());
		assertEquals(32, matchingSubSequence.get(4).getStart());
		
		assertEquals("Sequence A", matchingSubSequence.get(5).getSequence().getName());
		assertEquals(40, matchingSubSequence.get(5).getStart());
		
		assertEquals("Sequence A", matchingSubSequence.get(6).getSequence().getName());
		assertEquals(48, matchingSubSequence.get(6).getStart());
		
		matchingSubSequence = index.getMatchingSubSequence("GCATGCAT");
		assertEquals(2, matchingSubSequence.size());
		
		assertEquals("Sequence 1", matchingSubSequence.get(0).getSequence().getName());
		assertEquals(16, matchingSubSequence.get(0).getStart());
		
		assertEquals("Sequence R1", matchingSubSequence.get(1).getSequence().getName());
		assertEquals(48, matchingSubSequence.get(1).getStart());
	}
	
	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void test_DNA_1000_200_700_sequences() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		List<Sequence> population = DNASequencesPopulator.readPopulation("data" + File.separator + "populator" + File.separator + "test_sequences_dataset_dna_500_200_700.seqs" );
		System.out.println(population.get(0).seqString());
		System.out.println(population.get(1).seqString());
		System.out.println(population.get(2).seqString());
		System.out.println(population.get(3).seqString());
		
//		for (int i = 0; i < 1000; i++) {
//			index.addSequence(population.get(i));
//		}
		//System.out.println(index.indexStatus());
		
	}
	
}
