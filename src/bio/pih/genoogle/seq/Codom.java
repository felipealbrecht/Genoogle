/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009, 2010, 2011, 2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq;

import java.util.HashMap;

public class Codom {
	String content;
	
	public HashMap<String, AminoAcid> codomToProteinMap;
	
	public static Codom INSTANCE = new Codom();
	
	private Codom() {
		codomToProteinMap = new HashMap<String, AminoAcid>();
		
		codomToProteinMap.put("GGU", AminoAcid.glycine);
		codomToProteinMap.put("GGC", AminoAcid.glycine);
		codomToProteinMap.put("GGC", AminoAcid.glycine);
		codomToProteinMap.put("GGG", AminoAcid.glycine);
		
		codomToProteinMap.put("GCU", AminoAcid.alanine);
		codomToProteinMap.put("GCC", AminoAcid.alanine);
		codomToProteinMap.put("GCA", AminoAcid.alanine);
		codomToProteinMap.put("GCG", AminoAcid.alanine);
		
		codomToProteinMap.put("GUU", AminoAcid.valine);
		codomToProteinMap.put("GUC", AminoAcid.valine);
		codomToProteinMap.put("GUA", AminoAcid.valine);
		codomToProteinMap.put("GUG", AminoAcid.valine);
		
		codomToProteinMap.put("UUA", AminoAcid.leucine);
		codomToProteinMap.put("UUG", AminoAcid.leucine);
		codomToProteinMap.put("CUU", AminoAcid.leucine);
		codomToProteinMap.put("CUC", AminoAcid.leucine);
		codomToProteinMap.put("CUA", AminoAcid.leucine);
		codomToProteinMap.put("CUG", AminoAcid.leucine);
		
		codomToProteinMap.put("AUU", AminoAcid.isoleucine);
		codomToProteinMap.put("AUC", AminoAcid.isoleucine);
		codomToProteinMap.put("AUA", AminoAcid.isoleucine);
		
		codomToProteinMap.put("UCU", AminoAcid.serine);
		codomToProteinMap.put("UCC", AminoAcid.serine);
		codomToProteinMap.put("UCA", AminoAcid.serine);
		codomToProteinMap.put("UCG", AminoAcid.serine);
		codomToProteinMap.put("AGU", AminoAcid.serine);
		codomToProteinMap.put("AGC", AminoAcid.serine);
		
		codomToProteinMap.put("ACU", AminoAcid.threorine);
		codomToProteinMap.put("ACC", AminoAcid.threorine);
		codomToProteinMap.put("ACA", AminoAcid.threorine);
		codomToProteinMap.put("ACG", AminoAcid.threorine);
		
		codomToProteinMap.put("GAU", AminoAcid.aspartic);
		codomToProteinMap.put("GAC", AminoAcid.aspartic);
		
		codomToProteinMap.put("GAA", AminoAcid.glutamic);
		codomToProteinMap.put("GAG", AminoAcid.glutamic);
		
		codomToProteinMap.put("AAU", AminoAcid.asparagine);
		codomToProteinMap.put("AAC", AminoAcid.asparagine);
		
		codomToProteinMap.put("CAA", AminoAcid.glutamine);
		codomToProteinMap.put("CAG", AminoAcid.glutamine);
		
		codomToProteinMap.put("AAG", AminoAcid.lysine);
		codomToProteinMap.put("AAA", AminoAcid.lysine);
		
		codomToProteinMap.put("CGU", AminoAcid.arginine);
		codomToProteinMap.put("CGC", AminoAcid.arginine);
		codomToProteinMap.put("CGA", AminoAcid.arginine);
		codomToProteinMap.put("CGG", AminoAcid.arginine);
		codomToProteinMap.put("AGA", AminoAcid.arginine);
		codomToProteinMap.put("AGG", AminoAcid.arginine);

		codomToProteinMap.put("CAU", AminoAcid.histidine);
		codomToProteinMap.put("CAC", AminoAcid.histidine);
		
		codomToProteinMap.put("UUU", AminoAcid.phenylalanine);
		codomToProteinMap.put("UUC", AminoAcid.phenylalanine);
		
		codomToProteinMap.put("UGU", AminoAcid.cysteine);
		codomToProteinMap.put("UGC", AminoAcid.cysteine);
		
		codomToProteinMap.put("UGG", AminoAcid.tryptophan);
		
		codomToProteinMap.put("UAU", AminoAcid.tyrosine);
		codomToProteinMap.put("UAC", AminoAcid.tyrosine);
		
		codomToProteinMap.put("AUG", AminoAcid.methionine);
		
		codomToProteinMap.put("CCU", AminoAcid.proline);
		codomToProteinMap.put("CCC", AminoAcid.proline);
		codomToProteinMap.put("CCA", AminoAcid.proline);
		codomToProteinMap.put("CCG", AminoAcid.proline);
		
		codomToProteinMap.put("AUG", AminoAcid.start);
		
		codomToProteinMap.put("UAG", AminoAcid.end);
		codomToProteinMap.put("UAA", AminoAcid.end);
		codomToProteinMap.put("AUGA", AminoAcid.end);		
	}	
	
	public static void main(String[] args) {
		System.out.println(Codom.INSTANCE.codomToProteinMap.keySet().size());
	}
}