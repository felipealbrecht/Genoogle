package bio.pih.tests.encoder;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;

import bio.pih.encoder.SequenceEncoder;
import bio.pih.index.ValueOutOfBoundsException;

/**
 * Test some statics methods from {@link SequenceEncoder}
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
		assertEquals(1, SequenceEncoder.bitsByAlphabetSize(1));
		assertEquals(1, SequenceEncoder.bitsByAlphabetSize(2));
		assertEquals(2, SequenceEncoder.bitsByAlphabetSize(3));
		assertEquals(2, SequenceEncoder.bitsByAlphabetSize(4));
		assertEquals(3, SequenceEncoder.bitsByAlphabetSize(5));
		assertEquals(3, SequenceEncoder.bitsByAlphabetSize(7));
		assertEquals(3, SequenceEncoder.bitsByAlphabetSize(8));
		assertEquals(4, SequenceEncoder.bitsByAlphabetSize(15));
		assertEquals(5, SequenceEncoder.bitsByAlphabetSize(31));
		assertEquals(5, SequenceEncoder.bitsByAlphabetSize(32));
		assertEquals(6, SequenceEncoder.bitsByAlphabetSize(33));
		assertEquals(6, SequenceEncoder.bitsByAlphabetSize(64));
		assertEquals(7, SequenceEncoder.bitsByAlphabetSize(66));
		assertEquals(7, SequenceEncoder.bitsByAlphabetSize(100));
		assertEquals(7, SequenceEncoder.bitsByAlphabetSize(128));
		assertEquals(8, SequenceEncoder.bitsByAlphabetSize(200));
		assertEquals(8, SequenceEncoder.bitsByAlphabetSize(255));
		assertEquals(8, SequenceEncoder.bitsByAlphabetSize(256));
	}

	/**
	 * @throws ValueOutOfBoundsException
	 */
	@Test
	public void testGetClassFromSize() throws ValueOutOfBoundsException {
		assertEquals(Byte.class, SequenceEncoder.getClassFromSize(1));
		assertEquals(Byte.class, SequenceEncoder.getClassFromSize(7));
		assertEquals(Byte.class, SequenceEncoder.getClassFromSize(8));
		assertEquals(Short.class, SequenceEncoder.getClassFromSize(9));
		assertEquals(Short.class, SequenceEncoder.getClassFromSize(15));
		assertEquals(Short.class, SequenceEncoder.getClassFromSize(16));
		assertEquals(Integer.class, SequenceEncoder.getClassFromSize(17));
		assertEquals(Integer.class, SequenceEncoder.getClassFromSize(31));
		assertEquals(Integer.class, SequenceEncoder.getClassFromSize(32));
		assertEquals(Long.class, SequenceEncoder.getClassFromSize(33));
		assertEquals(Long.class, SequenceEncoder.getClassFromSize(63));
		assertEquals(Long.class, SequenceEncoder.getClassFromSize(64));
	}

}