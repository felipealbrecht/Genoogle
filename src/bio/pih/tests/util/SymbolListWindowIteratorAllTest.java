package bio.pih.tests.util;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author albrecht
 *
 */
public class SymbolListWindowIteratorAllTest extends TestSuite {

	
	@org.junit.Test
	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		
		suite.addTestSuite(OverlappedSymbolListWindowIteratorTest.class);
		suite.addTestSuite(NotOverlappedSymbolListWindowIteratorTest.class);
		
		
		// $JUnit-BEGIN$

		// $JUnit-END$
		return suite;
	}
}
