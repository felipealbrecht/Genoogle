/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009, 2010, 2011, 2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq;


// Basead on Automated Alphabet Reduction for Protein Datasets 
// Jaume Bacardit*1,2, Michael Stout1,2, Jonathan D Hirst3, Alfonso Valencia4, Robert E Smith5 and Natalio Krasnogor
public class Reduced_AA_8_Alphabet implements Alphabet {

	private static final long serialVersionUID = 4766651384453040525L;
	char letters[] = {'A', 'C', 'D', 'E', 'F', 'I', 'S', 'X'};
		
	public static Alphabet SINGLETON = new Reduced_AA_8_Alphabet ();
	

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
