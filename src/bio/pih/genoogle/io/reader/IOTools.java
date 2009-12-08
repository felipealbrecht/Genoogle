/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io.reader;

import java.io.BufferedReader;


/**
 * @author albrecht
 *
 */
public class IOTools {
	
    /**
     * A {@link IOTools} that uses the Genoogle clases.
     * @param br
     * @param ns
     * @return {@link StreamReader} of the DNA FASTA files. 
     */
    public static RichSequenceStreamReader readFastaDNA(BufferedReader br) {
        return new RichSequenceStreamReader(br,
                new FastaFormat(),
                new RichSequenceBuilderFactory());
    }
	
}
