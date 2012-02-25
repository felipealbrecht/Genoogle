/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009, 2010, 2011, 2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq;

public class AminoAcid {
	private String name;
	private String acronym;
	private char symbol;
		
	public static AminoAcid glycine = new AminoAcid("Glycine", "Gly", 'G');
	public static AminoAcid alanine = new AminoAcid("Alanine", "Ala", 'A');
	public static AminoAcid valine = new AminoAcid("Valine", "Val", 'V');
	public static AminoAcid leucine	 = new AminoAcid("Leucine", "Leu", 'L');
	public static AminoAcid isoleucine	= new AminoAcid("Isoleucine", "Ile", 'I');
	public static AminoAcid serine	= new AminoAcid("Serine", "Ser", 'S');
	public static AminoAcid threorine = new AminoAcid("Threorine", "Thr", 'T');
	public static AminoAcid aspartic = new AminoAcid("Aspartic", "Asp", 'D');
	public static AminoAcid glutamic = new AminoAcid("Glutamic", "Glu", 'E');
	public static AminoAcid asparagine = new AminoAcid("Asparagine", "Asn", 'N');
	public static AminoAcid glutamine = new AminoAcid("Glutamine", "Gln", 'Q');
	public static AminoAcid lysine = new AminoAcid("Lysine", "Lys", 'K');
	public static AminoAcid arginine = new AminoAcid("Arginine", "Arg", 'R');
	public static AminoAcid histidine= new AminoAcid("Histidine", "His", 'H');
	public static AminoAcid phenylalanine = new AminoAcid("Phenylalanine", "Phe", 'F');
	public static AminoAcid cysteine = new AminoAcid("Cysteine", "Cys", 'C');
	public static AminoAcid tryptophan = new AminoAcid("Tryptophan", "Trp", 'W');
	public static AminoAcid tyrosine = new AminoAcid("Tyrosine", "Try", 'Y');
	public static AminoAcid methionine = new AminoAcid("Methionine", "Met", 'M');
	public static AminoAcid proline = new AminoAcid("Proline", "Pro", 'P');
	public static AminoAcid start = new AminoAcid("START", "STR", '>');
	public static AminoAcid end = new AminoAcid("END", "END", '#');
	
	private AminoAcid (String name, String acronym, char symbol) {
		this.name = name;
		this.acronym = acronym;
		this.symbol = symbol;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAcronym() {
		return acronym;
	}
	
	public char getSymbol() {
		return symbol;
	}
	
}




