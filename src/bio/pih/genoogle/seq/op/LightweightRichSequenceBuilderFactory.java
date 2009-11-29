package bio.pih.genoogle.seq.op;

import org.biojava.bio.seq.io.SequenceBuilder;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;

/**
 * {@link RichSequenceBuilderFactory} that uses the {@link LightweightRichSequenceBuilder}.
 * 
 * @author albrecht
 */
public class LightweightRichSequenceBuilderFactory implements RichSequenceBuilderFactory {

	public SequenceBuilder makeSequenceBuilder() {
		return new LightweightSequenceBuilder();
	}
}
