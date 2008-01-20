package bio.pih.tests.compressor;

import bio.pih.compressor.SequenceCompressor;
import junit.framework.TestSuite;

/**
 * @author albrecht
 *
 */
public class SequenceCompressorAllTests extends TestSuite {
	
	/**
	 * @return all tests from {@link SequenceCompressor} and its subclasses
	 */
	@org.junit.Test
	public static TestSuite suite() {
		TestSuite suite = new TestSuite("SequenceCompressorAllTests");
		
		suite.addTestSuite(SequenceCompressorTest.class);
		suite.addTestSuite(DNASequenceCompressorToShortTest.class);
	
		return suite;
	}
}
