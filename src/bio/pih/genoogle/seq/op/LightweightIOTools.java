package bio.pih.genoogle.seq.op;

import java.io.BufferedReader;

import org.biojava.bio.seq.io.StreamReader;
import org.biojavax.Namespace;


/**
 * @author albrecht
 *
 */
public class LightweightIOTools {
	
    /**
     * @param br
     * @param ns
     * @return {@link StreamReader} of the DNA FASTA files. 
     */
    public static LightweightStreamReader readFastaDNA(BufferedReader br, Namespace ns) {
        return new LightweightStreamReader(br,
                new LightweightFastaFormat(),
                org.biojavax.bio.seq.RichSequence.IOTools.getDNAParser(),
                new LightweightRichSequenceBuilderFactory(),
                ns);
    }
	
}
