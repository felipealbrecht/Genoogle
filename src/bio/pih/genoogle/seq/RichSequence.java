package bio.pih.genoogle.seq;

public class RichSequence extends Sequence {
	
	private static final long serialVersionUID = 8654973053346425861L;

	private final String accession;
	private final String gi;
	private final String description;
	private final String type;
	private final String header;


	public RichSequence(Alphabet alphabet, String sequence, String name, String type, String accession, String gi, String description, String header) throws IllegalSymbolException {
		super(alphabet, sequence, name);
		this.type = type;
		this.accession = accession!=null?accession:"";
		this.gi = gi!=null?gi:"";
		this.description = description!=null?description:"";
		this.header = header;
	}
	
	public String getType() {
		return type;
	}

	public String getGi() {
		return gi;
	}

	public String getDescription() {
		return description;
	}

	public String getAccession() {
		return accession;
	}
	
	public String getHeader() {
		return header;
	}
}
