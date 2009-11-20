package bio.pih.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.seq.LightweightSymbolList;
import bio.pih.seq.op.LightweightIOTools;
import bio.pih.seq.op.LightweightStreamReader;

public class SequencesProvider {

	private final BufferedReader in;
	private boolean isFastaFile = false;

	LightweightStreamReader readFastaDNA;

	public SequencesProvider(BufferedReader in) throws IOException {
		this.in = in;
		this.in.mark(1);
		String firstLine = this.in.readLine();
		if (firstLine == null) {
			return;
		}
		this.in.reset();

		if (firstLine.charAt(0) == '>') {
			isFastaFile = true;
			readFastaDNA = LightweightIOTools.readFastaDNA(this.in, null);
		}
	}

	public synchronized boolean hasNext() throws IOException {
		if (isFastaFile) {
			return readFastaDNA.hasNext();
		}
		return in.ready();
	}

	public synchronized SymbolList getNextSequence() throws IOException, NoSuchElementException, BioException {
		if (isFastaFile) {
			return getNextFastaSequence();
		}
		return getNextLiteralSequence();
	}

	/**
	 * Read each line of the input stream, and each line will be considered a different sequence.
	 * 
	 * @param in
	 * @return {@link List} of {@link SymbolList} containing the sequences read. Or <code>null</code> if it does not have more sequences.
	 * @throws IllegalSymbolException
	 * @throws IOException
	 */
	private synchronized SymbolList getNextLiteralSequence() throws IllegalSymbolException, IOException {
		String seqString = in.readLine();
		if (seqString == null) {
			return null;
		}
		seqString = seqString.trim();
		while (seqString.length() == 0 && in.ready()) {
			seqString = in.readLine();
			if (seqString == null) {
				return null;
			}
			seqString = seqString.trim();
		}
		
		if (seqString.length() == 0) {
			return null;
		}
		
		return LightweightSymbolList.createDNA(seqString);
	}

	/**
	 * Read the file as a FASTA File, parsing it and returning the sequences.
	 * 
	 * @param in
	 * @return {@link List} of {@link SymbolList} containing the sequences read.
	 * @throws NoSuchElementException
	 * @throws BioException
	 */
	private SymbolList getNextFastaSequence() throws NoSuchElementException, BioException {
		return readFastaDNA.nextRichSequence();
	}
}
