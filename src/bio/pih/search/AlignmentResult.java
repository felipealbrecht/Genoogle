package bio.pih.search;

import java.io.Serializable;

import org.biojava.bio.seq.Sequence;

/**
 * @author albrecht
 *
 */
public class AlignmentResult implements Serializable {

	private static final long serialVersionUID = -7701610542981141900L;
			
	Sequence sequence;
	int pontuation;	
	
	/**
	 * @param sequence
	 * @param pontuation
	 */
	public AlignmentResult(Sequence sequence, int pontuation) {
		this.sequence = sequence;
		this.pontuation = pontuation;
	}
	
	/**
	 * @return
	 */
	public int getPontuation() {
		return pontuation;
	}
	
	/**
	 * @param pontuation
	 */
	public void setPontuation(int pontuation) {
		this.pontuation = pontuation;
	}
	
	/**
	 * @return
	 */
	public Sequence getSequence() {
		return sequence;
	}
	
	/**
	 * @param sequence
	 */
	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}
	
	@Override
	public String toString() {
		return this.getPontuation() + "\t" + this.getSequence();
	}
}
