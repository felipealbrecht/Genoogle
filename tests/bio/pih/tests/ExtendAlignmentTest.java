package bio.pih.tests;

import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.search.ExtendSequences;
import bio.pih.seq.LightweightSymbolList;
import junit.framework.TestCase;


/**
 * TODO: Create version where the input arent SymbolList, but String.
 */
public class ExtendAlignmentTest extends TestCase {
	
	private static final int EXTENSTION_DROPOFF = 5;
	private static final int SUB_SEQUENCE_LENGTH = 11;
	private static final DNASequenceEncoderToInteger ENCODER = DNASequenceEncoderToInteger.getEncoder(SUB_SEQUENCE_LENGTH);
	
	public void testExtendOne() throws Exception {
		int[] encoded_1_g = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("G"));
		int[] encoded_2_g = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("G"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1_g, 0, 0, encoded_2_g, 0, 0, EXTENSTION_DROPOFF, SUB_SEQUENCE_LENGTH, ENCODER);
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("G").seqString());
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("G").seqString());
	}
		
	public void testExtendTwo() throws Exception {
		int[] encoded_1_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AGA"));
		int[] encoded_2_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AGA"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1_gg, 0, 0, encoded_2_gg, 0, 0, EXTENSTION_DROPOFF, SUB_SEQUENCE_LENGTH, ENCODER);		
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("AGA").seqString());
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("AGA").seqString());
	} 
	
	public void testExtendThree() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AACCCAA"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AACCCAA"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 2, 4, encoded_2, 2, 4, EXTENSTION_DROPOFF, SUB_SEQUENCE_LENGTH, ENCODER);		
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("AACCCAA").seqString());
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("AACCCAA").seqString());				
	}
	
	public void testExtendOneOne() throws Exception {
		int[] encoded_1_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AG"));
		int[] encoded_2_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AG"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1_gg, 0, 0, encoded_2_gg, 0, 0, EXTENSTION_DROPOFF, SUB_SEQUENCE_LENGTH, ENCODER);		
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("AG").seqString());
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("AG").seqString());
	} 
	
	public void testExtendTwoOne() throws Exception {
	int[] encoded_1_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AGC"));
	int[] encoded_2_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AGC"));
	
	ExtendSequences extension = ExtendSequences.doExtension(encoded_1_gg, 1, 1, encoded_2_gg, 1, 1, EXTENSTION_DROPOFF, SUB_SEQUENCE_LENGTH, ENCODER);		
	assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("AGC").seqString());
	assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("AGC").seqString());
} 
	
	public void testExtendTwoOneBlash() throws Exception {
		int[] encoded_1_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AGC"));
		int[] encoded_2_gg = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AGC"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1_gg, 2, 2, encoded_2_gg, 2, 2, EXTENSTION_DROPOFF, SUB_SEQUENCE_LENGTH, ENCODER);		
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("AGC").seqString());
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("AGC").seqString());
	} 

public void testExtendThreeOne() throws Exception {		
	int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AACCCGG"));
	int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("AACCCGG"));
	
	ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 2, 4, encoded_2, 2, 4, EXTENSTION_DROPOFF, SUB_SEQUENCE_LENGTH, ENCODER);		
	assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("AACCCGG").seqString());
	assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("AACCCGG").seqString());				
}
	
	public void testExtend10() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ATGCATGACT"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ATGCATGACT"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 2, 4, encoded_2, 2, 4, EXTENSTION_DROPOFF, SUB_SEQUENCE_LENGTH, ENCODER);		
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("ATGCATGACT").seqString());
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("ATGCATGACT").seqString());				
	}
	
	public void testExtend11() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ATGCATGACTA"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ATGCATGACTA"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 2, 4, encoded_2, 2, 4, EXTENSTION_DROPOFF, SUB_SEQUENCE_LENGTH, ENCODER);		
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("ATGCATGACTA").seqString());				
	}
			
	public void testExtendFour() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTT"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTT"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 2, 4, encoded_2, 2, 4, EXTENSTION_DROPOFF, SUB_SEQUENCE_LENGTH, ENCODER);		
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTT").seqString());
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTT").seqString());				
	}
	
	public void testExtendFive() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTTTGATCA"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTTTGATCA"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 2, 4, encoded_2, 2, 4, EXTENSTION_DROPOFF, SUB_SEQUENCE_LENGTH, ENCODER);		
		assertEquals(extension.getQuerySequenceExtended(), LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTTTGATCA").seqString());
		assertEquals(extension.getTargetSequenceExtended(), LightweightSymbolList.createDNA("ACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTACGTTTGATCA").seqString());
	}
	
	public void testExtend11Diff() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("ATGCATGACTA"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("GTGCATGACTC"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 2, 4, encoded_2, 2, 4, EXTENSTION_DROPOFF, SUB_SEQUENCE_LENGTH, ENCODER);		
		assertEquals(LightweightSymbolList.createDNA("TGCATGACT").seqString(), extension.getTargetSequenceExtended());				
		assertEquals(LightweightSymbolList.createDNA("TGCATGACT").seqString(), extension.getQuerySequenceExtended());
	}
	
	
	public void testExtend11DiffMiddle() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("TAAGCATGCATGACTAGGTA"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("GCCAGGTGCATGACTCCCGA"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 6, 11, encoded_2, 6, 11, EXTENSTION_DROPOFF, SUB_SEQUENCE_LENGTH, ENCODER);		
		assertEquals(LightweightSymbolList.createDNA("TGCATGACT").seqString(), extension.getTargetSequenceExtended());
		assertEquals(LightweightSymbolList.createDNA("TGCATGACT").seqString(), extension.getQuerySequenceExtended());
	}
	
	public void testExtend11DiffMiddleSing() throws Exception {		
		int[] encoded_1 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("GGGGGATGATTGTAGTTTATGAGTATGGATGACAGTAGCATTATATATAGATAGACACCGGAACCCCC"));
		int[] encoded_2 = ENCODER.encodeSymbolListToIntegerArray(LightweightSymbolList.createDNA("CCCCCATGATTGTAGTTTATGAGTATGGACCAGCGTAGTATTATATATAGATAGACATGACCGGGACCCCC"));
		
		ExtendSequences extension = ExtendSequences.doExtension(encoded_1, 8, 25, encoded_2, 8, 25, EXTENSTION_DROPOFF, SUB_SEQUENCE_LENGTH, ENCODER);
	
		assertEquals(LightweightSymbolList.createDNA("ATGATTGTAGTTTATGAGTATGGACCAGCGTAGTATTATATATAGATAGACA").seqString(), extension.getTargetSequenceExtended());
		assertEquals(LightweightSymbolList.createDNA("ATGATTGTAGTTTATGAGTATGGATGACAGTAGCATTATATATAGATAGACA").seqString(), extension.getQuerySequenceExtended());
	}
}
