/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.encoder;

import java.util.Hashtable;

import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.seq.RNAAlphabet;

/**
 * Abstract class of the DNA Encoder to bit map representation.
 * 
 * @author albrecht
 */
public class RNASequenceEncoder extends SequenceEncoder {

	protected RNASequenceEncoder(int subSequenceLength) throws ValueOutOfBoundsException {
		super(RNAAlphabet.SINGLETON, subSequenceLength);
	}

	// All wildschars will have this value.
	// TODO: implements a way to put at the end of the sequence the "correct" base information.
	static byte defaultWildcharValue = 0x00;

	static Hashtable<Character, Integer> DNACharToBitsSubstitionTable;
	
	static Character[] DNABitsToSymbolSubstitionTable = new Character[] { 'A', 'C', 'G', 'U' };


	static {
		DNACharToBitsSubstitionTable = new Hashtable<Character, Integer>();
		DNACharToBitsSubstitionTable.put('a', 0x00);
		DNACharToBitsSubstitionTable.put('c', 0x01);
		DNACharToBitsSubstitionTable.put('g', 0x02);
		DNACharToBitsSubstitionTable.put('u', 0x03);
		DNACharToBitsSubstitionTable.put('A', 0x00);
		DNACharToBitsSubstitionTable.put('C', 0x01);
		DNACharToBitsSubstitionTable.put('G', 0x02);
		DNACharToBitsSubstitionTable.put('U', 0x03);
	}


	public final int getBitsFromChar(char symbol) {
		if (symbol == 'A' || symbol == 'a') {
			return 0;
		}
		if (symbol == 'C' || symbol == 'c') {
			return 1;
		}
		if (symbol == 'G' || symbol == 'g') {
			return 2;
		}
		if (symbol == 'U' || symbol == 'u') {
			return 3;
		}
		return 0;
	}

	public final char getSymbolFromBits(int bits) {
		return DNABitsToSymbolSubstitionTable[bits];
	}
}
