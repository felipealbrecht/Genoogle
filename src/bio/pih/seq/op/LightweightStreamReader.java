package bio.pih.seq.op;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.Namespace;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.seq.io.RichSequenceBuilder;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;
import org.biojavax.bio.seq.io.RichSequenceFormat;
import org.biojavax.bio.seq.io.RichStreamReader;

/**
 * @author albrecht
 * 
 */
public class LightweightStreamReader extends RichStreamReader {

	/**
	 * The symbol parser.
	 */
	protected Namespace ns;

	/**
	 * The symbol parser.
	 */
	protected SymbolTokenization symParser;

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
	 * @param sf
	 * @param ns
	 */
	public LightweightStreamReader(BufferedReader reader, RichSequenceFormat format, SymbolTokenization symParser, RichSequenceBuilderFactory sf, Namespace ns) {
		super(reader, format, symParser, sf, ns);
	}

	/**
	 * @param is
	 * @param format
	 * @param symParser
	 * @param sf
	 * @param ns
	 */
	public LightweightStreamReader(InputStream is, RichSequenceFormat format, SymbolTokenization symParser, RichSequenceBuilderFactory sf, Namespace ns) {
		super(is, format, symParser, sf, ns);
	}

	@Override
	public Sequence nextSequence() throws NoSuchElementException, BioException {
		if (!hasNext())
			throw new NoSuchElementException("Stream is empty");
		try {
			RichSequenceBuilder builder = (RichSequenceBuilder) sf.makeSequenceBuilder();
			moreSequenceAvailable = format.readRichSequence(reader, symParser, builder, ns);
			return builder.makeRichSequence();
		} catch (Exception e) {
			throw new BioException("Could not read sequence", e);
		}
	}
	

	@Override
	public BioEntry nextBioEntry() throws NoSuchElementException, BioException {
		return this.nextRichSequence();
	}


	@Override
	public boolean hasNext() {
		return moreSequenceAvailable;
	}

}
