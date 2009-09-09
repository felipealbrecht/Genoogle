package bio.pih.tests.encoder;

import junit.framework.TestSuite;
import bio.pih.encoder.SequenceEncoder;

/**
 * @author albrecht
 *
 */
public class SequenceCompressorAllTests extends TestSuite {
	
	/**
	 * @return all tests from {@link SequenceEncoder} and its subclasses
	 */
	@org.junit.Test
	public static TestSuite suite() {
		TestSuite suite = new TestSuite("SequenceCompressorAllTests");
		
		suite.addTestSuite(SequenceCompressorTest.class);
		suite.addTestSuite(DNASequenceEncoderToIntegerTest.class);
	
		return suite;
	}
}
