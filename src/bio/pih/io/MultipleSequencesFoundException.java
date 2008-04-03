package bio.pih.io;

/**
 * @author albrecht
 *
 */
public class MultipleSequencesFoundException extends Exception {

	private static final long serialVersionUID = 4455366283906614223L;

	int sequenceId;
	String databank1, databank2;
	MultipleSequencesFoundException(int sequenceId, String databank1, String databank2) {
		this.sequenceId = sequenceId;
		this.databank1 = databank1;
		this.databank2 = databank2;
	}
	
	/**
	 * @return
	 */
	public int getSequenceId() {
		return sequenceId;
	}
	
	/**
	 * @return
	 */
	public String getDatabank1() {
		return databank1;
	}
	
	/**
	 * @return
	 */
	public String getDatabank2() {
		return databank2;
	}		
}
