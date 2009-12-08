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

/**
 * A extended FastaFormat for LightweightSymbolList. Strongly basead on
 * http://code.open-bio.org/svnweb
 * /index.cgi/biojava/view/biojava-live/trunk/src/org/biojavax/bio/seq/io/FastaFormat.java?rev=4800
 * 
 * @author albrecht
 * 
 */
public class FastaFormat implements RichSequenceFormat {

	protected static final Pattern hp = Pattern.compile(">(\\S+)(\\s+(.*))?");
	protected static final Pattern dp = Pattern.compile("^(gi\\|(\\d+)\\|)*(\\S+)\\|(\\S+?)(\\.(\\d+))*\\|(\\S+)$");

	public boolean readRichSequence(BufferedReader reader, RichSequenceBuilder builder) throws IOException, ParseException {

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
		
		builder.setSequence(seq.toString().replaceAll("\\s+","").replaceAll("[\\.|~]","-"));
		
		builder.endSequence();

		return line != null;
	}

	public void processHeader(String line, RichSequenceBuilder rsiol) throws IOException, ParseException {
		Matcher m = hp.matcher(line);
		if (!m.matches()) {
			throw new IOException("Stream does not appear to contain FASTA formatted data: " + line);
		}

		String name = m.group(1);
		String desc = m.group(3);
		String gi = null;

		m = dp.matcher(name);
		if (m.matches()) {
			gi = m.group(2);
			String accession = m.group(4);
			String verString = m.group(6);
			int version = verString == null ? 0 : Integer.parseInt(verString);
			name = m.group(7);
			if (name == null)
				name = accession;

			rsiol.setAccession(accession);
			rsiol.setVersion(version);
			if (gi != null) {
				rsiol.setIdentifier(gi);
			}
		} else {
			rsiol.setAccession(name);
		}
		rsiol.setName(name);
		rsiol.setDescription(desc);
	}
}
