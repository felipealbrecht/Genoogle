/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.tests.io.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.NoSuchElementException;

import org.junit.Test;

import junit.framework.TestCase;

import bio.pih.genoogle.io.reader.IOTools;
import bio.pih.genoogle.io.reader.ParseException;
import bio.pih.genoogle.io.reader.RichSequenceStreamReader;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.RichSequence;

public class RichSequenceFastaFileReaderTest extends TestCase {

	String giSequences = ">gi|6626248|gb|AE000657.1| Aquifex aeolicus VF5, complete genome\n"
			+ "TGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGT\n"
			+ "TGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGT\n"
			+ ">gi|114053012|ref|NM_001046239.1| Bos taurus CD36 antigen like (MGC137452), mRNA\n"
			+ "TGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGT\n"
			+ "TGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGT\n";

	@Test
	public void testGiFastaFormatReader() throws NoSuchElementException, IOException, ParseException,
			IllegalSymbolException {
		StringReader sr = new StringReader(giSequences);
		RichSequenceStreamReader reader = IOTools.readFastaDNA(new BufferedReader(sr));

		RichSequence richSequence = reader.nextRichSequence();
		assertEquals("gi", richSequence.getType());
		assertEquals("6626248", richSequence.getGi());
		assertEquals("gb", richSequence.getName());
		assertEquals("AE000657.1", richSequence.getAccession());
		assertEquals(" Aquifex aeolicus VF5, complete genome", richSequence.getDescription());
		assertEquals(
				"TGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGTTGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGT",
				richSequence.seqString());

		richSequence = reader.nextRichSequence();
		assertEquals("gi", richSequence.getType());
		assertEquals("114053012", richSequence.getGi());
		assertEquals("ref", richSequence.getName());
		assertEquals("NM_001046239.1", richSequence.getAccession());
		assertEquals(" Bos taurus CD36 antigen like (MGC137452), mRNA", richSequence.getDescription());
		assertEquals(
				"TGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGTTGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGT",
				richSequence.seqString());
	}

	String lclSequences = ">lcl|Sequence_X\n"
			+ "TGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGT\n"
			+ ">lcl|RandomSequence_494.0|RandomSequence_494 bla bla bla\n"
			+ "TGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGT\n";

	@Test
	public void testLclFastaFormatReader() throws NoSuchElementException, IOException, ParseException,
			IllegalSymbolException {
		StringReader sr = new StringReader(lclSequences);
		RichSequenceStreamReader reader = IOTools.readFastaDNA(new BufferedReader(sr));

		RichSequence richSequence = reader.nextRichSequence();
		assertEquals("lcl", richSequence.getType());
		assertEquals("Sequence_X", richSequence.getName());
		assertEquals("", richSequence.getDescription());
		assertEquals("TGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGT", richSequence.seqString());

		richSequence = reader.nextRichSequence();
		assertEquals("lcl", richSequence.getType());
		assertEquals("RandomSequence_494.0", richSequence.getName());
		assertEquals("RandomSequence_494 bla bla bla", richSequence.getDescription());
		assertEquals("TGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGT", richSequence.seqString());
	}
	
	String unknowSequences = ">unknow|Sequence_X\n"
		+ "TGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGT\n"
		+ ">unknow|Blah|Blum|Zum| bla bla bla\n"
		+ "TGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGT\n";

@Test
public void testUnknowFastaFormatReader() throws NoSuchElementException, IOException, ParseException,
		IllegalSymbolException {
	StringReader sr = new StringReader(unknowSequences);
	RichSequenceStreamReader reader = IOTools.readFastaDNA(new BufferedReader(sr));

	RichSequence richSequence = reader.nextRichSequence();
	assertEquals("unknow", richSequence.getType());
	assertEquals("Sequence_X", richSequence.getName());
	assertEquals("", richSequence.getDescription());
	assertEquals("", richSequence.getGi());
	assertEquals("", richSequence.getAccession());
	assertEquals("TGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGT", richSequence.seqString());

	richSequence = reader.nextRichSequence();
	assertEquals("unknow", richSequence.getType());
	assertEquals("Blah", richSequence.getName());
	assertEquals("Blum", richSequence.getGi());
	assertEquals("Zum", richSequence.getAccession());
	assertEquals(" bla bla bla", richSequence.getDescription());
	assertEquals("TGCAACGATGGACTGGATGCCCCAGGAAAAGGAAAGAGGTATAACCATAACCGTTGCAACGACCGCATGT", richSequence.seqString());
}
}
