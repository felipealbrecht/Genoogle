package bio.pih.tests.util;

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
