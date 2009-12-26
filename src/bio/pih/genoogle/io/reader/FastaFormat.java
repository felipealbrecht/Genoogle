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

		StringBuffer seq = new StringBuffer();
		boolean hasMoreSeq = true;
		while (hasMoreSeq) {
			reader.mark(500);
			line = reader.readLine();
			if (line != null) {
				line = line.trim();
				if (line.length() > 0 && line.charAt(0) == '>') {
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

	public void processHeader(String line, RichSequenceBuilder rsiol) throws IOException, ParseException {
		Matcher matcher = giHeader.matcher(line);
		if (matcher.matches()) {
			rsiol.setType("gi");
			rsiol.setGi(matcher.group(1));
			rsiol.setName(matcher.group(2));
			rsiol.setAccession(matcher.group(3));
			rsiol.setDescription(matcher.group(4));
			return;
		}

		matcher = lclHeader.matcher(line);
		if (matcher.matches()) {
			rsiol.setType("lcl");
			rsiol.setName(matcher.group(1));
			rsiol.setDescription(matcher.group(3));
			return;
		}

		line = line.substring(1);
		String[] strings = line.split("\\|");
		
		if (strings.length == 0) {
			rsiol.setDescription(line);			
		} else {
			rsiol.setType(strings[0]);
		}
		
		if (strings.length > 2) {
			rsiol.setName(strings[1]);
		}
		if (strings.length > 3) {
			rsiol.setGi(strings[2]);
		}
		if (strings.length > 4) {
			rsiol.setAccession(strings[3]);
		}
		
		if (strings.length > 1) {
			rsiol.setDescription(strings[strings.length - 1]);
		}
	}

	// TODO:
	// NBRF PIR pir||entry
	// Patents pat|country|number
	// GenInfo Backbone Id bbs|number
	// General database identifier gnl|database|identifier
	// NCBI Reference Sequence ref|accession|locus

}
