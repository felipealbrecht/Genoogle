/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.tests.encoder;

import junit.framework.TestCase;

import org.junit.Test;

import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.index.ValueOutOfBoundsException;

/**
 * Test some statics methods from {@link SequenceEncoder}
 * 
 * @author albrecht
 */
public class SequenceEncoderTest extends TestCase {

	/**
	 * Test if the bits value from the alphabet size is correctly calculate.
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
}
