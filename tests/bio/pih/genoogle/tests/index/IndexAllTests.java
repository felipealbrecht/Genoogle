/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.tests.index;

import junit.framework.TestSuite;

import org.junit.Test;

/**
 * @author albrecht
 */
public class IndexAllTests extends TestSuite {

	/**
	 * @return all tests for index
	 */
	@Test
	public static TestSuite suite() {
		TestSuite suite = new TestSuite("IndexAllTests");
		
		suite.addTestSuite(SubSequencesArrayIndexTest.class);
		suite.addTestSuite(InvertedIndexBuilderTest.class);

		return suite;
	}
	
}
