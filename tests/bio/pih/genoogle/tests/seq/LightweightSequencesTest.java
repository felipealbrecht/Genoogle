/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.tests.seq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.junit.Test;

import bio.pih.genoogle.io.reader.IOTools;
import bio.pih.genoogle.io.reader.ParseException;
import bio.pih.genoogle.io.reader.RichSequenceStreamReader;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.Sequence;


/**
 * @author albrecht
 *
 */
public class LightweightSequencesTest extends TestCase {

	@Test
	public void testReadFastFile() throws NoSuchElementException, IOException, ParseException, IllegalSymbolException {
		
		BufferedReader is = new BufferedReader(new FileReader("data" + File.separator + "populator" + File.separator + "test_sequences_dataset_dna_500_200_700.fasta"));

		
		RichSequenceStreamReader readFastaDNA = IOTools.readFastaDNA(is);
		
		String sequence_100 = "gaacccggcgagagaaggttgacgcgtacccgttaatattgatgttacgactagcgcagttcctaacgcactcggtgtcg" +
							"ccagaagagagctagtgacgacacgtatcctggagcgacaccactaagcagagttgtccccaagaactgcggtccctgcg" +
							"ggattgggtcacccttcaggtgacgactaaatagggcaaccgatagcaggaggtcagccgcggagcg";
		
		String sequence_499 = "tcatgggctcgtgactccaatttctgtgcactgttgatgcctttgagtattttcatcacgctaggacctactgcagacgc" +
							"acgcaccctccaaggttgaggatagttggttcggtccctccgttgaggcaccaggtcccgttagtctggtgtcttccaca" +
							"gagcacgggtggaagtgcattgggcaccacggccgttgcactctttcgtccgatagttaaaacgtttttggctccgagtg" +
							"acatc";
		
		Sequence s;
		String name = "RandomSequence_";
		int pos = -1;
		
		while (readFastaDNA.hasNext()) {				
			pos++;
			s = readFastaDNA.nextRichSequence();
			assertEquals(name+""+pos+".0", s.getName());
			if (pos == 100) {
				assertEquals(sequence_100, s.seqString().toLowerCase());
			}
			
			if (pos == 499) {
				assertEquals(sequence_499, s.seqString().toLowerCase());
			}
		}
	}
}
