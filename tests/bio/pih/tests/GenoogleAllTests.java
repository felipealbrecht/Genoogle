package bio.pih.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import bio.pih.tests.compressor.SequenceCompressorAllTests;
import bio.pih.tests.index.IndexAllTests;
import bio.pih.tests.scheduler.SchedulerTest;
import bio.pih.tests.seq.SequencesAllTests;
import bio.pih.tests.util.SymbolListWindowIteratorAllTests;

/**
 * @author Albrecht	
 *
 */
public class GenoogleAllTests {

	/**
	 * @return all Genoogle tests 
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Genoogle all tests");
		suite.addTest(SequencesAllTests.suite());
		suite.addTest(SymbolListWindowIteratorAllTests.suite());
		suite.addTest(SequenceCompressorAllTests.suite());
		suite.addTest(IndexAllTests.suite());
		suite.addTestSuite(SchedulerTest.class);
		
		return suite;
	}
}