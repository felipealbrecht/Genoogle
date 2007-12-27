package bio.pih.tests.index;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;

import bio.pih.index.SubSequencesArrayIndex;
import bio.pih.index.ValueOutOfBoundsException;
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
	
	@Test
	public void testCreateSubSequenceArrayIndex() throws ValueOutOfBoundsException {
		SymbolListWindowIteratorFactory symbolListWindowIteratorFactory = SymbolListWindowIteratorFactory.getNotOverlappedFactory();
		SubSequencesArrayIndex index = new SubSequencesArrayIndex(8, 4, symbolListWindowIteratorFactory);
		
	}
}
