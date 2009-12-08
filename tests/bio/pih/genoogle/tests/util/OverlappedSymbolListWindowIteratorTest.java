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
		assertEquals(iterator.next().seqString(), LightweightSymbolList.createDNA("ACT").seqString());
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next().seqString(), LightweightSymbolList.createDNA("CTG").seqString());
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next().seqString(), LightweightSymbolList.createDNA("TGC").seqString());
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next().seqString(), LightweightSymbolList.createDNA("GCC").seqString());
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next().seqString(), LightweightSymbolList.createDNA("CCG").seqString());
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next().seqString(), LightweightSymbolList.createDNA("CGG").seqString());
		assertTrue(iterator.hasNext());
		assertEquals(iterator.next().seqString(), LightweightSymbolList.createDNA("GGA").seqString());
		assertFalse(iterator.hasNext());
	}
	
	@Test
	public void testWrongWindowsOverlapedSequenceWindowIterator() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGCCGGA");
		SymbolListWindowIterator iterator = factory.newSymbolListWindowIterator(dna, 3);
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next().seqString(), LightweightSymbolList.createDNA("T").seqString());
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next().seqString(), LightweightSymbolList.createDNA("CCC").seqString());
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next().seqString(), LightweightSymbolList.createDNA("AAAAA").seqString());
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next().seqString(), LightweightSymbolList.createDNA("GC").seqString());
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next().seqString(), LightweightSymbolList.createDNA("CAAAATTTCG").seqString());
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next().seqString(), LightweightSymbolList.createDNA("CGGCCC").seqString());
		assertTrue(iterator.hasNext());
		assertNotSame(iterator.next().seqString(), LightweightSymbolList.createDNA("GGATTT").seqString());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testSameSizeWindow() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGCCGGA");
		SymbolListWindowIterator iterator = factory.newSymbolListWindowIterator(dna, 9);
		assertTrue(iterator.hasNext() == true);
		assertEquals(iterator.next().seqString(), LightweightSymbolList.createDNA("ACTGCCGGA").seqString());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testLongerSizeWindow() throws IllegalSymbolException {
		SymbolList dna = LightweightSymbolList.createDNA("ACTGG");
		SymbolListWindowIterator iterator = factory.newSymbolListWindowIterator(dna, 9);
		assertFalse(iterator.hasNext());
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
