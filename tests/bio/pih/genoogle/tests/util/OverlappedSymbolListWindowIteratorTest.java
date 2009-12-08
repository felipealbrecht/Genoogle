/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.tests.util;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.util.SymbolListWindowIterator;
import bio.pih.genoogle.util.SymbolListWindowIteratorFactory;

/**
 * @author albrecht
 *
 */
public class OverlappedSymbolListWindowIteratorTest extends TestCase {
	SymbolListWindowIteratorFactory factory;
	
	@Override
	@BeforeClass
	public void setUp() {
		factory = SymbolListWindowIteratorFactory.getOverlappedFactory(); 
	}
	
	@Override
	@AfterClass
	public void tearDown() {
		factory = null;
	}
	
	@Test
	public void testOverlapedSequenceWindowIterator() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGCCGGA");
		SymbolListWindowIterator iterator = factory.newSymbolListWindowIterator(dna, 3);
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("ACT"));
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("CTG"));
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("TGC"));
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("GCC"));
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("CCG"));
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("CGG"));
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("GGA"));
		assertTrue(!iterator.hasNext());
	}
	
	@Test
	public void testWrongWindowsOverlapedSequenceWindowIterator() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGCCGGA");
		SymbolListWindowIterator iterator = factory.newSymbolListWindowIterator(dna, 3);
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next(), LightweightSymbolList.createDNA("T"));
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next(), LightweightSymbolList.createDNA("CCC"));
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next(), LightweightSymbolList.createDNA("AAAAA"));
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next(), LightweightSymbolList.createDNA("GC"));
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next(), LightweightSymbolList.createDNA("CAAAATTTCG"));
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next(), LightweightSymbolList.createDNA("CGGCCC"));
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next(), LightweightSymbolList.createDNA("GGATTT"));
		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testSameSizeWindow() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGCCGGA");
		SymbolListWindowIterator iterator = factory.newSymbolListWindowIterator(dna, 9);
		assertTrue(iterator.hasNext() == true);
		assertEquals(iterator.next(), LightweightSymbolList.createDNA("ACTGCCGGA"));
		assertTrue(!iterator.hasNext());
	}

	@Test
	public void testLongerSizeWindow() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGG");
		SymbolListWindowIterator iterator = factory.newSymbolListWindowIterator(dna, 9);
		assertTrue(!iterator.hasNext());
	}
	
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
