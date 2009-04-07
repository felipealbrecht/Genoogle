package bio.pih.encoder;

import java.util.Hashtable;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.Symbol;

import bio.pih.index.ValueOutOfBoundsException;

/**
 * @author albrecht
 */
public abstract class DNASequenceEncoder extends SequenceEncoder {
	
	protected DNASequenceEncoder(int subSequenceLength) throws ValueOutOfBoundsException {
		super(DNATools.getDNA(), subSequenceLength);
	}

	// All wildschars will have this value.
	// TODO: implements a way to put at the end of the sequence the "correct" base information.
	static byte defaultWildcharValue = 0x00;
	
	static Hashtable<Symbol, Integer> DNASymbolToBitsSubstitionTable;
	static Hashtable<Character, Integer> DNACharToBitsSubstitionTable;
	static Character[] DNABitsToSymbolSubstitionTable;
		
	static {
		DNASymbolToBitsSubstitionTable = new Hashtable<Symbol, Integer>();
		DNASymbolToBitsSubstitionTable.put(DNATools.a(), 0x00);
		DNASymbolToBitsSubstitionTable.put(DNATools.c(), 0x01);
		DNASymbolToBitsSubstitionTable.put(DNATools.g(), 0x02);
		DNASymbolToBitsSubstitionTable.put(DNATools.t(), 0x03);

		DNABitsToSymbolSubstitionTable = new Character[] {'A', 'C', 'G', 'T'};
	}
	
	static {
		DNACharToBitsSubstitionTable = new Hashtable<Character, Integer>();
		DNACharToBitsSubstitionTable.put('a', 0x00);
		DNACharToBitsSubstitionTable.put('c', 0x01);
		DNACharToBitsSubstitionTable.put('g', 0x02);
		DNACharToBitsSubstitionTable.put('t', 0x03);
		DNACharToBitsSubstitionTable.put('A', 0x00);
		DNACharToBitsSubstitionTable.put('C', 0x01);
		DNACharToBitsSubstitionTable.put('G', 0x02);
		DNACharToBitsSubstitionTable.put('T', 0x03);
	}
	

	public static int getBitsFromSymbol(Symbol symbol) {
		if (symbol == DNATools.a()) {
			return 0;
		}
		if (symbol == DNATools.c()) {
			return 1;
		}
		if (symbol == DNATools.g()) {
			return 2;
		}
		if (symbol == DNATools.t()) {
			return 3;
		}
		return 0;						
	}
	
	public static int getBitsFromChar(char symbol) {
		if (symbol == 'A' || symbol == 'a') {
			return 0;
		}
		if (symbol == 'C' || symbol == 'c') {
			return 1;
		}
		if (symbol == 'G' || symbol == 'g') {
			return 2;
		}
		if (symbol == 'T' || symbol == 't') {
			return 3;
		}
		return 0;
	}
	
	
	protected static char getSymbolFromBits(int bits) {
		return DNABitsToSymbolSubstitionTable[bits];
	}
}
