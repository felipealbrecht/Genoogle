package bio.pih.genoogle.index;

import java.io.IOException;

import org.biojava.bio.symbol.SymbolList;

import bio.pih.genoogle.encoder.DNASequenceEncoderToInteger;
import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.io.AbstractSequenceDataBank;

/**
 * An interface for index witch stores encoded {@link SequenceEncoder} sequences. 
 *  
 * @author albrecht
 */
public abstract class AbstractInvertedIndex {

	protected final AbstractSequenceDataBank databank;
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
	public AbstractInvertedIndex(AbstractSequenceDataBank databank, int subSequenceLength) throws ValueOutOfBoundsException {
		assert (subSequenceLength > 0);

		this.databank = databank;
		this.subSequenceLength = subSequenceLength;
		this.encoder = databank.getEncoder();

		int indexBitsSize = subSequenceLength * SequenceEncoder.bitsByAlphabetSize(databank.getAlphabet().size());
		this.indexSize = 1 << indexBitsSize;
		
		this.loaded = false;
	}
			
	public AbstractSequenceDataBank getDatabank() {
		return databank;
	}
	
	public int getIndexSize() {
		return indexSize;
	}
	
	
	/**
	 * @param subSequence
	 * @return a list containing the {@link SubSequenceIndexInfo} encoded, use {@link SubSequenceIndexInfo} to decode it.
	 * @throws ValueOutOfBoundsException
	 * @throws IOException 
	 * @throws InvalidHeaderData 
	 */
	abstract public long[] getMatchingSubSequence(SymbolList subSequence) throws ValueOutOfBoundsException, IOException, InvalidHeaderData;

	/**
	 * @param encodedSubSequence 
	 * @return a list containing the {@link SubSequenceIndexInfo} encoded, use {@link SubSequenceIndexInfo} to decode it.
	 * @throws IOException 
	 * @throws InvalidHeaderData 
	 */
	abstract public long[] getMatchingSubSequence(int encodedSubSequence) throws IOException, InvalidHeaderData;

	/**
	 * @return a string containing the status of the index.
	 */
	abstract public String indexStatus();
	
	/**
	 * Load the respective inverted index from a file.
	 * @throws IOException 
	 */
	abstract public void loadFromFile() throws IOException;
	
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
