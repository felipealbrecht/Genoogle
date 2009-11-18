package bio.pih.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.biojavax.bio.seq.RichSequence;

import bio.pih.seq.LightweightSymbolList;
import bio.pih.seq.op.LightweightIOTools;
import bio.pih.seq.op.LightweightStreamReader;

import com.google.common.collect.Lists;

/**
 * Read the sequences from a input stream.
 * 
 * If the first checks if the input stream contains sequences in the fasta format, seeing if the fist character is '>'.
 * If it is, this class will manage the input stream as a Fasta file, and parsing it as one.
 * Otherwise, will read each line, and each line will be considered a different sequence.
 * 
 * @author albrecht
 *
 */
public class InputSequencesReader {

	static public List<SymbolList> read(BufferedReader in) throws IOException, NoSuchElementException, BioException {		
		in.mark(1);
		String firstLine = in.readLine();
		in.reset();
		
		if (firstLine.charAt(0) == '>') {			
			return readFasta(in);
		}

		return readSequences(in);
	}

	/**
	 * Read each line of the input stream, and each line will be considered a different sequence.
	 * @param in
	 * @return {@link List} of {@link SymbolList} containing the sequences read.
	 * @throws IOException
	 * @throws IllegalSymbolException
	 */
	private static List<SymbolList> readSequences(BufferedReader in) throws IOException, IllegalSymbolException {
		List<SymbolList> sequences = Lists.newLinkedList();
		while (in.ready()) {
			in.reset();					
			String seqString = in.readLine();
			seqString = seqString.trim();
			if (seqString.length() == 0) {
				continue;
			}
			SymbolList sequence = LightweightSymbolList.createDNA(seqString);
			sequences.add(sequence);
		}
		return sequences;
	}

	/**
	 * Read the file as a FASTA File, parsing it and returning the sequences. 
	 * @param in
	 * @return {@link List} of {@link SymbolList} containing the sequences read.
	 * @throws NoSuchElementException
	 * @throws BioException
	 */
	private static List<SymbolList> readFasta(BufferedReader in) throws NoSuchElementException, BioException {
		List<SymbolList> sequences = Lists.newLinkedList();

		LightweightStreamReader readFastaDNA = LightweightIOTools.readFastaDNA(in, null);
		while (readFastaDNA.hasNext()) {
			RichSequence s = readFastaDNA.nextRichSequence();
			sequences.add(s);
		}
		return sequences;
	}

}
