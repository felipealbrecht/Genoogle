package bio.pih.tests.util;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author albrecht
 *
 */
public class SymbolListWindowIteratorAllTest {

	/**
	 * @return
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for bio.pih.tests.util");
		
		suite.addTestSuite(OverlappedSymbolListWindowIteratorTest.class);
		suite.addTestSuite(NotOverlappedSymbolListWindowIteratorTest.class);
		
		
		// $JUnit-BEGIN$

		// $JUnit-END$
		return suite;
	}
}
