package bio.pih.genoogle.seq;

import java.util.Arrays;


public class DNAAlphabet implements Alphabet {

	private static final long serialVersionUID = 4036186830403094421L;

	public static Alphabet SINGLETON = new DNAAlphabet();

	private static final int size = 4;

	public static char a = 'a';
	public static char c = 'c';
	public static char g = 'g';
	public static char t = 't';
	

	private static final char[] lLetters = {'a', 'c', 'g', 't'};
	private static final char[] uLetters = {'A', 'C', 'G', 'T'};
	private static final char[] lSpecialLetters = {'r', 'y', 'k', 'm', 's', 'w', 'b', 'd', 'h', 'v', 'n', 'x'};
	private static final char[] uSpecialLetters = {'R', 'Y', 'K', 'M', 'S', 'W', 'B', 'D', 'H', 'V', 'N', 'X'};
	private static final char[] allLetters = {'a', 'c', 'g', 't', 
		                                     'A', 'C', 'G', 'T', 
		                                     'r', 'y', 'k', 'm', 's', 'w', 'b', 'd', 'h', 'v', 'n', 'x', 
		                                     'R', 'Y', 'K', 'M', 'S', 'W', 'B', 'D', 'H', 'V', 'N', 'X'};
	static {
		Arrays.sort(lLetters);
		Arrays.sort(uLetters);
		Arrays.sort(lSpecialLetters);
		Arrays.sort(uSpecialLetters);
		Arrays.sort(allLetters);
	}

	@Override
	public String getName() {
		return "DNA";
	}
	
	@Override
	public int getSize() {
		return size;
	}

	@Override
	public boolean isValid(char c) {
		if (Arrays.binarySearch(allLetters, c) >= 0) {
			return true;
		}
		return false;
	}

	
	@Override
	public char[] getLetters() {		
		return lLetters.clone();
	}
}