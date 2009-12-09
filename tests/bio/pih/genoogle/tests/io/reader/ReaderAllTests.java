/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.tests.io.reader;

import junit.framework.TestSuite;

import org.junit.Test;

import bio.pih.genoogle.seq.LightweightSymbolList;

/**
 * @author albrecht
 *
 */
public class ReaderAllTests extends TestSuite {

	
	/**
	 * @return all tests for sequences, related with {@link LightweightSymbolList}
	 */
	@Test
	public static TestSuite suite() {
		TestSuite suite = new TestSuite("SequencesAllTests");
		
		suite.addTestSuite(RichSequenceFastaFileReaderTest.class);

		return suite;
	}
}
