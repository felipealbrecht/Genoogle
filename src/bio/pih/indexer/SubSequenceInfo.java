package bio.pih.indexer;

import java.io.Serializable;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.SymbolList;

/**
 * @author albrecht
 *
 */
public class SubSequenceInfo implements Serializable {
	
	private static final long serialVersionUID = -4573843446834948261L;
	
	private Sequence sequenceRef;
	private int start ;
	private int lenght;
	private SymbolList subSequence;
	
	/**
	 * @param sequenceRef
	 * @param subSequence
	 * @param start
	 * @param length
	 */
	public SubSequenceInfo(Sequence sequenceRef, SymbolList subSequence, int start, int length) {
		this.sequenceRef = sequenceRef;
		this.subSequence = subSequence;
		this.start = start;
		this.lenght = start;
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
	 * @param subSequence
	 */
	public void setSubSequence(SymbolList subSequence) {
		this.subSequence = subSequence;
	}
	
	/**
	 * @return
	 */
	public SymbolList getSubSequence() {
		return subSequence;
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
	
	/**
	 * @param lenght
	 */
	public void setLenght(int lenght) {
		 this.lenght = lenght;
	}
	
	/**
	 * @return
	 */
	public int getLenght() {
		return lenght;
	}
	
	@Override
	public String toString() {
		return this.sequenceRef.toString() + " ("+start+","+(start+lenght)+")";
	}
}
