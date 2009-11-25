package bio.pih.genoogle.tests.encoder;

import junit.framework.TestCase;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.junit.Test;

import bio.pih.genoogle.encoder.DNASequenceEncoderToInteger;
import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.seq.LightweightSymbolList;

/**
 * Test the encoding and decoding from {@link DNASequenceCompressorToInteger}
 * 
 * @author albrecht
 */
public class DNASequenceEncoderToIntegerTest extends TestCase {

	/**
	 * Test the {@link DNASequenceCompressorToInteger} encoding process 
	 * @throws ValueOutOfBoundsException
	 * @throws IllegalSymbolException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testEncodeDNASubSymbolList() throws ValueOutOfBoundsException, IllegalSymbolException {
		DNASequenceEncoderToInteger encoder = DNASequenceEncoderToInteger.getEncoder(8);
		
		String stringSequence = "TCGGACTG"; // 1101101000011110
		SymbolList symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals(Integer.parseInt("1101101000011110", 2), encoder.encodeSubSequenceToInteger(symbolList));

		stringSequence = "AACAACAA"; // 0000010000010000
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals(Integer.parseInt("0000010000010000", 2), encoder.encodeSubSequenceToInteger(symbolList));

		stringSequence = "CCCCCCCC"; // 0101010101010101
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals(Integer.parseInt("0101010101010101", 2), encoder.encodeSubSequenceToInteger(symbolList));

		stringSequence = "TTTTTTTT"; // 1111111111111111
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals(Integer.parseInt("1111111111111111", 2), encoder.encodeSubSequenceToInteger(symbolList));

		stringSequence = "ACTGGTCA"; // 0001111010110100
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals(Integer.parseInt("0001111010110100", 2), encoder.encodeSubSequenceToInteger(symbolList));

		stringSequence = "ATTTTTTT"; // 001111111111111
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals(Integer.parseInt("0011111111111111", 2), encoder.encodeSubSequenceToInteger(symbolList));

		stringSequence = "TCTAGCCA"; // 1101110010010100
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals(Integer.parseInt("1101110010010100", 2), encoder.encodeSubSequenceToInteger(symbolList));
	}

	/**
	 * The the decoding {@link DNASequenceCompressorToInteger} process 
	 * @throws ValueOutOfBoundsException
	 * @throws IllegalSymbolException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testDecodeToStringSubSequence() throws ValueOutOfBoundsException {
		DNASequenceEncoderToInteger encoder = DNASequenceEncoderToInteger.getEncoder(8);

		// String stringSequence = "TCGGACTG"; // 1101101000011110
		String stringSequence = encoder.decodeIntegerToString(Integer.parseInt("1101101000011110", 2));
		assertEquals("TCGGACTG", stringSequence);

		// String stringSequence = "AACAACAA"; // 0000010000010000
		stringSequence = encoder.decodeIntegerToString(Integer.parseInt("0000010000010000", 2));
		assertEquals("AACAACAA", stringSequence);

		// stringSequence = "CCCCCCCC"; // 0101010101010101
		stringSequence = encoder.decodeIntegerToString(Integer.parseInt("0101010101010101", 2));
		assertEquals("CCCCCCCC", stringSequence);

		// stringSequence = "TTTTTTTT"; // 1111111111111111
		stringSequence = encoder.decodeIntegerToString(Integer.parseInt("1111111111111111", 2));
		assertEquals("TTTTTTTT", stringSequence);

		// stringSequence = "ACTGGTCA"; // 0001111010110100
		stringSequence = encoder.decodeIntegerToString(Integer.parseInt("0001111010110100", 2));
		assertEquals("ACTGGTCA", stringSequence);

		// stringSequence = "ATTTTTTT"; // 0011111111111111
		stringSequence = encoder.decodeIntegerToString(Integer.parseInt("0011111111111111", 2));
		assertEquals("ATTTTTTT", stringSequence);

		// stringSequence = "TCTAGCCA"; // 1101110010010100
		stringSequence = encoder.decodeIntegerToString(Integer.parseInt("1101110010010100", 2));
		assertEquals("TCTAGCCA", stringSequence);
	}

	/**
	 * Test the encode and decode of the {@link DNASequenceCompressorToInteger}
	 * @throws ValueOutOfBoundsException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws BioException
	 */
	@Test
	public void testEncodedAndDecodeToIntegerSubSequence() throws ValueOutOfBoundsException, BioException {
		DNASequenceEncoderToInteger encoder = DNASequenceEncoderToInteger.getEncoder(8);

		String stringSequence = "TCGGACTG"; // 1101101000011110
		SymbolList symbolList = LightweightSymbolList.createDNA(stringSequence);
		int encoded = encoder.encodeSubSequenceToInteger(symbolList);
		assertEquals(symbolList.seqString(), encoder.decodeIntegerToString(encoded));

		stringSequence = "AACAACAA"; // 0000010000010000
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = encoder.encodeSubSequenceToInteger(symbolList);
		assertEquals(symbolList.seqString(), encoder.decodeIntegerToString(encoded));

		stringSequence = "CCCCCCCC"; // 0101010101010101
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = encoder.encodeSubSequenceToInteger(symbolList);
		assertEquals(symbolList.seqString(), encoder.decodeIntegerToString(encoded));

		stringSequence = "TTTTTTTT"; // 1111111111111111
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = encoder.encodeSubSequenceToInteger(symbolList);
		assertEquals(symbolList.seqString(), encoder.decodeIntegerToString(encoded));

		stringSequence = "ACTGGTCA"; // 0001111010110100
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = encoder.encodeSubSequenceToInteger(symbolList);
		assertEquals(symbolList.seqString(), encoder.decodeIntegerToString(encoded));

		stringSequence = "ATTTTTTT"; // 0011111111111111
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = encoder.encodeSubSequenceToInteger(symbolList);
		assertEquals(symbolList.seqString(), encoder.decodeIntegerToString(encoded));

		stringSequence = "TCTAGCCA"; // 1101110010010100
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = encoder.encodeSubSequenceToInteger(symbolList);
		assertEquals(symbolList.seqString(), encoder.decodeIntegerToString(encoded));
		
		stringSequence = "TCTAGCAA"; // 1101110010010000
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = encoder.encodeSubSequenceToInteger(symbolList);
		assertEquals(symbolList.seqString(), encoder.decodeIntegerToString(encoded));
	}

	/**
	 * Test the sequence encoding of {@link DNASequenceCompressorToInteger}
	 * @throws ValueOutOfBoundsException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws BioException
	 */
	@Test
	public void testDecodeToIntegerSequence() throws ValueOutOfBoundsException, BioException {
		DNASequenceEncoderToInteger encoder = DNASequenceEncoderToInteger.getEncoder(8);

		SymbolList createDNA = LightweightSymbolList.createDNA("TCTAGCCAATTTTTTTACTGGTCATTTTTTTTCCCCCCCCAACAACAATCGGACTG");		                                                        
		int[] encodeSequenceToInteger = encoder.encodeSymbolListToIntegerArray(createDNA);
		assertEquals(Integer.parseInt("1101110010010100", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()]);
		assertEquals(Integer.parseInt("0011111111111111", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()+1]);
		assertEquals(Integer.parseInt("0001111010110100", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()+2]);
		assertEquals(Integer.parseInt("1111111111111111", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()+3]);
		assertEquals(Integer.parseInt("0101010101010101", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()+4]);
		assertEquals(Integer.parseInt("0000010000010000", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()+5]);
		assertEquals(Integer.parseInt("1101101000011110", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()+6]);
		assertEquals( createDNA.length(), encodeSequenceToInteger[SequenceEncoder.getPositionLength()]);
		assertEquals( createDNA.seqString(), encoder.decodeIntegerArrayToString(encodeSequenceToInteger));
		

		createDNA = LightweightSymbolList.createDNA("TCTAGC");
		encodeSequenceToInteger = encoder.encodeSymbolListToIntegerArray(createDNA);
		assertEquals(Integer.parseInt("1101110010010000", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()]);
		assertEquals(createDNA.length(), encodeSequenceToInteger[SequenceEncoder.getPositionLength()]);
		assertEquals(createDNA.seqString(), encoder.decodeIntegerArrayToString(encodeSequenceToInteger));

		createDNA = LightweightSymbolList.createDNA("TTTTACTGGTC");
		encodeSequenceToInteger = encoder.encodeSymbolListToIntegerArray(createDNA);
		assertEquals(Integer.parseInt("1111111100011110", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()]);
		assertEquals(Integer.parseInt("1011010000000000", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()+1]);
		assertEquals(createDNA.length(), encodeSequenceToInteger[SequenceEncoder.getPositionLength()]);
		assertEquals(createDNA.seqString(), encoder.decodeIntegerArrayToString(encodeSequenceToInteger));

		createDNA = LightweightSymbolList.createDNA("AAACACTA" + // 0000000100011100
				"GCTACGTC" + // 1001110001101101
				"GAATAGCA" + // 1000001100100100
				"ACTGAGAT" + // 0001111000100011
				"GCATGAGC" + // 1001001110001001
				"ACAACTG"); //  0001000001111000
		encodeSequenceToInteger = encoder.encodeSymbolListToIntegerArray(createDNA);
		assertEquals(Integer.parseInt("0000000100011100", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()]);
		assertEquals(Integer.parseInt("1001110001101101", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()+1]);
		assertEquals(Integer.parseInt("1000001100100100", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()+2]);
		assertEquals(Integer.parseInt("0001111000100011", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()+3]);
		assertEquals(Integer.parseInt("1001001110001001", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()+4]);
		assertEquals(Integer.parseInt("0001000001111000", 2), encodeSequenceToInteger[SequenceEncoder.getPositionBeginBitsVector()+5]);
		assertEquals(createDNA.length(), encodeSequenceToInteger[SequenceEncoder.getPositionLength()]);
		assertEquals(createDNA.seqString(), encoder.decodeIntegerArrayToString(encodeSequenceToInteger));
	}
}
