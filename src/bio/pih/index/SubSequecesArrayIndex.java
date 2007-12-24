package bio.pih.index;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.biojava.bio.symbol.SymbolList;

/**
 * @author albrecht
 *
 */
public class SubSequecesArrayIndex {

	private static int maximumAlphabetBitsSize = 8;
	private ArrayList<LinkedList<SubSequenceInfo>> index;
	private final int alphabetSize;
	private final int bits;
	private int size;
	
	
	public SubSequecesArrayIndex(int bits, int alphabetSize) {
		this.bits = bits;
		this.alphabetSize = alphabetSize;
		this.size = alphabetSize << bits;		
	}
	
	
	/**
	 * 
	 * @param alphabetSize must be equal or higher than 1 and equals or lower than 256 
	 * @return
	 * @throws ValueOutOfBoundsException 
	 */
	public static int bitsByAlphabetSize(int alphabetSize) throws ValueOutOfBoundsException {
		if (alphabetSize <= 0) {
			throw new ValueOutOfBoundsException("alphabetSize lower than zero.");
		}
		if(alphabetSize > (1 << maximumAlphabetBitsSize)) {
			throw new ValueOutOfBoundsException("alphabetSize higher than " + (1 << maximumAlphabetBitsSize));
		}
		
		int maxValue = alphabetSize-1;
		if (maxValue == 0) {
			return 1;
		}
		
		int bits = maximumAlphabetBitsSize;
		while ((maxValue & (1 << bits)) == 0) {
			bits--;
		}
		// the "one" that was used above must be added.
		return bits+1;		
	}
	
	/**
	 * @param subSymbolList
	 * @param info
	 */
	public void addSubSequence(SymbolList subSymbolList, SubSequenceInfo info) { }
	
	/**
	 * @param subSymbolList
	 * @return
	 */
	List<SubSequenceInfo> retrievePosition(SymbolList subSymbolList) {
		return null;
	}
}
