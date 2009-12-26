/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import bio.pih.genoogle.io.reader.IOTools;
import bio.pih.genoogle.io.reader.ParseException;
import bio.pih.genoogle.io.reader.RichSequenceStreamReader;
import bio.pih.genoogle.seq.Alphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.seq.Sequence;
import bio.pih.genoogle.seq.SymbolList;

public class SequencesProvider {

	private final BufferedReader in;
	private boolean isFastaFile = false;

	RichSequenceStreamReader readFasta;
	private final Alphabet alphabet;

	public SequencesProvider(BufferedReader in, Alphabet alphabet) throws IOException {
		this.in = in;
		this.alphabet = alphabet;
		this.in.mark(1);
		String firstLine = this.in.readLine();
		if (firstLine == null) {
			return;
		}
		this.in.reset();

		if (firstLine.charAt(0) == '>') {
			isFastaFile = true;
			readFasta = IOTools.readFasta(this.in, alphabet);
		}
	}

	public synchronized boolean hasNext() throws IOException {
		if (isFastaFile) {
			return readFasta.hasNext();
		}
		return in.ready();
	}

	public synchronized SymbolList getNextSequence() throws IllegalSymbolException, IOException, NoSuchElementException, ParseException {
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
		
		return new LightweightSymbolList(alphabet, seqString);
	}

	/**
	 * Read the file as a FASTA File, parsing it and returning the sequences.
	 * 
	 * @param in
	 * @return {@link List} of {@link SymbolList} containing the sequences read.
	 */
	private Sequence getNextFastaSequence() throws NoSuchElementException, IOException, ParseException, IllegalSymbolException {
		return readFasta.nextRichSequence();
	}
}
