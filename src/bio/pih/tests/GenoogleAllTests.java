package bio.pih.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import bio.pih.tests.compressor.SequenceCompressorAllTests;
import bio.pih.tests.index.SubSequencesArrayIndexTest;
import bio.pih.tests.scheduler.SchedulerTest;
import bio.pih.tests.seq.generator.SequencePopulatorTest;
import bio.pih.tests.util.SymbolListWindowIteratorAllTests;

/**
 * @author Albrecht	
 *
 */
public class GenoogleAllTests {

	/**
	 * @return all tests make for Genoogle
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Genoogle all tests");
		suite.addTest(SymbolListWindowIteratorAllTests.suite());
		suite.addTest(SequenceCompressorAllTests.suite());
		suite.addTestSuite(SequencePopulatorTest.class);
		suite.addTestSuite(SubSequencesArrayIndexTest.class);
		suite.addTestSuite(SchedulerTest.class);
		
		return suite;
	}
}
