/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009, 2010, 2011, 2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.encoder;

import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.seq.Reduced_AA_8_Alphabet;
import bio.pih.genoogle.seq.SymbolList;

/**
 * Abstract class of the Reduced AA Encoder to bit map representation.
 * 
 * @author albrecht
 */
public class Reduced_AA_8_SequenceEncoder extends SequenceEncoder {

	protected Reduced_AA_8_SequenceEncoder(int subSequenceLength) throws ValueOutOfBoundsException {
		super(Reduced_AA_8_Alphabet.SINGLETON, subSequenceLength);
	}
		
	static Character[] ReducedAABitsToSymbolSubstitionTable = new Character[] {'A', 'C', 'D', 'E', 'F', 'I', 'S', 'X'};


	public final int getBitsFromChar(char symbol) {
		if (symbol == 'A') {
			return 0;
		}
		if (symbol == 'C') {
			return 1;
		}
		if (symbol == 'D') {
			return 2;
		}
		if (symbol == 'E') {
			return 3;
		}
		if (symbol == 'F') {
			return 4;
		}
		if (symbol == 'I') {
			return 5;
		}
		if (symbol == 'S') {
			return 6;
		}
		if (symbol == 'X') {
			return 7;
		}
		
		throw new RuntimeException("Invalid symbol " + symbol);
	}

	public final char getSymbolFromBits(int bits) {
		return ReducedAABitsToSymbolSubstitionTable[bits];
	}
	
	public static void main(String[] args) throws IllegalSymbolException {
		SequenceEncoder e = new Reduced_AA_8_SequenceEncoder(3);
		
		System.out.println(e.bitsByAlphabetSize);
		System.out.println(e.bitsMask);
		System.out.println(e.subSequenceLength);
		
		SymbolList s = LightweightSymbolList.createReducedAA("ACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISXACDEFISX");
		int[] encodeSubSequenceToInteger = e.encodeSymbolListToIntegerArray(s);
		String ss = e.decodeIntegerArrayToString(encodeSubSequenceToInteger);
		
		System.out.println(s.seqString().equals(ss));		
	}
}
