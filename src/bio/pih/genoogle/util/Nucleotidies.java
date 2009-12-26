package bio.pih.genoogle.util;

import bio.pih.genoogle.seq.Alphabet;
import bio.pih.genoogle.seq.DNAAlphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.RNAAlphabet;

public class Nucleotidies {

	
	public static String dnaToRna(String dnaSequence) throws IllegalSymbolException {
		Alphabet dna = DNAAlphabet.SINGLETON;
		
		StringBuilder sb = new StringBuilder(dnaSequence.length());
		
		for (int i = 0; i < dnaSequence.length(); i++) {
			char c = dnaSequence.charAt(i);
			if (!dna.isValid(c)) {
				throw new IllegalSymbolException(c);
			}
			
			if (c == 't') {
				sb.append('u');
			} else if (c == 'T') {
				sb.append('U');
			} else {
				sb.append(c);
			}			
		}
				
		return sb.toString();
	}
	
	public static String rnaToDna(String rnaSequence) throws IllegalSymbolException {
		Alphabet rna = RNAAlphabet.SINGLETON;
		
		StringBuilder sb = new StringBuilder(rnaSequence.length());
		
		for (int i = 0; i < rnaSequence.length(); i++) {
			char c = rnaSequence.charAt(i);
			if (!rna.isValid(c)) {
				throw new IllegalSymbolException(c);
			}
			
			if (c == 'u') {
				sb.append('t');
			} else if (c == 'U') {
				sb.append('T');
			} else {
				sb.append(c);
			}			
		}
				
		return sb.toString();
	}
	
	

}
