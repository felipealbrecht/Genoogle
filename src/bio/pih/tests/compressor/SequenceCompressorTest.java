package bio.pih.tests.compressor;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;

import bio.pih.compressor.SequenceCompressor;
import bio.pih.index.ValueOutOfBoundsException;

/**
 * Test some statics methods from {@link SequenceCompressor}
 * 
 * @author albrecht
 */
public class SequenceCompressorTest extends TestCase {

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
		assertEquals(1, SequenceCompressor.bitsByAlphabetSize(1));
		assertEquals(1, SequenceCompressor.bitsByAlphabetSize(2));
		assertEquals(2, SequenceCompressor.bitsByAlphabetSize(3));
		assertEquals(2, SequenceCompressor.bitsByAlphabetSize(4));
		assertEquals(3, SequenceCompressor.bitsByAlphabetSize(5));
		assertEquals(3, SequenceCompressor.bitsByAlphabetSize(7));
		assertEquals(3, SequenceCompressor.bitsByAlphabetSize(8));
		assertEquals(4, SequenceCompressor.bitsByAlphabetSize(15));
		assertEquals(5, SequenceCompressor.bitsByAlphabetSize(31));
		assertEquals(5, SequenceCompressor.bitsByAlphabetSize(32));
		assertEquals(6, SequenceCompressor.bitsByAlphabetSize(33));
		assertEquals(6, SequenceCompressor.bitsByAlphabetSize(64));
		assertEquals(7, SequenceCompressor.bitsByAlphabetSize(66));
		assertEquals(7, SequenceCompressor.bitsByAlphabetSize(100));
		assertEquals(7, SequenceCompressor.bitsByAlphabetSize(128));
		assertEquals(8, SequenceCompressor.bitsByAlphabetSize(200));
		assertEquals(8, SequenceCompressor.bitsByAlphabetSize(255));
		assertEquals(8, SequenceCompressor.bitsByAlphabetSize(256));
	}

	/**
	 * @throws ValueOutOfBoundsException
	 */
	@Test
	public void testGetClassFromSize() throws ValueOutOfBoundsException {
		assertEquals(Byte.class, SequenceCompressor.getClassFromSize(1));
		assertEquals(Byte.class, SequenceCompressor.getClassFromSize(7));
		assertEquals(Byte.class, SequenceCompressor.getClassFromSize(8));
		assertEquals(Short.class, SequenceCompressor.getClassFromSize(9));
		assertEquals(Short.class, SequenceCompressor.getClassFromSize(15));
		assertEquals(Short.class, SequenceCompressor.getClassFromSize(16));
		assertEquals(Integer.class, SequenceCompressor.getClassFromSize(17));
		assertEquals(Integer.class, SequenceCompressor.getClassFromSize(31));
		assertEquals(Integer.class, SequenceCompressor.getClassFromSize(32));
		assertEquals(Long.class, SequenceCompressor.getClassFromSize(33));
		assertEquals(Long.class, SequenceCompressor.getClassFromSize(63));
		assertEquals(Long.class, SequenceCompressor.getClassFromSize(64));
	}

}
