package bio.pih.tests.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.junit.Test;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SubSequencesComparer;
import bio.pih.index.ValueOutOfBoundsException;

/**
 * @author albrecht
 * 
 * <b>PAY ATTENTION: DONT CREATE TWO SAME SCORE SCHEME, BECAUSE OF THE FILE-LOCK
 * WINDOWS PROBLEM</b>
 */
public class SubSequenceComparerTest extends TestCase {

	/**
	 * Test the creation and reading process of the data of subsequences 4 bases
	 * length
	 * @throws ValueOutOfBoundsException 
	 * @throws BioException 
	 * @throws IOException 
	 * @throws IllegalSymbolException 
	 * @throws InvalidHeaderData 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateData_SubSequenceLength4() throws ValueOutOfBoundsException, IllegalSymbolException, IOException, BioException, InvalidHeaderData  {
		int defaultTreadshould = 1;
		int defaultMatch = -1;
		int defaultDismatch = 1;
		int defaultGapOpen = 2;
		int defaultGapExtend = 0;
		int defaultSubSequenceLength = 4;

		SubSequencesComparer subSequenceComparer = new SubSequencesComparer(DNATools.getDNA(), defaultSubSequenceLength, defaultMatch, defaultDismatch, defaultGapOpen, defaultGapExtend, defaultTreadshould);
		subSequenceComparer.generateData();
		subSequenceComparer = null;

		SubSequencesComparer subSequencesReader = new SubSequencesComparer(DNATools.getDNA(), defaultSubSequenceLength, defaultMatch, defaultDismatch, defaultGapOpen, defaultGapExtend, defaultTreadshould);
		assertTrue(subSequencesReader.hasDataFile());
		assertTrue(subSequencesReader.hasIndexFile());

		subSequencesReader.load();

		int[] similarSequences;

		for (int i = 0; i < subSequencesReader.getMaxEncodedSequenceValue(); i++) {
			similarSequences = subSequencesReader.getSimilarSequences((short) i);
			assertEquals(i, SubSequencesComparer.getSequenceFromIntRepresentation(similarSequences[0]));
			assertEquals(4, SubSequencesComparer.getScoreFromIntRepresentation(similarSequences[0]));

			for (int intRepresentation : similarSequences) {
				if (SubSequencesComparer.getScoreFromIntRepresentation(intRepresentation) < defaultTreadshould) {
					fail("subsequence alinhada possui score inferior ao limite");
				}
			}
		}

		new File(subSequencesReader.getDataFileName()).deleteOnExit();
		new File(subSequencesReader.getIndexFileName()).deleteOnExit();

	}

	/**
	 * Test the creation and reading process of the data of subsequences 6 bases
	 * length
	 * @throws ValueOutOfBoundsException 
	 * @throws IllegalSymbolException 
	 * @throws IOException 
	 * @throws BioException 
	 * @throws InvalidHeaderData 
	 * 
	 */
	@Test
	public void testCreateData_SubSequenceLength6()  throws ValueOutOfBoundsException, IllegalSymbolException, IOException, BioException, InvalidHeaderData  {
		int defaultTreadshould = 5;
		int defaultMatch = -1;
		int defaultDismatch = 1;
		int defaultGapOpen = 2;
		int defaultGapExtend = 0;
		int defaultSubSequenceLength = 5;

		SubSequencesComparer subSequenceComparer = new SubSequencesComparer(DNATools.getDNA(), defaultSubSequenceLength, defaultMatch, defaultDismatch, defaultGapOpen, defaultGapExtend, defaultTreadshould);
		subSequenceComparer.generateData();
		subSequenceComparer = null;

		SubSequencesComparer subSequencesReader = new SubSequencesComparer(DNATools.getDNA(), defaultSubSequenceLength, defaultMatch, defaultDismatch, defaultGapOpen, defaultGapExtend, defaultTreadshould);
		assertTrue(subSequencesReader.hasDataFile());
		assertTrue(subSequencesReader.hasIndexFile());

		subSequencesReader.load();

		int[] similarSequences;

		for (int i = 0; i < subSequencesReader.getMaxEncodedSequenceValue(); i++) {
			similarSequences = subSequencesReader.getSimilarSequences((short) i);
			assertEquals(1, similarSequences.length);
			assertEquals(i, SubSequencesComparer.getSequenceFromIntRepresentation(similarSequences[0]));
			assertEquals(5, SubSequencesComparer.getScoreFromIntRepresentation(similarSequences[0]));

			for (int intRepresentation : similarSequences) {
				if (SubSequencesComparer.getScoreFromIntRepresentation(intRepresentation) < defaultTreadshould) {
					fail("subsequence alinhada possui score inferior ao limite");
				}
			}
		}

		new File(subSequencesReader.getDataFileName()).deleteOnExit();
		new File(subSequencesReader.getIndexFileName()).deleteOnExit();
	}
	
	/**
	 * Test the load process of some similar sequences and check if they are realy similar 
	 * @throws ValueOutOfBoundsException 
	 * @throws IOException 
	 * @throws InvalidHeaderData 
	 */
	@Test(expected=FileNotFoundException.class)
	public void testLoadingNonexistentData() throws ValueOutOfBoundsException, InvalidHeaderData, IOException {
		int defaultTreadshould = 10;
		int defaultMatch = -10;
		int defaultDismatch = 10;
		int defaultGapOpen = 20;
		int defaultGapExtend = 20;
		int defaultSubSequenceLength = 40;
				
		SubSequencesComparer subSequencesReader = new SubSequencesComparer(DNATools.getDNA(), defaultSubSequenceLength, defaultMatch, defaultDismatch, defaultGapOpen, defaultGapExtend, defaultTreadshould);
		assertFalse(subSequencesReader.hasDataFile());
		assertFalse(subSequencesReader.hasIndexFile());

		try {
			subSequencesReader.load();			
		} catch (FileNotFoundException fnfe) {
			return;
		}
		fail("FileNotFoundException has not raised");
	}

	
	/**
	 * Test the load process of some similar sequences and check if they are realy similar
	 */
//	@Test
//	public void testLoadingSomeData() {
//		int defaultTreadshould = 2;
//		int defaultMatch = -1;
//		int defaultDismatch = 1;
//		int defaultGapOpen = 2;
//		int defaultGapExtend = 0;
//		int defaultSubSequenceLength = 4;
//
//		SubSequencesComparer subSequenceComparer = new SubSequencesComparer(DNATools.getDNA(), defaultSubSequenceLength, defaultMatch, defaultDismatch, defaultGapOpen, defaultGapExtend, defaultTreadshould);
//		subSequenceComparer.generateData();
//		subSequenceComparer = null;
//		
//		SubSequencesComparer subSequencesReader = new SubSequencesComparer(DNATools.getDNA(), defaultSubSequenceLength, defaultMatch, defaultDismatch, defaultGapOpen, defaultGapExtend, defaultTreadshould);
//		assertTrue(subSequencesReader.hasDataFile());
//		assertTrue(subSequencesReader.hasIndexFile());
//
//		subSequencesReader.load();
//		
//		
//	}
//	
}
