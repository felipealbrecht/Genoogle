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

import junit.framework.TestCase;

import org.junit.Test;

import bio.pih.genoogle.io.reader.IOTools;
import bio.pih.genoogle.io.reader.ParseException;
import bio.pih.genoogle.io.reader.RichSequenceStreamReader;
import bio.pih.genoogle.seq.DNAAlphabet;
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
		RichSequenceStreamReader reader = IOTools.readFasta(new BufferedReader(sr), DNAAlphabet.SINGLETON);

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
		RichSequenceStreamReader reader = IOTools.readFasta(new BufferedReader(sr), DNAAlphabet.SINGLETON);

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
		RichSequenceStreamReader reader = IOTools.readFasta(new BufferedReader(sr), DNAAlphabet.SINGLETON);

		RichSequence richSequence = reader.nextRichSequence();
		assertEquals("unknow", richSequence.getType());
		assertEquals("", richSequence.getName());
		assertEquals("Sequence_X", richSequence.getDescription());
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

	String influenzaSequence = ">gb|FJ966082:1-1701| /Human/HA/H1N1/USA/2009/04/01/hemagglutinin[Influenza A virus (A/California/04/2009(H1N1))]\n"
			+ "ATGAAGGCAATACTAGTAGTTCTGCTATATACATTTGCAACCGCAAATGCAGACACATTATGTATAGGTT\n"
			+ "ATCATGCGAACAATTCAACAGACACTGTAGACACAGTACTAGAAAAGAATGTAACAGTAACACACTCTGT\n";

	@Test
	public void testInfluenzaSequence() throws NoSuchElementException, IOException, ParseException,
			IllegalSymbolException {
		StringReader sr = new StringReader(influenzaSequence);
		RichSequenceStreamReader reader = IOTools.readFasta(new BufferedReader(sr), DNAAlphabet.SINGLETON);

		RichSequence richSequence = reader.nextRichSequence();
		assertEquals("gb", richSequence.getType());
		assertEquals("FJ966082:1-1701", richSequence.getName());
		assertEquals(" /Human/HA/H1N1/USA/2009/04/01/hemagglutinin[Influenza A virus (A/California/04/2009(H1N1))]",
				richSequence.getDescription());
		assertEquals("", richSequence.getGi());
		assertEquals("", richSequence.getAccession());
		assertEquals(
				"ATGAAGGCAATACTAGTAGTTCTGCTATATACATTTGCAACCGCAAATGCAGACACATTATGTATAGGTTATCATGCGAACAATTCAACAGACACTGTAGACACAGTACTAGAAAAGAATGTAACAGTAACACACTCTGT",
				richSequence.seqString());
	}

	String fiocruzHeader = ">NP_059666|NP_059666 putative cytochrome oxidase III [Plasmodium falciparum])\n"
			+ "ATGAAGGCAATACTAGTAGTTCTGCTATATACATTTGCAACCGCAAATGCAGACACATTATGTATAGGTTATCATGCGAACAATTCAACAGACACTGTAGACACAGTACTAGAAAAGAATGTAACAGTAACACACTCTGT";
	@Test
	public void testFiocruzSequence() throws NoSuchElementException, IOException, ParseException,
			IllegalSymbolException {
		StringReader sr = new StringReader(fiocruzHeader);
		RichSequenceStreamReader reader = IOTools.readFasta(new BufferedReader(sr), DNAAlphabet.SINGLETON);

		RichSequence richSequence = reader.nextRichSequence();
		assertEquals("NP_059666", richSequence.getType());
		assertEquals("", richSequence.getName());
		assertEquals("NP_059666 putative cytochrome oxidase III [Plasmodium falciparum])", richSequence.getDescription());
		assertEquals("", richSequence.getGi());
		assertEquals("", richSequence.getAccession());
		assertEquals(
				"ATGAAGGCAATACTAGTAGTTCTGCTATATACATTTGCAACCGCAAATGCAGACACATTATGTATAGGTTATCATGCGAACAATTCAACAGACACTGTAGACACAGTACTAGAAAAGAATGTAACAGTAACACACTCTGT",
				richSequence.seqString());
	}

}
