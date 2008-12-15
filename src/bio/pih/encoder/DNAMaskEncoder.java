package bio.pih.encoder;

import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.seq.LightweightSymbolList;
import bio.pih.util.SymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIteratorFactory;

public class DNAMaskEncoder extends DNASequenceEncoderToInteger {

	private final boolean[] mask;
	private final int patternLength;
	private final int resultLength;

	public DNAMaskEncoder(String mask, int subSequenceLength) {
		super(subSequenceLength);

		this.patternLength = mask.length();
		this.mask = new boolean[patternLength];
		int length = 0;
		for (int i = 0; i < this.patternLength; i++) {
			if (mask.charAt(i) == '1') {
				this.mask[i] = true;
				length++;
			}
		}
		if (length != subSequenceLength) {
			throw new RuntimeException("The subSequenceLength (" + subSequenceLength
					+ ") and the count of the usable values of the mask (" + length
					+ ") should be the same.");
		}
		this.resultLength = length;
	}
	
	public int getPatternLength() {
		return patternLength;
	}

	public int applyMask(SymbolList symbolList) {
		int encoded = 0;
		int offset = 0;
		int length = symbolList.length();
		
		for (int i = 1; i <= length; i++) {			
			if (this.mask[i - 1]) {
				encoded |= (getBitsFromSymbol(symbolList.symbolAt(i)) << ((resultLength - (i - offset)) << 1));
			} else {
				offset++;
			}
		}

		return encoded;
	}
	
	
	public int applyMask(String subSequence) {
		int encoded = 0;
		int offset = 0;
		int length = subSequence.length();
		
		for (int i = 0; i < length; i++) {			
			if (this.mask[i]) {
				encoded |= (getBitsFromChar(subSequence.charAt(i)) << ((resultLength - (i - offset + 1)) << 1));
			} else {
				offset++;
			}
		}

		return encoded;
	}	
	
	public int[] applySequenceMask(SymbolList sequence) {
		assert (sequence.getAlphabet().equals(alphabet));
		int size = sequence.length() / this.patternLength;
		int extra = sequence.length() % this.patternLength;
		if (extra != 0) { // extra space for incomplete sub-sequence
			// size++; // the masked sequences does not contain incomplete sub-sequences.
		}
		size++; // extra space for information on the length.
		int sequenceEncoded[] = new int[size];
		sequenceEncoded[getPositionLength()] = sequence.length();

		int pos = getPositionBeginBitsVector();
		SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory
				.getNotOverlappedFactory().newSymbolListWindowIterator(sequence,
						this.patternLength);
		while (symbolListWindowIterator.hasNext()) {
			SymbolList next = symbolListWindowIterator.next();
			sequenceEncoded[pos] = applyMask(next);
			pos++;
		}

		return sequenceEncoded;
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Pattern: ");
		sb.append("\"");
		for (int i = 0; i < patternLength; i++) {
			if (this.mask[i]) {
				sb.append('X');
			} else {
				sb.append(' ');
			}
		}
		sb.append("\" ");
		sb.append(patternLength);
		sb.append(" ");
		sb.append(resultLength);
		return sb.toString();
	}


	public static void mainX(String[] args) throws IllegalSymbolException {
		DNAMaskEncoder patternEncoder = new DNAMaskEncoder("111010010100110111", 11);
		SymbolList symbolList = LightweightSymbolList.createDNA("AAACACCACACCAACAAA");
		SymbolList symbolList2 = LightweightSymbolList.createDNA("CCCACAACACAACCACCC");
		SymbolList symbolList3 = LightweightSymbolList.createDNA("AAAAAAAAAAAAAAAAAA");
		SymbolList symbolList4 = LightweightSymbolList.createDNA("AAAAAAACCCCCCCCCCC");
		SymbolList symbolList5 = LightweightSymbolList.createDNA("ACGTACGTACGTACGTAC");

		System.out.println(patternEncoder);

		System.out.println(symbolList.seqString());
		int masked = patternEncoder.applyMask(symbolList);
		System.out.println(patternEncoder.decodeIntegerToString(masked));

		System.out.println(symbolList2.seqString());
		int masked2 = patternEncoder.applyMask(symbolList2);
		System.out.println(patternEncoder.decodeIntegerToString(masked2));

		System.out.println(symbolList3.seqString());
		int masked3 = patternEncoder.applyMask(symbolList3);
		System.out.println(patternEncoder.decodeIntegerToString(masked3));

		System.out.println(symbolList4.seqString());
		int masked4 = patternEncoder.applyMask(symbolList4);
		System.out.println(patternEncoder.decodeIntegerToString(masked4));

		System.out.println(symbolList5.seqString());
		int masked5 = patternEncoder.applyMask(symbolList5);
		System.out.println(patternEncoder.decodeIntegerToString(masked5));

	}

}
