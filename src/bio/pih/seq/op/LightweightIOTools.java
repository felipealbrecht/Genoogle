package bio.pih.seq.op;

import java.io.BufferedReader;

import org.biojavax.Namespace;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;
import org.biojavax.bio.seq.io.RichStreamReader;
import org.biojavax.bio.seq.io.SimpleRichSequenceBuilderFactory;


public class LightweightIOTools {
	
    public static LightweightStreamReader readFastaDNA(BufferedReader br, Namespace ns) {
        return new LightweightStreamReader(br,
                new LightweightFastaFormat(),
                org.biojavax.bio.seq.RichSequence.IOTools.getDNAParser(),
                new LightweightRichSequenceBuilderFactory(),
                ns);
    }
	
}
