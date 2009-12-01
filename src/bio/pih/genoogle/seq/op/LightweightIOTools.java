/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq.op;

import java.io.BufferedReader;

import org.biojava.bio.seq.io.StreamReader;
import org.biojavax.Namespace;
import org.biojavax.bio.seq.RichSequence.IOTools;


/**
 * @author albrecht
 *
 */
public class LightweightIOTools {
	
    /**
     * A {@link IOTools} that uses the Genoogle clases.
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
