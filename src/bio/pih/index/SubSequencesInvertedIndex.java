package bio.pih.index;

import java.io.IOException;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.SequenceEncoder;

/**
 * An interface for index that stores sequences encoded {@link SequenceEncoder} 
 * @author albrecht
 *
 */
public interface SubSequencesInvertedIndex {

	
	/**
	 * Add a {@link SymbolList} into the index
	 *
	 * @param sequenceId 
	 * @param sequence
	 */
	public void addSequence(int sequenceId, SymbolList sequence);
	
	/**
	 * Add an encoded sequences into the index
	 * 
	 * @param sequenceId
	 * @param encodedSequence 
	 */
	public void addSequence(int sequenceId, short[] encodedSequence);
	
	/**
	 * Optimize the internal data structure to lower memory requirements.
	 * Should be called <b>after</b> that all sequences was inserted into index. 
	 */
	public void optimize();
	
	/**
	 * @param subSequenceString
	 * @return a list containing the {@link EncoderSubSequenceIndexInfo} encoded, use {@link EncoderSubSequenceIndexInfo} to decode it. 
	 * @throws IllegalSymbolException
	 * @throws BioException
	 * @throws ValueOutOfBoundsException
	 */
	public int[] getMatchingSubSequence(String subSequenceString) throws IllegalSymbolException, BioException, ValueOutOfBoundsException;

	/**
	 * @param subSequence
	 * @return a list containing the {@link EncoderSubSequenceIndexInfo} encoded, use {@link EncoderSubSequenceIndexInfo} to decode it.
	 * @throws ValueOutOfBoundsException
	 */
	public int[] getMachingSubSequence(SymbolList subSequence) throws ValueOutOfBoundsException;

	/**
	 * @param encodedSubSequence 
	 * @return a list containing the {@link EncoderSubSequenceIndexInfo} encoded, use {@link EncoderSubSequenceIndexInfo} to decode it.
	 */
	public int[] getMachingSubSequence(short encodedSubSequence);

	/**
	 * @return a string containing the status of the index.
	 */
	public String indexStatus();
	
	/**
	 * Write the respective inverted index into a file.
	 * @throws IOException 
	 */
	public void write() throws IOException;
}