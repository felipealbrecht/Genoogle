package bio.pih.index;

import java.io.IOException;

import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.DNASequenceEncoderToShort;
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
	protected final int[] EMPTY_ARRAY = new int[0];
	
	protected final DNASequenceEncoderToShort encoder;
	protected boolean loaded;
	
	/**
	 * @param databank 
	 * @param subSequenceLength
	 * @param alphabet
	 * @throws ValueOutOfBoundsException
	 */
	public AbstractSubSequencesInvertedIndex(SequenceDataBank databank, int subSequenceLength) throws ValueOutOfBoundsException {
		assert (subSequenceLength > 0);

		this.databank = databank;
		this.subSequenceLength = subSequenceLength;
		this.encoder = (DNASequenceEncoderToShort) databank.getEncoder();

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
	 * Add a {@link SymbolList} into the index
	 *
	 * @param sequenceId 
	 * @param sequence
	 */
	abstract public void addSequence(int sequenceId, SymbolList sequence);
	
	/**
	 * Add an encoded sequences into the index
	 * 
	 * @param sequenceId
	 * @param encodedSequence 
	 */
	abstract public void addSequence(int sequenceId, short[] encodedSequence);
	
	/**
	 * @param subSequence
	 * @return a list containing the {@link EncoderSubSequenceIndexInfo} encoded, use {@link EncoderSubSequenceIndexInfo} to decode it.
	 * @throws ValueOutOfBoundsException
	 * @throws IOException 
	 */
	abstract public int[] getMachingSubSequence(SymbolList subSequence) throws ValueOutOfBoundsException, IOException;

	/**
	 * @param encodedSubSequence 
	 * @return a list containing the {@link EncoderSubSequenceIndexInfo} encoded, use {@link EncoderSubSequenceIndexInfo} to decode it.
	 * @throws IOException 
	 */
	abstract public int[] getMachingSubSequence(short encodedSubSequence) throws IOException;

	/**
	 * @return a string containing the status of the index.
	 */
	abstract public String indexStatus();
	
	/**
	 * Write the respective inverted index into a file.
	 * @throws IOException 
	 */
	abstract public void write() throws IOException;

	/**
	 * Load the respective inverted index from a file.
	 * @throws IOException 
	 */
	abstract public void load() throws IOException;

	/**
	 * Check the index consistency.
	 * @throws IOException
	 */
	abstract public void check() throws IOException;
	
	/**
	 * Check if the index data exists.
	 * @return <code>true</code> if the data exists and do not need be reloaded.
	 */
	abstract public boolean exists();
	
	/**
	 * Inform if the index is loaded. 
	 * @return <code>true</code> if the index is loaded.
	 */
	public boolean isLoaded() {
		return loaded;
	}
	
}
