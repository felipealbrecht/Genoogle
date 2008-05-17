package bio.pih.index;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.SymbolList;

/**
 * @author albrecht
 * 
 */
public class SimpleSubSequencesIndex implements Serializable {
	
	private static final long serialVersionUID = 7353450597201554133L;
	
	Alphabet alphabet;
	int subSequenceLength;
	Hashtable<SymbolList, List<EncoderSubSequenceIndexInfo>> index;
	int total; // just statistical information
	int subSymbolTotal; // just statistical information 
	
	/**
	 * @param alphabet
	 * @param subSequenceLength
	 */
	public SimpleSubSequencesIndex(Alphabet alphabet, int subSequenceLength) {
		this.alphabet = alphabet;
		this.subSequenceLength = subSequenceLength;
		this.total = 0;
		this.subSymbolTotal = 0;
		this.index = new Hashtable<SymbolList, List<EncoderSubSequenceIndexInfo>>();
	}
	
	/**
	 * Add a {@link SymbolList}, that represents a sub sequence, into the index
	 * @param subSymbolList
	 * @param info
	 */
	public void addSubSequence(SymbolList subSymbolList, EncoderSubSequenceIndexInfo info) {
		List<EncoderSubSequenceIndexInfo> infos = index.get(subSymbolList);
		if (infos == null) {
			infos = new LinkedList<EncoderSubSequenceIndexInfo>();
			index.put(subSymbolList, infos);
			this.subSymbolTotal++;
		}
		total++;
		infos.add(info);
	}

	/**
	 * @param subSymbolList
	 * @return all subsequences that match exactly with the subSymbolList 
	 */
	public List<EncoderSubSequenceIndexInfo> retrievePosition(SymbolList subSymbolList) {
		return index.get(subSymbolList);
	}
	
	/**
	 * @return
	 */
	public int getTotal() {
		return total;
	}
	
	/**
	 * @return
	 */
	public int getSubSymbolTotal() {
		return subSymbolTotal;
	}

}
