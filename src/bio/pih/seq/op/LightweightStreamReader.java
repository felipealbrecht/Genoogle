package bio.pih.seq.op;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.Namespace;
import org.biojavax.bio.seq.io.RichSequenceBuilder;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;
import org.biojavax.bio.seq.io.RichSequenceFormat;
import org.biojavax.bio.seq.io.RichStreamReader;

public class LightweightStreamReader extends RichStreamReader {

	public LightweightStreamReader(BufferedReader reader,
			RichSequenceFormat format, SymbolTokenization symParser,
			RichSequenceBuilderFactory sf, Namespace ns) {
		super(reader, format, symParser, sf, ns);
	}
	
	public LightweightStreamReader(InputStream is, RichSequenceFormat format,
			SymbolTokenization symParser, RichSequenceBuilderFactory sf,
			Namespace ns) {
		super(is, format, symParser, sf, ns);
	}
	
    public Sequence nextSequence() throws NoSuchElementException, BioException {
        if(!hasNext())
            throw new NoSuchElementException("Stream is empty");
        try {        	
            RichSequenceBuilder builder = (RichSequenceBuilder)sf.makeSequenceBuilder();
            moreSequenceAvailable = format.readRichSequence(reader, symParser, builder, ns);
            return builder.makeRichSequence();
        } catch (Exception e) {
            throw new BioException("Could not read sequence",e);
        }
    }

}
