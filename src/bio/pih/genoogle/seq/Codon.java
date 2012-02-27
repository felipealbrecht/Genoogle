/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009, 2010, 2011, 2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq;

import java.util.HashMap;

public class Codon {
	String content;
	
	private HashMap<String, AminoAcid> codonToProteinMap;
	
	public static Codon INSTANCE = new Codon();
	
	private Codon() {
		codonToProteinMap = new HashMap<String, AminoAcid>();
		
		codonToProteinMap.put("GGU", AminoAcid.glycine);
		codonToProteinMap.put("GGC", AminoAcid.glycine);
		codonToProteinMap.put("GGA", AminoAcid.glycine);
		codonToProteinMap.put("GGG", AminoAcid.glycine);
		
		codonToProteinMap.put("GCU", AminoAcid.alanine);
		codonToProteinMap.put("GCC", AminoAcid.alanine);
		codonToProteinMap.put("GCA", AminoAcid.alanine);
		codonToProteinMap.put("GCG", AminoAcid.alanine);
		
		codonToProteinMap.put("GUU", AminoAcid.valine);
		codonToProteinMap.put("GUC", AminoAcid.valine);
		codonToProteinMap.put("GUA", AminoAcid.valine);
		codonToProteinMap.put("GUG", AminoAcid.valine);
		
		codonToProteinMap.put("UUA", AminoAcid.leucine);
		codonToProteinMap.put("UUG", AminoAcid.leucine);
		codonToProteinMap.put("CUU", AminoAcid.leucine);
		codonToProteinMap.put("CUC", AminoAcid.leucine);
		codonToProteinMap.put("CUA", AminoAcid.leucine);
		codonToProteinMap.put("CUG", AminoAcid.leucine);
		
		codonToProteinMap.put("AUU", AminoAcid.isoleucine);
		codonToProteinMap.put("AUC", AminoAcid.isoleucine);
		codonToProteinMap.put("AUA", AminoAcid.isoleucine);
		
		codonToProteinMap.put("UCU", AminoAcid.serine);
		codonToProteinMap.put("UCC", AminoAcid.serine);
		codonToProteinMap.put("UCA", AminoAcid.serine);
		codonToProteinMap.put("UCG", AminoAcid.serine);
		codonToProteinMap.put("AGU", AminoAcid.serine);
		codonToProteinMap.put("AGC", AminoAcid.serine);
		
		codonToProteinMap.put("ACU", AminoAcid.threorine);
		codonToProteinMap.put("ACC", AminoAcid.threorine);
		codonToProteinMap.put("ACA", AminoAcid.threorine);
		codonToProteinMap.put("ACG", AminoAcid.threorine);
		
		codonToProteinMap.put("GAU", AminoAcid.aspartic);
		codonToProteinMap.put("GAC", AminoAcid.aspartic);
		
		codonToProteinMap.put("GAA", AminoAcid.glutamic);
		codonToProteinMap.put("GAG", AminoAcid.glutamic);
		
		codonToProteinMap.put("AAU", AminoAcid.asparagine);
		codonToProteinMap.put("AAC", AminoAcid.asparagine);
		
		codonToProteinMap.put("CAA", AminoAcid.glutamine);
		codonToProteinMap.put("CAG", AminoAcid.glutamine);
		
		codonToProteinMap.put("AAG", AminoAcid.lysine);
		codonToProteinMap.put("AAA", AminoAcid.lysine);
		
		codonToProteinMap.put("CGU", AminoAcid.arginine);
		codonToProteinMap.put("CGC", AminoAcid.arginine);
		codonToProteinMap.put("CGA", AminoAcid.arginine);
		codonToProteinMap.put("CGG", AminoAcid.arginine);
		codonToProteinMap.put("AGA", AminoAcid.arginine);
		codonToProteinMap.put("AGG", AminoAcid.arginine);

		codonToProteinMap.put("CAU", AminoAcid.histidine);
		codonToProteinMap.put("CAC", AminoAcid.histidine);
		
		codonToProteinMap.put("UUU", AminoAcid.phenylalanine);
		codonToProteinMap.put("UUC", AminoAcid.phenylalanine);
		
		codonToProteinMap.put("UGU", AminoAcid.cysteine);
		codonToProteinMap.put("UGC", AminoAcid.cysteine);
		
		codonToProteinMap.put("UGG", AminoAcid.tryptophan);
		
		codonToProteinMap.put("UAU", AminoAcid.tyrosine);
		codonToProteinMap.put("UAC", AminoAcid.tyrosine);
		
		codonToProteinMap.put("AUG", AminoAcid.methionine);
		
		codonToProteinMap.put("CCU", AminoAcid.proline);
		codonToProteinMap.put("CCC", AminoAcid.proline);
		codonToProteinMap.put("CCA", AminoAcid.proline);
		codonToProteinMap.put("CCG", AminoAcid.proline);
		
		codonToProteinMap.put("AUG", AminoAcid.start);
		
		codonToProteinMap.put("UAG", AminoAcid.end);
		codonToProteinMap.put("UAA", AminoAcid.end);
		codonToProteinMap.put("UGA", AminoAcid.end);		
	}	
	
	public AminoAcid convert(String codon) {
		if (codon.length() != 3) {
			// throw exception
		}		
		// TODO: changer the method to have an convertRNA and a DNA inputs, 
		// so it will be not necessary
		
		/*
		 * http://en.wikipedia.org/wiki/FASTA_format
		R	A G (puRine)
		Y	C T U (pYrimidine)
		K	G T U (Ketone[citation needed])
		M	A C (aMino group[citation needed])
		S	C G (Strong interaction[citation needed])
		W	A T U (Weak interaction[citation needed])
		B	C G T U (not A) (B comes after A)
		D	A G T U (not C) (D comes after C)
		H	A C T U (not G) (H comes after G)
		V	A C G (not T, not U) (V comes after U)
		N	A C G T U (aNy)
		X	Masked
		*/ 
		
		String c = codon.toUpperCase().replace('T', 'U')
			.replace('R', 'A')
			.replace('Y', 'C')
			.replace('K', 'G')
			.replace('M', 'A')
			.replace('S', 'C')
			.replace('W', 'A')
			.replace('B', 'C')
			.replace('D', 'A')
			.replace('H', 'A')
			.replace('V', 'A')
			.replace('N', 'A')
			.replace('X', 'A');
				
		return codonToProteinMap.get(c);
	}
}