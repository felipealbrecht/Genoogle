/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io.reader;

import bio.pih.genoogle.seq.Alphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.RichSequence;

/**
 * Constructs a Lightweight sequence builder.
 *
 * @author Felipe Albrecht
 * @since 1.5
 */
public class RichSequenceBuilder {

	private Alphabet alphabet;
	private String type;
	private String accession;
	private String description;
	private String gi;
	private String name;
	private String sequence;
	private String header;

	/**
	 * Sets the sequence info back to default values, ie. in order to start constructing a new
	 * sequence from scratch.
	 */
	public void startSequence() {
		this.type = null;
		this.alphabet = null;
		this.sequence = null;
		this.accession = null;
		this.description = null;
		this.gi = null;
		this.name = null;
		this.header = null;
	}

	public void setAccession(String accession)  {
		this.accession = accession;
	}

	public void setDescription(String description)  {
		this.description = description;
	}

	public void setGi(String gi) {
		this.gi = gi;
	}

	public void setName(String name) {
		this.name = name;
	}


	public void setSequence(String sequence) {
		this.sequence = sequence;
	}


	public void setAlphabet(Alphabet alphabet) {
		this.alphabet = alphabet;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public void endSequence() throws ParseException {
		if (this.sequence == null) {
			throw new ParseException("Sequence content has not been supplied");
		}
		if (this.header == null) {
			throw new ParseException("Sequence header has not been supplied");
		}
		if (this.description == null) {
			throw new ParseException("Description does not have been supplied: " + this.header);
		}
	}

	public RichSequence makeRichSequence() throws ParseException, IllegalSymbolException {
		this.endSequence();

		RichSequence rs = new RichSequence(this.alphabet, this.sequence, this.name, this.type, this.accession, this.gi, this.description, this.header);
		return rs;
	}

}
