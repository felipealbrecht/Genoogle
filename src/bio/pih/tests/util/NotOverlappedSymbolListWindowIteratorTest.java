package bio.pih.tests.util;

import junit.framework.TestCase;

import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import bio.pih.seq.LightweightSymbolList;
import bio.pih.util.SymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIteratorFactory;

/**
 * @author albrecht
 *
 */
public class NotOverlappedSymbolListWindowIteratorTest extends TestCase {

	SymbolListWindowIteratorFactory factory;
	
	@Override
	@BeforeClass
	public void setUp() {
		factory = SymbolListWindowIteratorFactory.getNotOverlappedFactory(); 
	}
	
	@Override
	@AfterClass
	public void tearDown() {
		factory = null;
	}
	
	/**
	 * @throws IllegalSymbolException
	 */
	@Test
	public void testNotOverlapedSymbolListWindowIterator() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGCCGGAC");
		SymbolListWindowIterator iterator = factory.newSymbolListWindowIterator(dna, 3);
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("ACT"));
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("GCC"));
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("GGA"));
		assertTrue(!iterator.hasNext());
	}
	
	/**
	 * @throws IllegalSymbolException
	 */
	@Test
	public void testWrongWindowsNotOverlapedSymbolListWindowIterator() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGCCGGAC");
		SymbolListWindowIterator iterator = factory.newSymbolListWindowIterator(dna, 3);
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next(), LightweightSymbolList.createDNA("T"));
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next(), LightweightSymbolList.createDNA("TTTTGAC"));
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next(), LightweightSymbolList.createDNA("AAA"));
		assertTrue(!iterator.hasNext());
	}

	/**
	 * @throws IllegalSymbolException
	 */
	@Test
	public void testSameSizeWindow() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGCCGGA");
		SymbolListWindowIterator iterator = factory.newSymbolListWindowIterator(dna, 9);
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("ACTGCCGGA"));
		assertTrue(!iterator.hasNext());
	}

	/**
	 * @throws IllegalSymbolException
	 */
	@Test
	public void testLongerSizeWindow() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGG");
		SymbolListWindowIterator iterator = factory.newSymbolListWindowIterator(dna, 9);
		assertTrue(!iterator.hasNext());
	}

	/**
	 * @throws IllegalSymbolException
	 */
	@Test(expected = java.lang.IndexOutOfBoundsException.class)
	public void testWindowNegativeSize() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGG");
		try {
			factory.newSymbolListWindowIterator(dna, -1);
			fail("Expected: IndexOutOfBoundsException exception");
		} catch (IndexOutOfBoundsException iob) {	
		}
	}

}
