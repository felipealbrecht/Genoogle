package bio.pih.genoogle.seq;

public interface Alphabet {
	public int getSize();

	boolean isValid(char c);
	
	char[] getLetters();
}
