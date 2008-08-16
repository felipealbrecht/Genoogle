package bio.pih.tests.index;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.junit.Test;

import bio.pih.encoder.DNASequenceEncoderToShort;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SimilarSubSequencesIndex;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.index.SimilarSubSequencesIndex.ComparationResult;

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

		SimilarSubSequencesIndex subSequenceComparer = new SimilarSubSequencesIndex(DNATools.getDNA(), defaultSubSequenceLength, defaultMatch, defaultDismatch, defaultGapOpen, defaultGapExtend, defaultTreadshould);
		subSequenceComparer.deleteData();
		subSequenceComparer.generateData();
		subSequenceComparer = null;

		SimilarSubSequencesIndex subSequencesReader = new SimilarSubSequencesIndex(DNATools.getDNA(), defaultSubSequenceLength, defaultMatch, defaultDismatch, defaultGapOpen, defaultGapExtend, defaultTreadshould);
		assertTrue(subSequencesReader.hasDataFile());
		assertTrue(subSequencesReader.hasIndexFile());

		subSequencesReader.load();

		int[] similarSequences;

		for (int i = 0; i < subSequencesReader.getMaxEncodedSequenceValue(); i++) {
			similarSequences = subSequencesReader.getSimilarSequences((short) (i & 0xFFFF));
			assertEquals(i, SimilarSubSequencesIndex.getSequence(similarSequences[0]));
			assertEquals(4, SimilarSubSequencesIndex.getScore(similarSequences[0]));

			for (int intRepresentation : similarSequences) {
				if (SimilarSubSequencesIndex.getScore(intRepresentation) < defaultTreadshould) {
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
	public void testCreateData_SubSequenceLength5()  throws ValueOutOfBoundsException, IllegalSymbolException, IOException, BioException, InvalidHeaderData  {
		int defaultTreadshould = 5;
		int defaultMatch = -1;
		int defaultDismatch = 1;
		int defaultGapOpen = 2;
		int defaultGapExtend = 0;
		int defaultSubSequenceLength = 5;

		SimilarSubSequencesIndex subSequenceComparer = new SimilarSubSequencesIndex(DNATools.getDNA(), defaultSubSequenceLength, defaultMatch, defaultDismatch, defaultGapOpen, defaultGapExtend, defaultTreadshould);
		subSequenceComparer.deleteData();
		subSequenceComparer.generateData();
		subSequenceComparer = null;

		SimilarSubSequencesIndex subSequencesReader = new SimilarSubSequencesIndex(DNATools.getDNA(), defaultSubSequenceLength, defaultMatch, defaultDismatch, defaultGapOpen, defaultGapExtend, defaultTreadshould);
		assertTrue(subSequencesReader.hasDataFile());
		assertTrue(subSequencesReader.hasIndexFile());

		subSequencesReader.load();

		int[] similarSequences;

		for (int i = 0; i < subSequencesReader.getMaxEncodedSequenceValue(); i++) {
			similarSequences = subSequencesReader.getSimilarSequences((short) i);
			assertEquals(1, similarSequences.length);
			assertEquals(i, SimilarSubSequencesIndex.getSequence(similarSequences[0]));
			assertEquals(5, SimilarSubSequencesIndex.getScore(similarSequences[0]));

			for (int intRepresentation : similarSequences) {
				if (SimilarSubSequencesIndex.getScore(intRepresentation) < defaultTreadshould) {
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
	 */
	@Test(expected=IOException.class)
	public void testLoadingNonexistentData() throws ValueOutOfBoundsException {
		int defaultTreadshould = 10;
		int defaultMatch = -10;
		int defaultDismatch = 10;
		int defaultGapOpen = 20;
		int defaultGapExtend = 20;
		int defaultSubSequenceLength = 40;
				
		SimilarSubSequencesIndex subSequencesReader = new SimilarSubSequencesIndex(DNATools.getDNA(), defaultSubSequenceLength, defaultMatch, defaultDismatch, defaultGapOpen, defaultGapExtend, defaultTreadshould);
		assertFalse(subSequencesReader.hasDataFile());
		assertFalse(subSequencesReader.hasIndexFile());

		
		try {
			subSequencesReader.load();
		} catch (IOException e) {
			System.out.println("Okay!");
			return;
		} catch (InvalidHeaderData e) {
			fail("Invalid header data " + e.getMessage());
		}
			
		fail("FileNotFoundException has not raised");
	}
	
	/**
	 * Test the default data and verify if the data read is correct.
	 * @throws Exception 
	 * @throws BioException 
	 * @throws IllegalSymbolException 
	 */
	@Test
	public void testDefaultDataNoCheck() throws IllegalSymbolException, BioException, Exception {				
		SimilarSubSequencesIndex defaultInstance = SimilarSubSequencesIndex.getDefaultInstance();
		
		// DISCOMENT IT ONLY IF YOU DONT WANT TO DOWNLOAD THE DATA
		//defaultInstance.generateData()
										
		assertTrue(defaultInstance.hasDataFile());
		assertTrue(defaultInstance.hasIndexFile());

		long time = System.currentTimeMillis();
		defaultInstance.load();
		System.out.println("total: " + (System.currentTimeMillis() - time) / 1000);
		
		int score;
		int[] similarSequences;
		
		DNASequenceEncoderToShort encoder = defaultInstance.getEncoder();
		ComparationResult ar;
		List<ComparationResult> results = null;
		

		for (int i = 0; i < defaultInstance.getMaxEncodedSequenceValue(); i += 21771) {
			similarSequences = defaultInstance.getSimilarSequences((short) i);
			assertEquals(i, SimilarSubSequencesIndex.getSequence(similarSequences[0]) & 0xFFFF);
			assertEquals(8, SimilarSubSequencesIndex.getScore(similarSequences[0]));
			results = new LinkedList<ComparationResult>();
			
			for (int encodedSequence = 0; encodedSequence <= defaultInstance.getMaxEncodedSequenceValue(); encodedSequence++) {
				score =	(int) defaultInstance.getAligner().fastPairwiseAlignment(
						encoder.decodeShortToSymbolList((short)i),
						encoder.decodeShortToSymbolList((short)encodedSequence));
				score *= -1;
				
				assertEquals((short) defaultInstance.compareCompactedSequences((short)i, (short) encodedSequence), score);
														
				ar = new ComparationResult((short) score, (short) encodedSequence);
				if (score >= SimilarSubSequencesIndex.getDefaultTreadshould()) {
					results.add(ar);
				}							
			}
			Collections.sort(results, ComparationResult.getScoreComparator());
			
			assertEquals(results.size(), similarSequences.length);
			for (int r = 0; r < results.size(); r++) {
				assertEquals(results.get(r).getIntRepresentation(), similarSequences[r]);
			}			
		}
	}
	
	/**
	 * Test the default data checking and verify if the data read is correct.
	 * @throws Exception 
	 * @throws BioException 
	 * @throws IllegalSymbolException 
	 */
	@Test
	public void testDefaultDataCheckConsistency() throws IllegalSymbolException, BioException, Exception {				
		SimilarSubSequencesIndex defaultInstance = SimilarSubSequencesIndex.getDefaultInstance();
		
		// DISCOMENT IT ONLY IF YOU DONT WANT TO DOWNLOAD THE DATA
		//defaultInstance.generateData()
										
		assertTrue(defaultInstance.hasDataFile());
		assertTrue(defaultInstance.hasIndexFile());

		long time = System.currentTimeMillis();
		defaultInstance.load(true);
		System.out.println("total: " + (System.currentTimeMillis() - time) / 1000);
		
		int score;
		int[] similarSequences;
		
		DNASequenceEncoderToShort encoder = defaultInstance.getEncoder();
		ComparationResult ar;
		List<ComparationResult> results = null;
		

		for (int i = 0; i < defaultInstance.getMaxEncodedSequenceValue(); i += 21771) {
			similarSequences = defaultInstance.getSimilarSequences((short) i);
			assertEquals(i, SimilarSubSequencesIndex.getSequence(similarSequences[0]) & 0xFFFF);
			assertEquals(8, SimilarSubSequencesIndex.getScore(similarSequences[0]));
			results = new LinkedList<ComparationResult>();
			
			for (int encodedSequence = 0; encodedSequence <= defaultInstance.getMaxEncodedSequenceValue(); encodedSequence++) {							
				score =	(int) defaultInstance.getAligner().fastPairwiseAlignment(
						encoder.decodeShortToSymbolList((short)i),
						encoder.decodeShortToSymbolList((short)encodedSequence));
				score *= -1;
				
				assertEquals((int) defaultInstance.compareCompactedSequences((short)i, (short)encodedSequence), score);
														
				ar = new ComparationResult((short) score, (short) encodedSequence);
				if (score >= SimilarSubSequencesIndex.getDefaultTreadshould()) {
					results.add(ar);
				}							
			}
			Collections.sort(results, ComparationResult.getScoreComparator());
			
			assertEquals(results.size(), similarSequences.length);
			for (int r = 0; r < results.size(); r++) {
				assertEquals(results.get(r).getIntRepresentation(), similarSequences[r]);
			}			
		}
	}
	
}
