/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.tests.encoder;

import junit.framework.TestSuite;
import bio.pih.genoogle.encoder.SequenceEncoder;

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
