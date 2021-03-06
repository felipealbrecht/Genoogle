/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io.reader;

import java.io.BufferedReader;

import bio.pih.genoogle.seq.Alphabet;


/**
 * @author albrecht
 *
 */
public class IOTools {
	
    /**
     * Tools to read a fasta file.
     * @param br
     * @return {@link RichSequenceStreamReader} of the DNA FASTA files. 
     */
    public static RichSequenceStreamReader readFasta(BufferedReader br, Alphabet alphabet) {
        return new RichSequenceStreamReader(br,
                new FastaFormat(alphabet),
                new RichSequenceBuilderFactory());
    }
	
}
