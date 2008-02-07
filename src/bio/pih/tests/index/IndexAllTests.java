package bio.pih.tests.index;

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
		
		suite.addTestSuite(PontuationSchemaTest.class);
		suite.addTestSuite(SubSequencesArrayIndexTest.class);
		suite.addTestSuite(SubSequenceComparerTest.class);

		return suite;
	}
	
}
