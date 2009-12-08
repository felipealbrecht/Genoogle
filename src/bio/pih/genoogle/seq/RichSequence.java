package bio.pih.genoogle.seq;

public class RichSequence extends Sequence {
	

	private final String accession;

	private final int version;

	private final double seqVersion;

	public RichSequence(Alphabet alphabet, String sequence, String name, String accession, int version,  double seqVersion) throws IllegalSymbolException {
		super(name, alphabet, sequence);
		this.accession = accession;
		this.version = version;
		this.seqVersion = seqVersion;
	}

	public String getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getVersion() {
		// TODO Auto-generated method stub
		return 1;
	}

	public String getAccession() {
		// TODO Auto-generated method stub
		return null;
	}

}