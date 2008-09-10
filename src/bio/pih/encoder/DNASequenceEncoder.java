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

		DNABitsToSymbolSubstitionTable = new Character[] {'A', 'C', 'G', 'T'};
	}
	

	public int getBitsFromSymbol(Symbol symbol) {
		Integer b = DNASymbolToBitsSubstitionTable.get(symbol);
		if (b == null) {
			return defaultWildcharValue ;
		} 
		
		return b.byteValue();		
	}
	

	public static char getSymbolFromBits(int bits) {
		return DNABitsToSymbolSubstitionTable[bits];
	}
	
	@Override
	public int getLengthInBytes(int sequenceLength) {
		int total = sequenceLength / subSequenceLength;
		int extra = sequenceLength % subSequenceLength;
		if (extra != 0) { // extra space for the incomplete sub-sequence
			total++;
		}

		total++; // extra space for length information

		total *= 2; // convert short to byte

		return total;
	}

}
