package bio.pih.seq.op;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;

import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.io.FastaFormat;
import org.biojavax.bio.seq.io.RichSeqIOListener;

import bio.pih.seq.LightweightSymbolList;

/**
 * A extended FastaFormat for LightweightSymbolList
 * 
 * @author albrecht
 * 
 */
public class LightweightFastaFormat extends FastaFormat {

	/**
	 * {@inheritDoc} If namespace is null, then the namespace of the sequence in the fasta is used. If the namespace is null and so is the namespace of the sequence in the fasta, then the default namespace is used.
	 */
	public boolean readRichSequence(BufferedReader reader, SymbolTokenization symParser, RichSeqIOListener rsiol, Namespace ns) throws IllegalSymbolException, IOException, ParseException {

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

		rsiol.startSequence();

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
			String namespace = m.group(3);
			String accession = m.group(4);
			String verString = m.group(6);
			int version = verString == null ? 0 : Integer.parseInt(verString);
			name = m.group(7);
			if (name == null)
				name = accession;

			rsiol.setAccession(accession);
			rsiol.setVersion(version);
			if (gi != null)
				rsiol.setIdentifier(gi);
			if (ns == null)
				rsiol.setNamespace((Namespace) RichObjectFactory.getObject(SimpleNamespace.class, new Object[] { namespace }));
			else
				rsiol.setNamespace(ns);
		} else {
			rsiol.setAccession(name);
			rsiol.setNamespace((ns == null ? RichObjectFactory.getDefaultNamespace() : ns));
		}
		rsiol.setName(name);
		if (!this.getElideComments())
			rsiol.setDescription(desc);

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
		if (!this.getElideSymbols()) {
			try {
				LightweightSymbolList sl = LightweightSymbolList.constructLightweightSymbolList(symParser.getAlphabet(), symParser, seq.toString().replaceAll("\\s+", "").replaceAll("[\\.|~]", "-"));
				((LightweightSequenceBuilder)rsiol).addSymbols(sl);
			} catch (Exception e) {
				String message = ParseException.newMessage(this.getClass(), name, gi, "problem parsing symbols", seq.toString());
				throw new ParseException(e, message);
			}
		}

		rsiol.endSequence();

		return line != null;
	}
}
