package bio.pih.seq.op;

import java.io.BufferedReader;

import org.biojavax.Namespace;


/**
 * @author albrecht
 *
 */
public class LightweightIOTools {
	
    /**
     * @param br
     * @param ns
     * @return
     */
    public static LightweightStreamReader readFastaDNA(BufferedReader br, Namespace ns) {
        return new LightweightStreamReader(br,
                new LightweightFastaFormat(),
                org.biojavax.bio.seq.RichSequence.IOTools.getDNAParser(),
                new LightweightRichSequenceBuilderFactory(),
                ns);
    }
	
}
