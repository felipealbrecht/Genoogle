package bio.pih.tests.index;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;

import bio.pih.index.SubSequecesArrayIndex;
import bio.pih.index.ValueOutOfBoundsException;

/**
 * Tests for the {@link SubSequecesArrayIndex}
 * 
 * TODO: tests that checks the limits
 * 
 * @author albrecht
 */
public class SchedulerTest extends TestCase {

	/**
	 * Test if the bits value from the alphabet size is correctly calculate.
	 * @throws ValueOutOfBoundsException 
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException 
	 */
	@Test
	public void testGetBitsBySize() throws ValueOutOfBoundsException {
		assertEquals(1, SubSequecesArrayIndex.bitsByAlphabetSize(1));
		assertEquals(1, SubSequecesArrayIndex.bitsByAlphabetSize(2));
		assertEquals(2, SubSequecesArrayIndex.bitsByAlphabetSize(3));
		assertEquals(2, SubSequecesArrayIndex.bitsByAlphabetSize(4));
		assertEquals(3, SubSequecesArrayIndex.bitsByAlphabetSize(5));
		assertEquals(3, SubSequecesArrayIndex.bitsByAlphabetSize(7));
		assertEquals(3, SubSequecesArrayIndex.bitsByAlphabetSize(8));
		assertEquals(4, SubSequecesArrayIndex.bitsByAlphabetSize(15));
		assertEquals(5, SubSequecesArrayIndex.bitsByAlphabetSize(31));
		assertEquals(5, SubSequecesArrayIndex.bitsByAlphabetSize(32));
		assertEquals(6, SubSequecesArrayIndex.bitsByAlphabetSize(33));
		assertEquals(6, SubSequecesArrayIndex.bitsByAlphabetSize(64));
		assertEquals(7, SubSequecesArrayIndex.bitsByAlphabetSize(66));
		assertEquals(7, SubSequecesArrayIndex.bitsByAlphabetSize(100));		
		assertEquals(7, SubSequecesArrayIndex.bitsByAlphabetSize(128));
		assertEquals(8, SubSequecesArrayIndex.bitsByAlphabetSize(200));
		assertEquals(8, SubSequecesArrayIndex.bitsByAlphabetSize(255));
		assertEquals(8, SubSequecesArrayIndex.bitsByAlphabetSize(256));
	}	
}
