package bio.pih.genoogle.seq;

public class Sequence extends LightweightSymbolList {

	private final String name;

	public Sequence(Sequence parent, int begin, int end) {
		super(parent, begin, end);
		this.name = parent.getName() + "_" + begin + "_" + end;
	}

	public Sequence(Alphabet alphabet, String seq, String name) throws IllegalSymbolException {
		super(alphabet, seq);
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
