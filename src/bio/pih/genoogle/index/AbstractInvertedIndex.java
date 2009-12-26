/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.index;

import java.io.IOException;

import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.seq.SymbolList;

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
	
	protected final SequenceEncoder encoder;
	protected volatile boolean loaded;
	
	/**
	 * @param databank
	 * @param subSequenceLength
	 */
	public AbstractInvertedIndex(AbstractSequenceDataBank databank, int subSequenceLength) {
		this.databank = databank;
		this.subSequenceLength = subSequenceLength;
		this.encoder = databank.getEncoder();

		int indexBitsSize = subSequenceLength * SequenceEncoder.bitsByAlphabetSize(databank.getAlphabet().getSize());
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
	 */
	abstract public long[] getMatchingSubSequence(SymbolList subSequence) throws ValueOutOfBoundsException, IOException;

	/**
	 * @param encodedSubSequence 
	 * @return a list containing the {@link SubSequenceIndexInfo} encoded, use {@link SubSequenceIndexInfo} to decode it.
	 */
	abstract public long[] getMatchingSubSequence(int encodedSubSequence) throws IOException;

	/**
	 * @return a string containing the status of the index.
	 */
	abstract public String indexStatus();
	
	/**
	 * Load the respective inverted index from a file.
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
