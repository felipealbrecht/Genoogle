/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io.reader;


/**
 * Factory class to {@link RichSequenceBuilder}.
 * 
 * @author albrecht
 */
public class RichSequenceBuilderFactory {

	public RichSequenceBuilder makeSequenceBuilder() {
		return new RichSequenceBuilder();
	}
}
