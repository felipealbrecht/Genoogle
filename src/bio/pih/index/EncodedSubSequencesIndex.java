package bio.pih.index;

import java.util.List;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.SequenceEncoder;

/**
 * An interface for index that stores sequences encoded {@link SequenceEncoder} 
 * @author albrecht
 *
 */
public interface EncodedSubSequencesIndex {

	/**
	 * Add a sequence into index
	 * 
	 * @param sequence
	 */
	public abstract void addSequence(Sequence sequence);

	/**
	 * @param subSequenceString
	 * @return
	 * @throws IllegalSymbolException
	 * @throws BioException
	 * @throws ValueOutOfBoundsException
	 */
	public abstract List<SubSequenceInfo> getMatchingSubSequence(String subSequenceString) throws IllegalSymbolException, BioException, ValueOutOfBoundsException;

	/**
	 * @param subSequence
	 * @return
	 * @throws ValueOutOfBoundsException
	 */
	public abstract List<SubSequenceInfo> getMachingSubSequence(SymbolList subSequence) throws ValueOutOfBoundsException;

	/**
	 * @param encodedSubSequence 
	 * @return
	 */
	public abstract List<SubSequenceInfo> getMachingSubSequence(short encodedSubSequence);

	/**
	 * @return a string containing the status of the index.
	 */
	public abstract String indexStatus();

}