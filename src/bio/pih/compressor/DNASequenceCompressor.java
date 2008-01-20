package bio.pih.compressor;

import java.util.Hashtable;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.Symbol;

import bio.pih.index.ValueOutOfBoundsException;

/**
 * @author albrecht
 */
public abstract class DNASequenceCompressor extends SequenceCompressor {
	
	protected DNASequenceCompressor(int subSequenceLength) throws ValueOutOfBoundsException {
		super(DNATools.getDNA(), subSequenceLength);
	}

	static Hashtable<Symbol, Byte> DNASymbolToBitsSubstitionTable;
	static Hashtable<Byte, Character> DNABitsToSymbolSubstitionTable;
		
	static {
		DNASymbolToBitsSubstitionTable = new Hashtable<Symbol, Byte>();
		DNASymbolToBitsSubstitionTable.put(DNATools.a(), (byte) 0x00);
		DNASymbolToBitsSubstitionTable.put(DNATools.c(), (byte) 0x01);
		DNASymbolToBitsSubstitionTable.put(DNATools.g(), (byte) 0x02);
		DNASymbolToBitsSubstitionTable.put(DNATools.t(), (byte) 0x03);

		DNABitsToSymbolSubstitionTable = new Hashtable<Byte, Character>();
		DNABitsToSymbolSubstitionTable.put((byte) 0x00, 'A');
		DNABitsToSymbolSubstitionTable.put((byte) 0x01, 'C');
		DNABitsToSymbolSubstitionTable.put((byte) 0x02, 'G');
		DNABitsToSymbolSubstitionTable.put((byte) 0x03, 'T');
	}
	

	protected byte getBitsFromSymbol(Symbol symbol) {
		return DNASymbolToBitsSubstitionTable.get(symbol).byteValue();
	}
	

	protected char getSymbolFromBits(byte bits) {
		return DNABitsToSymbolSubstitionTable.get(bits);
	}

}
