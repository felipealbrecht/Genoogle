package bio.pih.index;

import java.util.LinkedList;
import java.util.List;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.util.SymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIteratorFactory;

/**
 * @author albrecht
 *
 */
public class SubSequencesArrayIndex {

	private static int maximumAlphabetBitsSize = 8;
	private IndexBucket index[];
	private final int alphabetSize;
	private final int subSequenceLength;
	private final SymbolListWindowIteratorFactory symbolListWindowIteratorFactory;
	private Class integerType;
	private int bitsByAlphabet;
	
		
	public SubSequencesArrayIndex(int subSequenceLength, int alphabetSize, SymbolListWindowIteratorFactory symbolListWindowIteratorFactory) throws ValueOutOfBoundsException {
		assert(symbolListWindowIteratorFactory != null);
		assert(subSequenceLength > 0);
		assert(alphabetSize > 0);
		
		this.subSequenceLength = subSequenceLength;
		this.alphabetSize = alphabetSize;		
		this.symbolListWindowIteratorFactory = symbolListWindowIteratorFactory;
		
		int indexSize = subSequenceLength * bitsByAlphabetSize(alphabetSize);
		this.integerType = getClassFromSize(indexSize);		
		this.index = new IndexBucket[indexSize];
	}
		
	/** 
	 * @param alphabetSize must be equal or higher than 1 and equals or lower than 256 
	 * @return
	 * @throws ValueOutOfBoundsException is size is between from 1 to 64 
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
	
	public void addSequence(Sequence sequence) {
		if (sequence == null) {
			throw new NullPointerException("sequence can not be null");
		}
		
		SymbolListWindowIterator symbolListWindowIterator = symbolListWindowIteratorFactory.newSymbolListWindowIterator(sequence, this.subSequenceLength);

		while (symbolListWindowIterator.hasNext()) {
			SymbolList next = symbolListWindowIterator.next();
			SubSequenceInfo subSequenceInfo = new SubSequenceInfo(sequence, next, symbolListWindowIterator.getActualPos(), this.subSequenceLength);
			addSubSequence(next, subSequenceInfo);
		}		
	}
	
	/**
	 * 
	 * @param size
	 * @return the integer Class that is need to store the value passed in size param.
	 * @throws ValueOutOfBoundsException
	 */
	public static Class getClassFromSize(int size) throws ValueOutOfBoundsException {
		if (size <= 0) {
			throw new ValueOutOfBoundsException("size lower than zero.");
		}
		if(size > Long.MAX_VALUE) {
			throw new ValueOutOfBoundsException("size higher than " + Long.MAX_VALUE);
		}
		
		if (size <= 8) {
			return Byte.class;
		} else if (size <= 16) {
			return Short.class;
		} else if (size <= 32) {
			return Integer.class;
		} else if (size <= 64) {
			return Long.class;
		} else {
			throw new ValueOutOfBoundsException("size is higher than a " + Long.MAX_VALUE + "? May be it's a bug.");
		}		
		
		
	}
	
	/**
	 * @param subSymbolList
	 * @param info
	 */
	private void addSubSequence(SymbolList subSymbolList, SubSequenceInfo info) { }
	
	/**
	 * @param subSymbolList
	 * @return
	 */
	List<SubSequenceInfo> retrievePosition(SymbolList subSymbolList) {
		return null;
	}
	
	public class IndexBucket {
		int value;
		List<SubSequenceInfo> elements;
		
		/**
		 * @param value
		 */
		public IndexBucket(int value) {
			this.value = value;
			this.elements = new LinkedList<SubSequenceInfo>();
		}
		
		/**
		 * @return the value associate with this bucket
		 */
		public int getValue() {
			return value;
		}
		
		/**
		 * @return the elements in this bucket
		 */
		public List<SubSequenceInfo> getElements() {
			return elements;
		}
	}
}
