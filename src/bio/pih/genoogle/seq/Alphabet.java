package bio.pih.genoogle.seq;

import java.io.Serializable;

public interface Alphabet extends Serializable {
	
	public String getName();
	
	public int getSize();

	boolean isValid(char c);
	
	char[] getLetters();
}
