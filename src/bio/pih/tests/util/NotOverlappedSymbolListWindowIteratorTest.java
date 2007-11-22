package bio.pih.tests.util;

import junit.framework.TestCase;

import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.junit.Test;

import bio.pih.seq.LightweightSymbolList;
import bio.pih.util.NotOverlappedSymbolListWindowIterator;
import bio.pih.util.OverlappedSymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIterator;

public class NotOverlappedSymbolListWindowIteratorTest extends TestCase {

	@Test
	public void testOverlapedSymbolListWindowIterator() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGCCGGAC");
		SymbolListWindowIterator iterator = new NotOverlappedSymbolListWindowIterator(dna, 3);
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("ACT"));
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("GCC"));
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("GGA"));
		assertTrue(!iterator.hasNext());
	}
	
	@Test
	public void testWrongWindowsOverlapedSymbolListWindowIterator() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGCCGGAC");
		SymbolListWindowIterator iterator = new NotOverlappedSymbolListWindowIterator(dna, 3);
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next(), LightweightSymbolList.createDNA("T"));
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next(), LightweightSymbolList.createDNA("TTTTGAC"));
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next(), LightweightSymbolList.createDNA("AAA"));
		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testSameSizeWindow() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGCCGGA");
		SymbolListWindowIterator iterator = new NotOverlappedSymbolListWindowIterator(dna, 9);
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("ACTGCCGGA"));
		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testLongerSizeWindow() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGG");
		SymbolListWindowIterator iterator = new NotOverlappedSymbolListWindowIterator(dna, 9);
		assertTrue(!iterator.hasNext());
	}

	@Test(expected = java.lang.IndexOutOfBoundsException.class)
	public void testWindowNegativeSize() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGG");
		try {
			new OverlappedSymbolListWindowIterator(dna, -1);
			fail("Expected: IndexOutOfBoundsException exception");
		} catch (IndexOutOfBoundsException iob) {	
		}
	}

}
