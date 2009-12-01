/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.tests.util;

import junit.framework.TestSuite;

/**
 * @author albrecht
 *
 */
public class SymbolListWindowIteratorAllTests extends TestSuite {

	
	/**
	 * @return all tests from symbol list window iterator
	 */
	@org.junit.Test
	public static TestSuite suite() {
		TestSuite suite = new TestSuite("SymbolListWindowIteratorAllTest");
		
		suite.addTestSuite(OverlappedSymbolListWindowIteratorTest.class);
		suite.addTestSuite(NotOverlappedSymbolListWindowIteratorTest.class);

		return suite;
	}
}
