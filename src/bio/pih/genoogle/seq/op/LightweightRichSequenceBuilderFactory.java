package bio.pih.genoogle.seq.op;

import org.biojava.bio.seq.io.SequenceBuilder;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;

/**
 * @author albrecht
 *
 */
public class LightweightRichSequenceBuilderFactory implements RichSequenceBuilderFactory {

	public SequenceBuilder makeSequenceBuilder() {
		return new LightweightSequenceBuilder(); 
	}
}
