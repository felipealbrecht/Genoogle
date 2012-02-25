/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009, 2010, 2011, 2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq;

import java.util.HashMap;

// Basead on Automated Alphabet Reduction for Protein Datasets 
// Jaume Bacardit*1,2, Michael Stout1,2, Jonathan D Hirst3, Alfonso Valencia4, Robert E Smith5 and Natalio Krasnogor
public class Reduced_AA_8_Alphabet implements Alphabet {

	private static final long serialVersionUID = 4766651384453040525L;
	char letters[] = {'A', 'C', 'D', 'E', 'F', 'I', 'S', 'X'};
	HashMap<Character, Character> reduced = new HashMap<Character, Character>();
	
	public static Alphabet SINGLETON = new Reduced_AA_8_Alphabet ();
	
	private Reduced_AA_8_Alphabet() {
		reduced.put('A', 'A');
		reduced.put('V', 'A');
		
		reduced.put('C', 'C');
		reduced.put('G', 'C');
		reduced.put('N', 'C');
		reduced.put('P', 'C');
		
		reduced.put('D', 'D');
		
		reduced.put('E', 'E');
		reduced.put('K', 'E');
		reduced.put('R', 'E');
		reduced.put('Q', 'E');
		
		reduced.put('F', 'F');
		reduced.put('W', 'F');
		reduced.put('Y', 'F');
		reduced.put('H', 'F');
		
		reduced.put('I', 'I');
		reduced.put('L', 'I');
		reduced.put('M', 'I');
		
		reduced.put('S', 'S');
		reduced.put('T', 'S');
		
		reduced.put('X', 'X');
	}
	
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
		return reduced.containsKey(c);
	}

	@Override
	public char[] getLetters() {
		return letters;
	}
	
	
		
}
