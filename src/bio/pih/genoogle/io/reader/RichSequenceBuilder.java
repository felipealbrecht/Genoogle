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
	
	private int version;
	private boolean versionSeen;
	private double seqVersion = 0.0;
	private String accession;
	private String description;
	private String identifier;
	private String name;
	private Alphabet alphabet;
	private String sequence;

	/**
	 * Sets the sequence info back to default values, ie. in order to start constructing a new
	 * sequence from scratch.
	 */
	public void startSequence() {
		this.alphabet = null;
		this.sequence = null;
		this.version = 0;
		this.versionSeen = false;
		this.seqVersion = 0.0;
		this.accession = null;
		this.description = null;
		this.identifier = null;
		this.name = null;
	}

	public void setVersion(int version) throws ParseException {
		if (this.versionSeen) {
			throw new ParseException("Current BioEntry already has a version");
		}
		try {
			this.version = version;
			this.versionSeen = true;
		} catch (NumberFormatException e) {
			throw new ParseException("Could not parse version as an integer");
		}
	}


	public void setAccession(String accession) throws ParseException {
		if (accession == null)
			throw new ParseException("Accession cannot be null");
		this.accession = accession;
	}



	public void setDescription(String description) throws ParseException {
		if (this.description != null)
			throw new ParseException("Current BioEntry already has a description");
		this.description = description;
	}




	public void setIdentifier(String identifier) throws ParseException {
		if (identifier == null)
			throw new ParseException("Identifier cannot be null");
		if (this.identifier != null)
			throw new ParseException("Current BioEntry already has a identifier");
		this.identifier = identifier;
	}



	public void setName(String name) throws ParseException {
		if (name == null)
			throw new ParseException("Name cannot be null");
		if (this.name != null)
			throw new ParseException("Current BioEntry already has a name");
		this.name = name;
	}

	
	public void setSequence(String sequence) {
		this.sequence = sequence;		
	}
	

	public void setAlphabet(Alphabet alphabet) {
		this.alphabet = alphabet;
	}
	
	public void endSequence() throws ParseException {
		if (this.sequence == null)
			throw new ParseException("Sequence String hasnot been supplied");
		if (this.name == null)
			throw new ParseException("Name has not been supplied");
		if (this.accession == null)
			throw new ParseException("No accessions have been supplied");
	}

	public RichSequence makeRichSequence() throws ParseException, IllegalSymbolException {
		this.endSequence();

		RichSequence rs = new RichSequence(this.alphabet, this.sequence, this.name, this.accession, this.version, seqVersion);
		return rs;
	}


}
