package bio.pih.tests.index;

import java.io.IOException;

import junit.framework.TestCase;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.junit.Test;

import bio.pih.index.SubSequencesArrayIndex;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.seq.LightweightSymbolList;
import bio.pih.util.SymbolListWindowIteratorFactory;

/**
 * Tests for the {@link SubSequencesArrayIndex}
 * 
 * TODO: tests that checks the limits
 * 
 * @author albrecht
 */
public class SubSequencesArrayIndexTest extends TestCase {

	/**
	 * Test if the bits value from the alphabet size is correctly calculate.
	 * 
	 * @throws ValueOutOfBoundsException
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	@Test
	public void testGetBitsBySize() throws ValueOutOfBoundsException {
		assertEquals(1, SubSequencesArrayIndex.bitsByAlphabetSize(1));
		assertEquals(1, SubSequencesArrayIndex.bitsByAlphabetSize(2));
		assertEquals(2, SubSequencesArrayIndex.bitsByAlphabetSize(3));
		assertEquals(2, SubSequencesArrayIndex.bitsByAlphabetSize(4));
		assertEquals(3, SubSequencesArrayIndex.bitsByAlphabetSize(5));
		assertEquals(3, SubSequencesArrayIndex.bitsByAlphabetSize(7));
		assertEquals(3, SubSequencesArrayIndex.bitsByAlphabetSize(8));
		assertEquals(4, SubSequencesArrayIndex.bitsByAlphabetSize(15));
		assertEquals(5, SubSequencesArrayIndex.bitsByAlphabetSize(31));
		assertEquals(5, SubSequencesArrayIndex.bitsByAlphabetSize(32));
		assertEquals(6, SubSequencesArrayIndex.bitsByAlphabetSize(33));
		assertEquals(6, SubSequencesArrayIndex.bitsByAlphabetSize(64));
		assertEquals(7, SubSequencesArrayIndex.bitsByAlphabetSize(66));
		assertEquals(7, SubSequencesArrayIndex.bitsByAlphabetSize(100));
		assertEquals(7, SubSequencesArrayIndex.bitsByAlphabetSize(128));
		assertEquals(8, SubSequencesArrayIndex.bitsByAlphabetSize(200));
		assertEquals(8, SubSequencesArrayIndex.bitsByAlphabetSize(255));
		assertEquals(8, SubSequencesArrayIndex.bitsByAlphabetSize(256));
	}

	/**
	 * @throws ValueOutOfBoundsException
	 */
	@Test
	public void testGetClassFromSize() throws ValueOutOfBoundsException {
		assertEquals(Byte.class, SubSequencesArrayIndex.getClassFromSize(1));
		assertEquals(Byte.class, SubSequencesArrayIndex.getClassFromSize(7));
		assertEquals(Byte.class, SubSequencesArrayIndex.getClassFromSize(8));
		assertEquals(Short.class, SubSequencesArrayIndex.getClassFromSize(9));
		assertEquals(Short.class, SubSequencesArrayIndex.getClassFromSize(15));
		assertEquals(Short.class, SubSequencesArrayIndex.getClassFromSize(16));
		assertEquals(Integer.class, SubSequencesArrayIndex.getClassFromSize(17));
		assertEquals(Integer.class, SubSequencesArrayIndex.getClassFromSize(31));
		assertEquals(Integer.class, SubSequencesArrayIndex.getClassFromSize(32));
		assertEquals(Long.class, SubSequencesArrayIndex.getClassFromSize(33));
		assertEquals(Long.class, SubSequencesArrayIndex.getClassFromSize(63));
		assertEquals(Long.class, SubSequencesArrayIndex.getClassFromSize(64));
	}

	/**
	 * @throws ValueOutOfBoundsException
	 * @throws IllegalSymbolException
	 */
	@Test
	public void testCreateSubSequenceArrayIndex() throws ValueOutOfBoundsException, IllegalSymbolException {
		SymbolListWindowIteratorFactory symbolListWindowIteratorFactory = SymbolListWindowIteratorFactory.getNotOverlappedFactory();
		SubSequencesArrayIndex index = new SubSequencesArrayIndex(8, DNATools.getDNA(), symbolListWindowIteratorFactory);

		String stringSequence = "CATGACTGGCATCAGTGCATGCATGCAGTCAGTATATATGACGC";
		SymbolList symbolList = LightweightSymbolList.createDNA(stringSequence);
		SimpleSequence ss = new SimpleSequence(symbolList, null, "Simple Sequence", null);
		index.addSequence(ss);

	}

	/**
	 * @throws ValueOutOfBoundsException
	 * @throws IllegalSymbolException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testEncodeSubSequence() throws ValueOutOfBoundsException, IllegalSymbolException, InstantiationException, IllegalAccessException {
		SymbolListWindowIteratorFactory symbolListWindowIteratorFactory = SymbolListWindowIteratorFactory.getNotOverlappedFactory();
		SubSequencesArrayIndex index = new SubSequencesArrayIndex(8, DNATools.getDNA(), symbolListWindowIteratorFactory);

		String stringSequence = "TCGGACTG"; // 1101101000011110
		SymbolList symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals(Integer.parseInt("1101101000011110", 2), index.encodeToInt(symbolList));

		stringSequence = "AACAACAA"; // 0000010000010000
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals(Integer.parseInt("0000010000010000", 2), index.encodeToInt(symbolList));

		stringSequence = "CCCCCCCC"; // 0101010101010101
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals(Integer.parseInt("0101010101010101", 2), index.encodeToInt(symbolList));

		stringSequence = "TTTTTTTT"; // 1111111111111111
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals(Integer.parseInt("1111111111111111", 2), index.encodeToInt(symbolList));

		stringSequence = "ACTGGTCA"; // 0001111010110100
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals(Integer.parseInt("0001111010110100", 2), index.encodeToInt(symbolList));

		stringSequence = "ATTTTTTT"; // 001111111111111
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals(Integer.parseInt("0011111111111111", 2), index.encodeToInt(symbolList));

		stringSequence = "TCTAGCCA"; // 1101110010010100
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		assertEquals(Integer.parseInt("1101110010010100", 2), index.encodeToInt(symbolList));
	}

	@Test
	public void testDecodeToStringSubSequence() throws ValueOutOfBoundsException, IllegalSymbolException, InstantiationException, IllegalAccessException {
		SymbolListWindowIteratorFactory symbolListWindowIteratorFactory = SymbolListWindowIteratorFactory.getNotOverlappedFactory();
		SubSequencesArrayIndex index = new SubSequencesArrayIndex(8, DNATools.getDNA(), symbolListWindowIteratorFactory);

		// String stringSequence = "TCGGACTG"; // 1101101000011110
		String stringSequence = index.decodeToString(Integer.parseInt("1101101000011110", 2));
		assertEquals("TCGGACTG", stringSequence);

		// String stringSequence = "AACAACAA"; // 0000010000010000
		stringSequence = index.decodeToString(Integer.parseInt("0000010000010000", 2));
		assertEquals("AACAACAA", stringSequence);

		// stringSequence = "CCCCCCCC"; // 0101010101010101
		stringSequence = index.decodeToString(Integer.parseInt("0101010101010101", 2));
		assertEquals("CCCCCCCC", stringSequence);

		// stringSequence = "TTTTTTTT"; // 1111111111111111
		stringSequence = index.decodeToString(Integer.parseInt("1111111111111111", 2));
		assertEquals("TTTTTTTT", stringSequence);

		// stringSequence = "ACTGGTCA"; // 0001111010110100
		stringSequence = index.decodeToString(Integer.parseInt("0001111010110100", 2));
		assertEquals("ACTGGTCA", stringSequence);

		// stringSequence = "ATTTTTTT"; // 0011111111111111
		stringSequence = index.decodeToString(Integer.parseInt("0011111111111111", 2));
		assertEquals("ATTTTTTT", stringSequence);

		// stringSequence = "TCTAGCCA"; // 1101110010010100
		stringSequence = index.decodeToString(Integer.parseInt("1101110010010100", 2));
		assertEquals("TCTAGCCA", stringSequence);

		// LightweightSymbolList.createLightSymbolList(DNATools.getDNA(),
		// symbols);
	}

	@Test
	public void testDecodeToSequenceSubSequence() throws ValueOutOfBoundsException, InstantiationException, IllegalAccessException, BioException {
		SymbolListWindowIteratorFactory symbolListWindowIteratorFactory = SymbolListWindowIteratorFactory.getNotOverlappedFactory();
		SubSequencesArrayIndex index = new SubSequencesArrayIndex(8, DNATools.getDNA(), symbolListWindowIteratorFactory);

		String stringSequence = "TCGGACTG"; // 1101101000011110
		SymbolList symbolList = LightweightSymbolList.createDNA(stringSequence);
		int encoded = index.encodeToInt(symbolList);
		assertEquals(symbolList, index.decodeToSymbolList(encoded));

		stringSequence = "AACAACAA"; // 0000010000010000
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = index.encodeToInt(symbolList);
		assertEquals(symbolList, index.decodeToSymbolList(encoded));

		stringSequence = "CCCCCCCC"; // 0101010101010101
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = index.encodeToInt(symbolList);
		assertEquals(symbolList, index.decodeToSymbolList(encoded));

		stringSequence = "TTTTTTTT"; // 1111111111111111
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = index.encodeToInt(symbolList);
		assertEquals(symbolList, index.decodeToSymbolList(encoded));

		stringSequence = "ACTGGTCA"; // 0001111010110100
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = index.encodeToInt(symbolList);
		assertEquals(symbolList, index.decodeToSymbolList(encoded));

		stringSequence = "ATTTTTTT"; // 001111111111111
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = index.encodeToInt(symbolList);
		assertEquals(symbolList, index.decodeToSymbolList(encoded));

		stringSequence = "TCTAGCCA"; // 1101110010010100
		symbolList = LightweightSymbolList.createDNA(stringSequence);
		encoded = index.encodeToInt(symbolList);
		assertEquals(symbolList, index.decodeToSymbolList(encoded));
	}
}
