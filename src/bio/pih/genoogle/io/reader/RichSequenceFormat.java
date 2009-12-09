/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io.reader;

import java.io.BufferedReader;
import java.io.IOException;

public interface RichSequenceFormat {

	public boolean readRichSequence(BufferedReader reader, RichSequenceBuilder rsiol) throws IOException, ParseException;

}