package bio.pih.tests.compressor;

import junit.framework.TestCase;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.junit.Test;

import bio.pih.encoder.DNASequenceEncoderToShort;
import bio.pih.encoder.SequenceEncoder;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.seq.LightweightSymbolList;

/**
 * Test the encoding and decoding from {@link DNASequenceCompressorToShort}
 * 
 * @author albrecht
 */
public class DNASequenceCompressorToShortTest extends TestCase {

	/**
	 * Test the {@link DNASequenceCompressorToShort} encoding process 
	 * @throws ValueOutOfBoundsException
	 * @throws IllegalSymbolException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testEncodeDNASubSymbolList() throws ValueOutOfBoundsException, IllegalSymbolException {
		DNASequenceEncoderToShort compressor = new DNASequenceEncoderToShort(8);
		
		String stringSequence = "TCGGACTG"; // 1101101000011110
		SymbolList symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals((short) Integer.parseInt("1101101000011110", 2), compressor.encodeSubSymbolListToShort(symbolList));

		stringSequence = "AACAACAA"; // 0000010000010000
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals((short) Integer.parseInt("0000010000010000", 2), compressor.encodeSubSymbolListToShort(symbolList));

		stringSequence = "CCCCCCCC"; // 0101010101010101
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals((short) Integer.parseInt("0101010101010101", 2), compressor.encodeSubSymbolListToShort(symbolList));

		stringSequence = "TTTTTTTT"; // 1111111111111111
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals((short) Integer.parseInt("1111111111111111", 2), compressor.encodeSubSymbolListToShort(symbolList));

		stringSequence = "ACTGGTCA"; // 0001111010110100
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals((short) Integer.parseInt("0001111010110100", 2), compressor.encodeSubSymbolListToShort(symbolList));

		stringSequence = "ATTTTTTT"; // 001111111111111
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals((short) Integer.parseInt("0011111111111111", 2), compressor.encodeSubSymbolListToShort(symbolList));

		stringSequence = "TCTAGCCA"; // 1101110010010100
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals((short) Integer.parseInt("1101110010010100", 2), compressor.encodeSubSymbolListToShort(symbolList));
	}

	/**
	 * The the decoding {@link DNASequenceCompressorToShort} process 
	 * @throws ValueOutOfBoundsException
	 * @throws IllegalSymbolException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testDecodeToStringSubSequence() throws ValueOutOfBoundsException {
		DNASequenceEncoderToShort compressor = new DNASequenceEncoderToShort(8);

		// String stringSequence = "TCGGACTG"; // 1101101000011110
		String stringSequence = compressor.decodeShortToString((short) Integer.parseInt("1101101000011110", 2));
		assertEquals("TCGGACTG", stringSequence);

		// String stringSequence = "AACAACAA"; // 0000010000010000
		stringSequence = compressor.decodeShortToString((short) Integer.parseInt("0000010000010000", 2));
		assertEquals("AACAACAA", stringSequence);

		// stringSequence = "CCCCCCCC"; // 0101010101010101
		stringSequence = compressor.decodeShortToString((short) Integer.parseInt("0101010101010101", 2));
		assertEquals("CCCCCCCC", stringSequence);

		// stringSequence = "TTTTTTTT"; // 1111111111111111
		stringSequence = compressor.decodeShortToString((short) Integer.parseInt("1111111111111111", 2));
		assertEquals("TTTTTTTT", stringSequence);

		// stringSequence = "ACTGGTCA"; // 0001111010110100
		stringSequence = compressor.decodeShortToString((short) Integer.parseInt("0001111010110100", 2));
		assertEquals("ACTGGTCA", stringSequence);

		// stringSequence = "ATTTTTTT"; // 0011111111111111
		stringSequence = compressor.decodeShortToString((short) Integer.parseInt("0011111111111111", 2));
		assertEquals("ATTTTTTT", stringSequence);

		// stringSequence = "TCTAGCCA"; // 1101110010010100
		stringSequence = compressor.decodeShortToString((short) Integer.parseInt("1101110010010100", 2));
		assertEquals("TCTAGCCA", stringSequence);
	}

	/**
	 * Test the encode and decode of the {@link DNASequenceCompressorToShort}
	 * @throws ValueOutOfBoundsException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws BioException
	 */
	@Test
	public void testEncodedAndDecodeToShortSubSequence() throws ValueOutOfBoundsException, BioException {
		DNASequenceEncoderToShort compressor = new DNASequenceEncoderToShort(8);

		String stringSequence = "TCGGACTG"; // 1101101000011110
		SymbolList symbolList = LightweightSymbolList.createDNA(stringSequence);
		short encoded = compressor.encodeSubSymbolListToShort(symbolList);
		assertEquals(symbolList, compressor.decodeShortToSymbolList(encoded));

		stringSequence = "AACAACAA"; // 0000010000010000
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = compressor.encodeSubSymbolListToShort(symbolList);
		assertEquals(symbolList, compressor.decodeShortToSymbolList(encoded));

		stringSequence = "CCCCCCCC"; // 0101010101010101
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = compressor.encodeSubSymbolListToShort(symbolList);
		assertEquals(symbolList, compressor.decodeShortToSymbolList(encoded));

		stringSequence = "TTTTTTTT"; // 1111111111111111
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = compressor.encodeSubSymbolListToShort(symbolList);
		assertEquals(symbolList, compressor.decodeShortToSymbolList(encoded));

		stringSequence = "ACTGGTCA"; // 0001111010110100
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = compressor.encodeSubSymbolListToShort(symbolList);
		assertEquals(symbolList, compressor.decodeShortToSymbolList(encoded));

		stringSequence = "ATTTTTTT"; // 0011111111111111
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = compressor.encodeSubSymbolListToShort(symbolList);
		assertEquals(symbolList, compressor.decodeShortToSymbolList(encoded));

		stringSequence = "TCTAGCCA"; // 1101110010010100
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = compressor.encodeSubSymbolListToShort(symbolList);
		assertEquals(symbolList, compressor.decodeShortToSymbolList(encoded));
	}

	/**
	 * Test the sequence encoding of {@link DNASequenceCompressorToShort}
	 * @throws ValueOutOfBoundsException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws BioException
	 */
	@Test
	public void testDecodeToIntegerSequence() throws ValueOutOfBoundsException, BioException {
		DNASequenceEncoderToShort compressor = new DNASequenceEncoderToShort(8);

		SymbolList createDNA = LightweightSymbolList.createDNA("TCTAGCCAATTTTTTTACTGGTCATTTTTTTTCCCCCCCCAACAACAATCGGACTG");		                                                        
		short[] encodeSequenceToShort = compressor.encodeSymbolListToShortArray(createDNA);
		assertEquals((short) Integer.parseInt("1101110010010100", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()]);
		assertEquals((short) Integer.parseInt("0011111111111111", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()+1]);
		assertEquals((short) Integer.parseInt("0001111010110100", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()+2]);
		assertEquals((short) Integer.parseInt("1111111111111111", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()+3]);
		assertEquals((short) Integer.parseInt("0101010101010101", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()+4]);
		assertEquals((short) Integer.parseInt("0000010000010000", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()+5]);
		assertEquals((short) Integer.parseInt("1101101000011110", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()+6]);
		assertEquals( createDNA.length(), encodeSequenceToShort[SequenceEncoder.getPositionLength()]);
		assertEquals( createDNA, compressor.decodeShortArrayToSymbolList(encodeSequenceToShort));
		

		createDNA = LightweightSymbolList.createDNA("TCTAGC");
		encodeSequenceToShort = compressor.encodeSymbolListToShortArray(createDNA);
		assertEquals((short) Integer.parseInt("1101110010010000", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()]);
		assertEquals(createDNA.length(), encodeSequenceToShort[SequenceEncoder.getPositionLength()]);
		assertEquals(createDNA, compressor.decodeShortArrayToSymbolList(encodeSequenceToShort));

		createDNA = LightweightSymbolList.createDNA("TTTTACTGGTC");
		encodeSequenceToShort = compressor.encodeSymbolListToShortArray(createDNA);
		assertEquals((short) Integer.parseInt("1111111100011110", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()]);
		assertEquals((short) Integer.parseInt("1011010000000000", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()+1]);
		assertEquals(createDNA.length(), encodeSequenceToShort[SequenceEncoder.getPositionLength()]);
		assertEquals(createDNA, compressor.decodeShortArrayToSymbolList(encodeSequenceToShort));

		createDNA = LightweightSymbolList.createDNA("AAACACTA" + // 0000000100011100
				"GCTACGTC" + // 1001110001101101
				"GAATAGCA" + // 1000001100100100
				"ACTGAGAT" + // 0001111000100011
				"GCATGAGC" + // 1001001110001001
				"ACAACTG"); //  0001000001111000
		encodeSequenceToShort = compressor.encodeSymbolListToShortArray(createDNA);
		assertEquals((short) Integer.parseInt("0000000100011100", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()]);
		assertEquals((short) Integer.parseInt("1001110001101101", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()+1]);
		assertEquals((short) Integer.parseInt("1000001100100100", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()+2]);
		assertEquals((short) Integer.parseInt("0001111000100011", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()+3]);
		assertEquals((short) Integer.parseInt("1001001110001001", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()+4]);
		assertEquals((short) Integer.parseInt("0001000001111000", 2), encodeSequenceToShort[SequenceEncoder.getPositionBeginBitsVector()+5]);
		assertEquals(createDNA.length(), encodeSequenceToShort[SequenceEncoder.getPositionLength()]);
		assertEquals(createDNA, compressor.decodeShortArrayToSymbolList(encodeSequenceToShort));
	}
}
