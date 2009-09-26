package bio.pih.index;

import java.io.IOException;

import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.encoder.SequenceEncoder;
import bio.pih.io.SequenceDataBank;

/**
 * An interface for index witch stores encoded {@link SequenceEncoder} sequences. 
 *  
 * @author albrecht
 */
public abstract class AbstractSubSequencesInvertedIndex {

	protected final SequenceDataBank databank;
	protected final int subSequenceLength;
	protected final int indexSize;
	protected final long[] EMPTY_ARRAY = new long[0];
	
	protected final DNASequenceEncoderToInteger encoder;
	protected volatile boolean loaded;
	
	/**
	 * @param databank
	 * @param subSequenceLength
	 * @throws ValueOutOfBoundsException
	 */
	public AbstractSubSequencesInvertedIndex(SequenceDataBank databank, int subSequenceLength) throws ValueOutOfBoundsException {
		assert (subSequenceLength > 0);

		this.databank = databank;
		this.subSequenceLength = subSequenceLength;
		this.encoder = databank.getEncoder();

		int indexBitsSize = subSequenceLength * SequenceEncoder.bitsByAlphabetSize(databank.getAlphabet().size());
		this.indexSize = 1 << indexBitsSize;
		
		this.loaded = false;
	}
	
	/**
	 * Begin the construction of a index.
	 * @throws ValueOutOfBoundsException 
	 */
	abstract public void constructIndex() throws ValueOutOfBoundsException;
	
	/**
	 * Finalize the index construction, doing optimizations and/or store process.  
	 * Should be called <b>after</b> all sequences was added into index. 
	 * @throws IOException 
	 */
	abstract public void finishConstruction() throws IOException;
	
	/**
	 * Add an encoded sequences into the index
	 * 
	 * @param sequenceId
	 * @param encodedSequence
	 * @param subSequenceOffSet 
	 */
	abstract public void addSequence(int sequenceId, int[] encodedSequence, int subSequenceOffSet);
	
	/**
	 * @param subSequence
	 * @return a list containing the {@link EncoderSubSequenceIndexInfo} encoded, use {@link EncoderSubSequenceIndexInfo} to decode it.
	 * @throws ValueOutOfBoundsException
	 * @throws IOException 
	 * @throws InvalidHeaderData 
	 */
	abstract public long[] getMatchingSubSequence(SymbolList subSequence) throws ValueOutOfBoundsException, IOException, InvalidHeaderData;

	/**
	 * @param encodedSubSequence 
	 * @return a list containing the {@link EncoderSubSequenceIndexInfo} encoded, use {@link EncoderSubSequenceIndexInfo} to decode it.
	 * @throws IOException 
	 * @throws InvalidHeaderData 
	 */
	abstract public long[] getMatchingSubSequence(int encodedSubSequence) throws IOException, InvalidHeaderData;

	/**
	 * @return a string containing the status of the index.
	 */
	abstract public String indexStatus();
	
	/**
	 * Write the respective inverted index into a file.
	 * @throws IOException 
	 */
	abstract public void saveToFile() throws IOException;

	/**
	 * Load the respective inverted index from a file.
	 * @throws IOException 
	 */
	abstract public void loadFromFile() throws IOException;

	/**
	 * Check the saved index file consistency.
	 * @throws IOException
	 */
	abstract public void checkFile() throws IOException;
	
	/**
	 * Check if the index file data exists.
	 * @return <code>true</code> if the data exists and do not need be reloaded.
	 */
	abstract public boolean fileExists();
	
	/**
	 * Inform if the index is loaded. 
	 * @return <code>true</code> if the index is loaded.
	 */
	public boolean isLoaded() {
		return loaded;
	}
	
	public String getName() {
		return databank.getName() + "_SubSequencesInvertedIndex";
	}
	
}
