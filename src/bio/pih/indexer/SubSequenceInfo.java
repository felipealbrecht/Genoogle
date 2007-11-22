package bio.pih.indexer;

import java.io.Serializable;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.SymbolList;

public class SubSequenceInfo implements Serializable {
	
	private static final long serialVersionUID = -4573843446834948261L;
	
	private Sequence sequenceRef;
	private int start;
	private int lenght;
	private SymbolList subSequence;
	
	public SubSequenceInfo(Sequence sequenceRef, SymbolList subSequence, int start, int length) {
		this.sequenceRef = sequenceRef;
		this.subSequence = subSequence;
		this.start = start;
		this.lenght = start;
	}
	
	public void setSequence(Sequence sequencerRef) {
		this.sequenceRef = sequencerRef;
	}
	
	public Sequence getSequence() {
		return sequenceRef;
	}
	
	public void setSubSequence(SymbolList subSequence) {
		this.subSequence = subSequence;
	}
	
	public SymbolList getSubSequence() {
		return subSequence;
	}
	
	public void setStart(int start) {
		this.start = start;
	}
	
	public int getStart() {
		return start;
	}
	
	public void setLenght(int lenght) {
		this.lenght = lenght;
	}
	
	public int getLenght() {
		return lenght;
	}
	
	public String toString() {
		return this.sequenceRef.toString() + " ("+start+","+(start+lenght)+")";
	}
}
