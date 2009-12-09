/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import bio.pih.genoogle.tests.encoder.SequenceCompressorAllTests;
import bio.pih.genoogle.tests.index.IndexAllTests;
import bio.pih.genoogle.tests.io.reader.ReaderAllTests;
import bio.pih.genoogle.tests.seq.SequencesAllTests;
import bio.pih.genoogle.tests.util.SymbolListWindowIteratorAllTests;

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
		suite.addTest(ReaderAllTests.suite());
		suite.addTestSuite(ExtendAlignmentTest.class);
		suite.addTestSuite(TestCircularArrayList.class);
		
		return suite;
	}
}
