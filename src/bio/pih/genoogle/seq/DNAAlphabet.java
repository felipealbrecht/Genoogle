package bio.pih.genoogle.seq;


public class DNAAlphabet implements Alphabet {

	private static final long serialVersionUID = 4036186830403094421L;

	public static Alphabet SINGLETON = new DNAAlphabet();

	private static final int size = 4;

	public static char a = 'a';
	public static char c = 'c';
	public static char g = 'g';
	public static char t = 't';

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
		if ((c == 'a' || c == 'A') || (c == 'c' || c == 'C') || (c == 'g' || c == 'G') || (c == 't' || c == 'T')) {
			return true;
		}
		return false;
	}

	private final char[] letters = {'a', 'c', 'g', 't'};
	
	@Override
	public char[] getLetters() {
		return letters.clone();
	}
}
