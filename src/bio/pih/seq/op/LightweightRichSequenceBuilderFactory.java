package bio.pih.seq.op;

import org.biojava.bio.seq.io.SequenceBuilder;
import org.biojavax.bio.seq.io.RichSequenceBuilderFactory;
import org.biojavax.bio.seq.io.SimpleRichSequenceBuilderFactory;

public class LightweightRichSequenceBuilderFactory implements RichSequenceBuilderFactory {

	public SequenceBuilder makeSequenceBuilder() {
		return new LightweightSequenceBuilder(); 
	}
}
