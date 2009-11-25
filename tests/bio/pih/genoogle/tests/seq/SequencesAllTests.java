package bio.pih.genoogle.tests.seq;

import junit.framework.TestSuite;

import org.junit.Test;

import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.tests.seq.generator.SequencePopulatorTest;

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
