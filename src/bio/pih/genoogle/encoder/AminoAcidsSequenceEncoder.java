/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009, 2010, 2011, 2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.encoder;

import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.seq.AminoAcidAlphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.seq.SymbolList;

/**
 * Abstract class of the Reduced AA Encoder to bit map representation.
 * 
 * @author albrecht
 */
public class AminoAcidsSequenceEncoder extends SequenceEncoder {

	protected AminoAcidsSequenceEncoder(int subSequenceLength) throws ValueOutOfBoundsException {
		super(AminoAcidAlphabet.SINGLETON, subSequenceLength);
	}
		
	// TODO: read it from alphabet.
	char letters[] = {'G', 'A', 'V', 'L', 'I', 'S', 'T', 'D', 'E', 'N', 'Q', 'K', 'R', 'H', 'F', 'C', 'W', 'Y', 'M', 'P', '$', '#'};


	public final int getBitsFromChar(char symbol) {
		if (symbol == 'G') {
			return 0;
		}
		if (symbol == 'A') {
			return 1;
		}
		if (symbol == 'V') {
			return 2;
		}
		if (symbol == 'L') {
			return 3;
		}
		if (symbol == 'I') {
			return 4;
		}
		if (symbol == 'S') {
			return 5;
		}
		if (symbol == 'T') {
			return 6;
		}
		if (symbol == 'D') {
			return 7;
		}
		if (symbol == 'E') {
			return 8;
		}
		if (symbol == 'N') {
			return 9;
		}
		if (symbol == 'Q') {
			return 10;
		}
		if (symbol == 'K') {
			return 11;
		}
		if (symbol == 'R') {
			return 12;
		}
		if (symbol == 'H') {
			return 13;
		}
		if (symbol == 'F') {
			return 14;
		}
		if (symbol == 'C') {
			return 15;
		}
		if (symbol == 'W') {
			return 16;
		}
		if (symbol == 'Y') {
			return 17;
		}
		if (symbol == 'M') {
			return 18;
		}
		if (symbol == 'P') {
			return 19;
		}
		if (symbol == '$') {
			return 20;
		}
		if (symbol == '#') {
			return 21;
		}
		throw new RuntimeException("Invalid symbol " + symbol);
	}

	public final char getSymbolFromBits(int bits) {
		return letters[bits];
	}
	
	public static void main(String[] args) throws IllegalSymbolException {
		SequenceEncoder e = new AminoAcidsSequenceEncoder(3);
		
		System.out.println(e.bitsByAlphabetSize);
		System.out.println(e.bitsMask);
		System.out.println(e.subSequenceLength);
		                                                    
		SymbolList s = LightweightSymbolList.createProtein("GAVLISTDENQKRHFCWYMP$#");
		int[] encodeSubSequenceToInteger = e.encodeSymbolListToIntegerArray(s);
		String ss = e.decodeIntegerArrayToString(encodeSubSequenceToInteger);
		
		System.out.println(ss);
		
		System.out.println(s.seqString().equals(ss));		
	}
}
