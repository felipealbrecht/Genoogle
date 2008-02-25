package bio.pih.tests.index;

import junit.framework.TestCase;

import org.biojava.bio.BioException;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.junit.Test;

import bio.pih.alignment.GenoogleNeedlemanWunsch;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.seq.LightweightSymbolList;

/**
 * @author albrecht
 *
 * Some tests to verify is the scores schema satisfies the alignment objectives.
 */
public class PontuationSchemaTest extends TestCase {

	
	/**
	 * @throws ValueOutOfBoundsException 
	 * @throws BioException 
	 * 
	 * It does not test many things really, but is a good place to verify the alignments scores. 
	 */
	@Test
	public void testPontuation_1_minus1_minus4_0() throws BioException {
		int match = -1;
		int dismatch = 1;
		int gapOpen = 2;
		int gapExtend = 0;
		int threadshould = 1;
		
		SubstitutionMatrix substitutionMatrix = new SubstitutionMatrix(DNATools.getDNA(), match * -1, dismatch * -1); // values
		GenoogleNeedlemanWunsch aligner = new GenoogleNeedlemanWunsch(match, dismatch, gapOpen, gapOpen, gapExtend, substitutionMatrix);
		
		Sequence seq1 = new SimpleSequence(LightweightSymbolList.createDNA("ACTGCGTC"), null, "ACTGCGTC", null);
		Sequence seq2 = new SimpleSequence(LightweightSymbolList.createDNA("TGCGTCCA"), null, "TGCGTCCA", null);
		double value = aligner.pairwiseAlignment(seq1, seq2) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value > threadshould);

		Sequence seq3 = new SimpleSequence(LightweightSymbolList.createDNA("TGGACCCC"), null, "TGGACCCC", null);
		Sequence seq4 = new SimpleSequence(LightweightSymbolList.createDNA("TGGACCCC"), null, "TGGACCCC", null);
		value = aligner.pairwiseAlignment(seq3, seq4) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value > threadshould);

		Sequence seq3a = new SimpleSequence(LightweightSymbolList.createDNA("AAAAAAAA"), null, "AAAAAAAA", null);
		Sequence seq4a = new SimpleSequence(LightweightSymbolList.createDNA("CCCCCCCC"), null, "CCCCCCCC", null);
		value = aligner.pairwiseAlignment(seq3a, seq4a) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value < threadshould);

		Sequence seq5 = new SimpleSequence(LightweightSymbolList.createDNA("AAAACCCC"), null, "AAAACCCC", null);
		Sequence seq6 = new SimpleSequence(LightweightSymbolList.createDNA("CCCCAAAA"), null, "CCCCAAAA", null);
		value = aligner.pairwiseAlignment(seq6, seq5) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value < threadshould);

		Sequence seq5a = new SimpleSequence(LightweightSymbolList.createDNA("AATCCCCT"), null, "AATCCCCT", null);
		Sequence seq6a = new SimpleSequence(LightweightSymbolList.createDNA("CCCCAAGA"), null, "CCCCAAGA", null);
		value = aligner.pairwiseAlignment(seq6a, seq5a) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value < threadshould);

		// continuacao da seq6a
		Sequence seq5aa = new SimpleSequence(LightweightSymbolList.createDNA("AATCCCCT"), null, "AATCCCCT", null);
		Sequence seq6aa = new SimpleSequence(LightweightSymbolList.createDNA("TCCCCAAG"), null, "TCCCAAAG", null);
		value = aligner.pairwiseAlignment(seq6aa, seq5aa) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value < threadshould);

		// continuacao da seq5a
		Sequence seq5ab = new SimpleSequence(LightweightSymbolList.createDNA("CAATCCCC"), null, "CAATCCCC", null);
		Sequence seq6ab = new SimpleSequence(LightweightSymbolList.createDNA("CCCCAAGA"), null, "CCCCAAGA", null);
		value = aligner.pairwiseAlignment(seq6ab, seq5ab) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value < threadshould);

		Sequence seq5b = new SimpleSequence(LightweightSymbolList.createDNA("AACCCCCT"), null, "AACCCCCT", null);
		Sequence seq6b = new SimpleSequence(LightweightSymbolList.createDNA("CCCCCAAA"), null, "CCCCCAAA", null);
		value = aligner.pairwiseAlignment(seq6b, seq5b) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value < threadshould);

		Sequence seq5c = new SimpleSequence(LightweightSymbolList.createDNA("AACCCCCT"), null, "AACCCCCT", null);
		Sequence seq6c = new SimpleSequence(LightweightSymbolList.createDNA("ACCCCCAA"), null, "ACCCCAAA", null);
		value = aligner.pairwiseAlignment(seq6c, seq5c) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value > threadshould);

		Sequence seq5d = new SimpleSequence(LightweightSymbolList.createDNA("TACCCCCT"), null, "TACCCCCT", null);
		Sequence seq6d = new SimpleSequence(LightweightSymbolList.createDNA("TCCCCCAA"), null, "TCCCCCAA", null);
		value = aligner.pairwiseAlignment(seq6d, seq5d) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value > threadshould);

		Sequence seq7 = new SimpleSequence(LightweightSymbolList.createDNA("AAAAAAAA"), null, "AAAAAAAA", null);
		Sequence seq8 = new SimpleSequence(LightweightSymbolList.createDNA("AAAAAAAA"), null, "AAAAAAAA", null);
		value = aligner.pairwiseAlignment(seq7, seq8) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value > threadshould);

		Sequence seq7a = new SimpleSequence(LightweightSymbolList.createDNA("AAAAAAAA"), null, "AAAAAAAA", null);
		Sequence seq8a = new SimpleSequence(LightweightSymbolList.createDNA("AAAAAAAC"), null, "AAAAAAAC", null);
		value = aligner.pairwiseAlignment(seq7a, seq8a) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value > threadshould);

		Sequence seq9 = new SimpleSequence(LightweightSymbolList.createDNA("AACCAACC"), null, "AACCAACC", null);
		Sequence seq10 = new SimpleSequence(LightweightSymbolList.createDNA("CCAACCAA"), null, "CCAACCAA", null);
		value = aligner.pairwiseAlignment(seq9, seq10) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value > threadshould);

		Sequence seq11 = new SimpleSequence(LightweightSymbolList.createDNA("AAAAACCC"), null, "AAAAACCC", null);
		Sequence seq12 = new SimpleSequence(LightweightSymbolList.createDNA("CCCAAAAA"), null, "CCCAAAAA", null);
		value = aligner.pairwiseAlignment(seq11, seq12) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value >= threadshould);

		Sequence seq11a = new SimpleSequence(LightweightSymbolList.createDNA("AAATGCCC"), null, "AAATGCCC", null);
		Sequence seq12a = new SimpleSequence(LightweightSymbolList.createDNA("CCCGTAAA"), null, "CCCGTAAA", null);
		value = aligner.pairwiseAlignment(seq11a, seq12a) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value < threadshould);

		Sequence seq13 = new SimpleSequence(LightweightSymbolList.createDNA("AAAGGCCC"), null, "AAAAACCC", null);
		Sequence seq14 = new SimpleSequence(LightweightSymbolList.createDNA("CCCGGAAA"), null, "CCCAAAAA", null);
		value = aligner.pairwiseAlignment(seq13, seq14) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value < threadshould);

		Sequence seq15 = new SimpleSequence(LightweightSymbolList.createDNA("ACTGCCCC"), null, "AAAAACCC", null);
		Sequence seq16 = new SimpleSequence(LightweightSymbolList.createDNA("ACTGTTTT"), null, "CCCAAAAA", null);
		value = aligner.pairwiseAlignment(seq15, seq16) * -1;
		System.out.println(aligner.getAlignmentString());
		assertTrue(value < threadshould);
	}
}
