package bio.pih.genoogle.tests.util;

import junit.framework.TestCase;

import org.junit.Test;

import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.util.Nucleotidies;

public class NucleotidiesConverterTest extends TestCase {

	@Test
	public void testTTTTToUUUU() throws IllegalSymbolException {
		String rna = Nucleotidies.dnaToRna("TTTT");
		assertEquals("UUUU", rna);
	}
	
	@Test
	public void testACGTToACGU() throws IllegalSymbolException {
		String rna = Nucleotidies.dnaToRna("ACGT");
		assertEquals("ACGU", rna);
	}
	
	@Test
	public void testUUUUToTTTT() throws IllegalSymbolException {
		String rna = Nucleotidies.rnaToDna("UUUU");
		assertEquals("TTTT", rna);
	}
	
	@Test
	public void testACGUToACGT() throws IllegalSymbolException {
		String rna = Nucleotidies.rnaToDna("ACGU");
		assertEquals("ACGT", rna);
	}
}
