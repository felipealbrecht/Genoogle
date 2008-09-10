package bio.pih.tests;

import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.search.ExtendSequences;
import bio.pih.seq.LightweightSymbolList;
import junit.framework.TestCase;

public class ExtendAlignmentTest extends TestCase {
	
	private static final DNASequenceEncoderToInteger ENCODER = DNASequenceEncoderToInteger.getDefaultEncoder();
	
	public void testExtendOne() throws Exception {
		int[] encoded_1_g = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("G"));
		int[] encoded_2_g = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("G"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1_g, 0, 0, encoded_2_g, 0, 0, 5, 10);
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("G"));
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("G"));
	}
		
	public void testExtendTwo() throws Exception {
		int[] encoded_1_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AGA"));
		int[] encoded_2_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AGA"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1_gg, 0, 0, encoded_2_gg, 0, 0, 5, 10);		
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("AGA"));
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("AGA"));
	} 
	
	public void testExtendThree() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AACCCAA"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AACCCAA"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 2, 4, encoded_2, 2, 4, 5, 10);		
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("AACCCAA"));
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("AACCCAA"));				
	}
	
	public void testExtendOneOne() throws Exception {
		int[] encoded_1_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AG"));
		int[] encoded_2_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AG"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1_gg, 0, 0, encoded_2_gg, 0, 0, 5, 10);		
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("AG"));
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("AG"));
	} 
	
	public void testExtendTwoOne() throws Exception {
	int[] encoded_1_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AGC"));
	int[] encoded_2_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AGC"));
	
	ExtendSequences extension = ExtendSequences.doExtension(encoded_1_gg, 1, 1, encoded_2_gg, 1, 1, 5, 10);		
	assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("AGC"));
	assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("AGC"));
} 
	
	public void testExtendTwoOneBlash() throws Exception {
		int[] encoded_1_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AGC"));
		int[] encoded_2_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AGC"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1_gg, 2, 2, encoded_2_gg, 2, 2, 5, 10);		
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("AGC"));
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("AGC"));
	} 

public void testExtendThreeOne() throws Exception {		
	int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AACCCGG"));
	int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AACCCGG"));
	
	ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 2, 4, encoded_2, 2, 4, 5, 10);		
	assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("AACCCGG"));
	assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("AACCCGG"));				
}
	
	public void testExtend10() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ATGCATGACT"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ATGCATGACT"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 2, 4, encoded_2, 2, 4, 5, 10);		
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("ATGCATGACT"));
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("ATGCATGACT"));				
	}
	
	public void testExtend11() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ATGCATGACTA"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ATGCATGACTA"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 2, 4, encoded_2, 2, 4, 5, 10);		
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("ATGCATGACTA"));				
	}
			
	public void testExtendFour() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTT"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTT"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 2, 4, encoded_2, 2, 4, 5, 10);		
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTT"));
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTT"));				
	}
	
	public void testExtendFive() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTTTGATCA"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTTTGATCA"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 2, 4, encoded_2, 2, 4, 5, 10);		
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTTTGATCA"));
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTTTGATCA"));
	}
	
	public void testExtend11Diff() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ATGCATGACTA"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("GTGCATGACTC"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 2, 4, encoded_2, 2, 4, 5, 10);		
		assertEquals(LightweightSymbolList.createDNA("TGCATGACT"), extension.getTargetSequenceExtended());				
		assertEquals(LightweightSymbolList.createDNA("TGCATGACT"), extension.getQuerySequenceExtended());
	}
	
	
	public void testExtend11DiffMiddle() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("TAAGCATGCATGACTAGGTA"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("GCCAGGTGCATGACTCCCGA"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 6, 11, encoded_2, 6, 11, 5, 10);		
		assertEquals(LightweightSymbolList.createDNA("TGCATGACT"), extension.getTargetSequenceExtended());
		assertEquals(LightweightSymbolList.createDNA("TGCATGACT"), extension.getQuerySequenceExtended());
	}
	
	public void testExtend11DiffMiddleSing() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("GGGGGATGATTGTAGTTTATGAGTATGGATGACAGTAGCATTATATATAGATAGACACCGGAACCCCC"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("CCCCCATGATTGTAGTTTATGAGTATGGACCAGCGTAGTATTATATATAGATAGACATGACCGGGACCCCC"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 8, 25, encoded_2, 8, 25, 5, 10);
		System.out.println(extension.getTargetSequenceExtended().seqString());
		System.out.println(extension.getQuerySequenceExtended().seqString());
		System.out.println(LightweightSymbolList.createDNA("ATGATTGTAGTTTATGAGTATGGATGACAGTAGCATTATATATAGATAGACA").seqString());
		assertEquals(LightweightSymbolList.createDNA("ATGATTGTAGTTTATGAGTATGGACCAGCGTAGTATTATATATAGATAGACA"), extension.getTargetSequenceExtended());
		assertEquals(LightweightSymbolList.createDNA("ATGATTGTAGTTTATGAGTATGGATGACAGTAGCATTATATATAGATAGACA"), extension.getQuerySequenceExtended());
	}
}
