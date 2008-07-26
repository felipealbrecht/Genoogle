package bio.pih.tests.seq;

import org.junit.Test;

import bio.pih.seq.LightweightSymbolList;
import bio.pih.tests.seq.generator.SequencePopulatorTest;
import junit.framework.TestSuite;

/**
 * @author albrecht
 *
 */
public class SequencesAllTests extends TestSuite {

	
	/**
	 * @return all tests for sequences, related with {@link LightweightSymbolList}
	 */
	@Test
	public static TestSuite suite() {
		TestSuite suite = new TestSuite("SequencesAllTests");
		
		suite.addTestSuite(LightweightSequencesTest.class);
		suite.addTestSuite(SequencePopulatorTest.class);

		return suite;
	}
}