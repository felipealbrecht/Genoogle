/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009, 2010, 2011, 2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq;

import java.util.Arrays;

public class AminoAcidAlphabet implements Alphabet {

	private static final long serialVersionUID = -621945776486639225L;

	char letters[] = {'G', 'A', 'V', 'L', 'I', 'S', 'T', 'D', 'E', 'N', 'Q', 'K', 'R', 'H', 'F', 'C', 'W', 'Y', 'M', 'P', '>', '#'};

	public static Alphabet SINGLETON = new AminoAcidAlphabet ();
		
	@Override
	public String getName() {
		return "Reduced Amino Acids with 8 Alphabet";
	}

	@Override
	public int getSize() {
		return letters.length;
	}

	@Override
	public boolean isValid(char c) {
		for (int i = 0; i < letters.length; i++) {
			if (letters[i] == c) {
				return true;
			}
		}
		return false;
	}

	@Override
	public char[] getLetters() {
		return letters;
	}
	
	
		
}
