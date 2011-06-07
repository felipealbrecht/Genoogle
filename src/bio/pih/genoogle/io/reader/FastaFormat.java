/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import bio.pih.genoogle.seq.Alphabet;

/**
 * A extended FastaFormat for LightweightSymbolList. Strongly basead on
 * http://code.open-bio.org/svnweb
 * /index.cgi/biojava/view/biojava-live/trunk/src/org/biojavax/bio/seq/io/FastaFormat.java?rev=4800
 * 
 * @author albrecht
 * 
 */
public class FastaFormat implements RichSequenceFormat {

	// TODO: Put this value as parameter.
	private static final int READ_AHEAD_LIMIT =  (int) Math.pow(2, 20); // Every line read, will look ahead 1M 

	static Logger logger = Logger.getLogger(FastaFormat.class.getName());

	protected static final Pattern hp = Pattern.compile(">(\\S+)(\\s+(.*))?");
	protected static final Pattern dp = Pattern.compile("^(gi\\|(\\d+)\\|)*(\\S+)\\|(\\S+?)(\\.(\\d+))*\\|(\\S+)");

	private final Alphabet alphabet;
	
	public FastaFormat(Alphabet alphabet) {
		this.alphabet = alphabet;
		
	}

	public boolean readRichSequence(BufferedReader reader, RichSequenceBuilder builder) throws IOException,
			ParseException {

		String line = reader.readLine();
		if (line == null) {
			throw new IOException("Premature stream end");
		}
		while (line.length() == 0) {
			line = reader.readLine();
			if (line == null) {
				throw new IOException("Premature stream end");
			}
		}
		if (!line.startsWith(">")) {
			throw new IOException("Stream does not appear to contain FASTA formatted data: " + line);
		}

		builder.startSequence();

		processHeader(line, builder);
		
		builder.setHeader(line.substring(1));

		StringBuffer seq = new StringBuffer();
		boolean hasMoreSeq = true;
		while (hasMoreSeq) {
			reader.mark(READ_AHEAD_LIMIT);
			// TODO: Not read with readline, but read ant put into a buffer.
			line = reader.readLine();
			if (line != null) {
				line = line.trim();
				if (line.length() > 0 && line.charAt(0) == '>') {
					logger.debug("New header: '" + line + "'.");
					if (line.length() > READ_AHEAD_LIMIT) {
						throw new IOException("Sequence header length ("+line.length()+") too long. The limit is " + READ_AHEAD_LIMIT+ ".");
					}
					reader.reset();
					hasMoreSeq = false;
				} else {
					seq.append(line);
				}
			} else {
				hasMoreSeq = false;
			}
		}

		builder.setSequence(seq.toString().replaceAll("\\s+", "").replaceAll("[\\.|~]", "-"));

		builder.setAlphabet(alphabet);

		builder.endSequence();

		return line != null;
	}

	/**
	 * GenBank gi|gi-number|gb|accession|locus 
	 * EMBL Data Library gi|gi-number|emb|accession|locus
	 * DDBJ, DNA Database of Japan gi|gi-number|dbj|accession|locus
	 */
	protected static final Pattern giHeader = Pattern.compile(">gi\\|(\\d+)\\|(\\S+)\\|(\\S+)\\|(\\s+(.*))?");

	/**
	 * Local Sequence identifier lcl|identifier
	 */
	protected static final Pattern lclHeader = Pattern.compile(">lcl\\|(\\S+)(\\|(\\s*(.*))?)?");
	
	/**
	 * >EMBLCDS:BAJ49870 BAJ49870.1 Candidatus Caldiarchaeum subterraneum archaeal cell division control protein 6
	 */	
	protected static final Pattern emblHeader = Pattern.compile(">(\\S+):(\\S+)(\\s+)(\\S+\\.\\d)(\\s+)(\\S+(.*))");
	
	/**
	 * >contig00001_1 length=19730
	 */	
	protected static final Pattern ecoli = Pattern.compile(">contig(\\S+)(\\s+)(\\S+(.*))");

	public void processHeader(String line, RichSequenceBuilder sequenceBuilder) throws IOException, ParseException {
		Matcher matcher = giHeader.matcher(line);
		if (matcher.matches()) {
			sequenceBuilder.setType("gi");
			sequenceBuilder.setGi(matcher.group(1));
			sequenceBuilder.setName(matcher.group(2));
			sequenceBuilder.setAccession(matcher.group(3));
			sequenceBuilder.setDescription(matcher.group(4));
			return;
		}

		matcher = lclHeader.matcher(line);
		if (matcher.matches()) {
			sequenceBuilder.setType("lcl");
			sequenceBuilder.setName(matcher.group(1));
			sequenceBuilder.setDescription(matcher.group(3));
			return;
		}
		
		matcher = emblHeader.matcher(line);
		if (matcher.matches()) {
			sequenceBuilder.setType(matcher.group(1));
			sequenceBuilder.setGi(matcher.group(2));
			sequenceBuilder.setName(matcher.group(4));
			sequenceBuilder.setDescription(matcher.group(6));			
			return;
		}
		matcher = ecoli.matcher(line);
		if (matcher.matches()) {
			sequenceBuilder.setType("contig");
			sequenceBuilder.setName(matcher.group(1));
			sequenceBuilder.setDescription(matcher.group(3));
			return;
		}

		line = line.substring(1);
		String[] strings = line.split("\\|");
		
		if (strings.length == 0) {
			sequenceBuilder.setDescription(line);			
		} else {
			sequenceBuilder.setType(strings[0]);
		}
		
		if (strings.length > 2) {
			sequenceBuilder.setName(strings[1]);
		}
		if (strings.length > 3) {
			sequenceBuilder.setGi(strings[2]);
		}
		if (strings.length > 4) {
			sequenceBuilder.setAccession(strings[3]);
		}
		
		if (strings.length > 1) {
			sequenceBuilder.setDescription(strings[strings.length - 1]);
		}
	}

	// TODO:
	// NBRF PIR pir||entry
	// Patents pat|country|number
	// GenInfo Backbone Id bbs|number
	// General database identifier gnl|database|identifier
	// NCBI Reference Sequence ref|accession|locus

}
