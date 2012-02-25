/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009, 2010, 2011, 2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq;

import java.util.Arrays;


public class RNAAlphabet implements Alphabet {

	private static final long serialVersionUID = -2862576026568250745L;

	public static Alphabet SINGLETON = new RNAAlphabet();

	private static final int size = 4;

	public static char a = 'a';
	public static char c = 'c';
	public static char g = 'g';
	public static char u = 'u';
	

	private static final char[] lLetters = {'a', 'c', 'g', 'u'};
	private static final char[] uLetters = {'A', 'C', 'G', 'U'};
	private static final char[] lSpecialLetters = {'r', 'y', 'k', 'm', 's', 'w', 'b', 'd', 'h', 'v', 'n', 'x'};
	private static final char[] uSpecialLetters = {'R', 'Y', 'K', 'M', 'S', 'W', 'B', 'D', 'H', 'V', 'N', 'X'};
	private static final char[] allLetters = {'a', 'c', 'g', 'u', 
		                                     'A', 'C', 'G', 'U', 
		                                     'r', 'y', 'k', 'm', 's', 'w', 'b', 'd', 'h', 'v', 'n', 'x', 
		                                     'R', 'Y', 'K', 'M', 'S', 'W', 'B', 'D', 'H', 'V', 'N', 'X'};
	static {
		Arrays.sort(lLetters);
		Arrays.sort(uLetters);
		Arrays.sort(lSpecialLetters);
		Arrays.sort(uSpecialLetters);
		Arrays.sort(allLetters);
	}
	
	private RNAAlphabet() {
	}
	
	@Override
	public String getName() {
		return "RNA";
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
	
	@Override
	public String toString() {
		return "RNA Alphabet";
	}
}