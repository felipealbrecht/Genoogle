package bio.pih.index;

import java.io.Serializable;

import org.biojava.bio.seq.Sequence;

/**
 * @author albrecht
 *
 */
public class SubSequenceInfo implements Serializable {
	
	private static final long serialVersionUID = -4573843446834948261L;
	
	private Sequence sequenceRef;
	private int start ;
	private short subSequenceEncoded;
	
	/**
	 * @param sequenceRef
	 * @param sequenceEncoded 
	 * @param start
	 */
	public SubSequenceInfo(Sequence sequenceRef, short sequenceEncoded, int start) {
		this.sequenceRef = sequenceRef;
		this.subSequenceEncoded = sequenceEncoded;
		this.start = start;
	}
	
	/**
	 * @param sequencerRef
	 */
	public void setSequence(Sequence sequencerRef) {
		this.sequenceRef = sequencerRef;
	}
	
	/**
	 * @return
	 */
	public Sequence getSequence() {
		return sequenceRef;
	}
	
	/**
	 * @param subSequenceEncoded 
	 */
	public void setSubSequence(short subSequenceEncoded) {
		this.subSequenceEncoded = subSequenceEncoded;
	}
	
	/**
	 * @return
	 */
	public short getSubSequence() {
		return subSequenceEncoded;
	}
	
	/**
	 * @param start
	 */
	public void setStart(int start) {
		this.start = start;
	}
	
	/**
	 * @return
	 */
	public int getStart() {
		return start;
	}
		
	@Override
	public String toString() {
		return this.sequenceRef.toString() + " ("+start+")";
	}
}
