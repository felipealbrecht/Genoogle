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
	
	static Hashtable<Symbol, Byte> DNASymbolToBitsSubstitionTable;
	static Character[] DNABitsToSymbolSubstitionTable;
		
	static {
		DNASymbolToBitsSubstitionTable = new Hashtable<Symbol, Byte>();
		DNASymbolToBitsSubstitionTable.put(DNATools.a(), (byte) 0x00);
		DNASymbolToBitsSubstitionTable.put(DNATools.c(), (byte) 0x01);
		DNASymbolToBitsSubstitionTable.put(DNATools.g(), (byte) 0x02);
		DNASymbolToBitsSubstitionTable.put(DNATools.t(), (byte) 0x03);

		DNABitsToSymbolSubstitionTable = new Character[] {'A', 'C', 'G', 'T'};
	}
	

	protected byte getBitsFromSymbol(Symbol symbol) {
		Byte b = DNASymbolToBitsSubstitionTable.get(symbol);
		if (b == null) {
			return defaultWildcharValue ;
		} else {
			return b.byteValue();
		}
		
	}
	

	protected char getSymbolFromBits(byte bits) {
		return DNABitsToSymbolSubstitionTable[bits];
	}

}
