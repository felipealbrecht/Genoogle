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
	static Character[] DNABitsToSymbolSubstitionTable;
		
	static {
		DNASymbolToBitsSubstitionTable = new Hashtable<Symbol, Integer>();
		DNASymbolToBitsSubstitionTable.put(DNATools.a(), 0x00);
		DNASymbolToBitsSubstitionTable.put(DNATools.c(), 0x01);
		DNASymbolToBitsSubstitionTable.put(DNATools.g(), 0x02);
		DNASymbolToBitsSubstitionTable.put(DNATools.t(), 0x03);

		DNABitsToSymbolSubstitionTable = new Character[] {'a', 'c', 'g', 't'};
	}
	

	protected static int getBitsFromSymbol(Symbol symbol) {
		Integer b = DNASymbolToBitsSubstitionTable.get(symbol);
		if (b == null) {
			return defaultWildcharValue ;
		} 
		
		return b.byteValue();		
	}
	
	protected static char getSymbolFromBits(int bits) {
		return DNABitsToSymbolSubstitionTable[bits];
	}
}
