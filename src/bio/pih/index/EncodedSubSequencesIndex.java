package bio.pih.index;

import java.util.List;

import org.biojava.bio.BioException;
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
	 * Add a {@link SymbolList} into the index
	 *
	 * @param sequenceId 
	 * @param sequence
	 */
	public abstract void addSequence(int sequenceId, SymbolList sequence);
	
	/**
	 * Add an encoded sequences into the index
	 * 
	 * @param sequenceId
	 * @param encodedSequence 
	 */
	public abstract void addSequence(int sequenceId, short[] encodedSequence);
	
	/**
	 * Optimize the internal data structure to lower memory requirements.
	 * Should be called <b>after</b> that all sequences was inserted into index. 
	 */
	public void optime();
	
	/**
	 * @param subSequenceString
	 * @return a list containing the {@link SubSequenceIndexInfo} encoded, use {@link SubSequenceIndexInfo} to decode it. 
	 * @throws IllegalSymbolException
	 * @throws BioException
	 * @throws ValueOutOfBoundsException
	 */
	public abstract int[] getMatchingSubSequence(String subSequenceString) throws IllegalSymbolException, BioException, ValueOutOfBoundsException;

	/**
	 * @param subSequence
	 * @return a list containing the {@link SubSequenceIndexInfo} encoded, use {@link SubSequenceIndexInfo} to decode it.
	 * @throws ValueOutOfBoundsException
	 */
	public abstract int[] getMachingSubSequence(SymbolList subSequence) throws ValueOutOfBoundsException;

	/**
	 * @param encodedSubSequence 
	 * @return a list containing the {@link SubSequenceIndexInfo} encoded, use {@link SubSequenceIndexInfo} to decode it.
	 */
	public abstract int[] getMachingSubSequence(short encodedSubSequence);

	/**
	 * @return a string containing the status of the index.
	 */
	public abstract String indexStatus();

}