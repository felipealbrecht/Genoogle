/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.encoder;

import org.apache.log4j.Logger;

import bio.pih.genoogle.seq.Alphabet;
import bio.pih.genoogle.seq.RNAAlphabet;

public class SequenceEncoderFactory {

	static Logger logger = Logger.getLogger(SequenceEncoderFactory.class.getCanonicalName());

	private static DNASequenceEncoder[] dnaEncoders = new DNASequenceEncoder[17];
	private static RNASequenceEncoder[] rnaEncoders = new RNASequenceEncoder[17];

	/**
	 * @param subSequenceLength
	 *            length of the subSequences.
	 * @return singleton of the {@link DNASequenceEncoderToInteger}
	 */
	public static SequenceEncoder getEncoder(Alphabet alphabet, int subSequenceLength) {
		if (subSequenceLength < 1 || subSequenceLength > 16) {
			throw new IndexOutOfBoundsException("Invalid sub sequence length '" + subSequenceLength + "'");
		}
			
		if (alphabet == RNAAlphabet.SINGLETON) {
			rnaEncoders[subSequenceLength] = new RNASequenceEncoder(subSequenceLength);
			return rnaEncoders[subSequenceLength];		
		} else {
			dnaEncoders[subSequenceLength] = new DNASequenceEncoder(subSequenceLength);
			return dnaEncoders[subSequenceLength];
		} 
	}
	
}
