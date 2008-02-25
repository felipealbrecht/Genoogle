package bio.pih.tests.seq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.junit.Test;

import bio.pih.seq.op.LightweightIOTools;
import bio.pih.seq.op.LightweightStreamReader;


/**
 * @author albrecht
 *
 */
public class LightweightSequencesTest extends TestCase {

	/**
	 * @throws FileNotFoundException
	 * @throws NoSuchElementException
	 * @throws BioException
	 */
	@Test
	public void testReadFastFile() throws FileNotFoundException, NoSuchElementException, BioException {
		
		BufferedReader is = new BufferedReader(new FileReader("data" + File.separator + "populator" + File.separator + "test_sequences_dataset_dna_500_200_700.fasta"));

		
		LightweightStreamReader readFastaDNA = LightweightIOTools.readFastaDNA(is, null);
		
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
			assertEquals(name+""+pos, s.getName());
			if (pos == 100) {
				assertEquals(sequence_100, s.seqString().toLowerCase());
			}
			
			if (pos == 499) {
				assertEquals(sequence_499, s.seqString().toLowerCase());
			}
		}
	}
}
