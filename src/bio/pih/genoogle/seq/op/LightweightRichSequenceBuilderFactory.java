/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.seq.op;

import org.biojava.bio.seq.io.SequenceBuilder;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;

/**
 * {@link RichSequenceBuilderFactory} that uses the {@link LightweightSequenceBuilder}
 * 
 * @author albrecht
 */
public class LightweightRichSequenceBuilderFactory implements RichSequenceBuilderFactory {

	public SequenceBuilder makeSequenceBuilder() {
		return new LightweightSequenceBuilder();
	}
}
