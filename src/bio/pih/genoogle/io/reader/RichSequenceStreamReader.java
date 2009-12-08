/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.RichSequence;
import bio.pih.genoogle.seq.Sequence;

/**
 * A reader that read the sequences from a stream.
 * 
 * @author albrecht
 */
public class RichSequenceStreamReader {

	/**
	 * The sequence format.
	 */
	protected RichSequenceFormat format;

	/**
	 * The sequence-builder factory.
	 */
	protected RichSequenceBuilderFactory sf;

	/**
	 * The stream of data to parse.
	 */

	protected BufferedReader reader;

	/**
	 * Flag indicating if more sequences are available.
	 */
	protected boolean moreSequenceAvailable = true;

	/**
	 * @param reader
	 * @param format
	 * @param symParser
	 */
	public RichSequenceStreamReader(BufferedReader reader, RichSequenceFormat format, RichSequenceBuilderFactory sf) {
		this.reader = reader;
		this.format = format;
		this.sf = sf;
	}

	public Sequence nextSequence() throws NoSuchElementException, IOException, ParseException, IllegalSymbolException {
		return this.nextRichSequence();
	}

	public boolean hasNext() {
		return moreSequenceAvailable;
	}

	public RichSequence nextRichSequence() throws NoSuchElementException, IOException, ParseException, IllegalSymbolException {
		if (!hasNext()) {
			throw new NoSuchElementException("Stream is empty");
		}

		RichSequenceBuilder builder = (RichSequenceBuilder) sf.makeSequenceBuilder();
		moreSequenceAvailable = format.readRichSequence(reader, builder);
		return builder.makeRichSequence();
	}

}
