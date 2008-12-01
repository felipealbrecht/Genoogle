package bio.pih.io;

import java.io.IOException;
import java.util.List;

import bio.pih.index.AbstractSubSequencesInvertedIndex;
import bio.pih.index.EncoderSubSequenceIndexInfo;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SimilarSubSequencesIndex;
import bio.pih.index.ValueOutOfBoundsException;


/**
 * This interface works like a Façade for the DataBank access the {@link AbstractSubSequencesInvertedIndex} with the {@link SimilarSubSequencesIndex}
 * 
 * @author albrecht
 */
public interface IndexedSequenceDataBank extends SequenceDataBank {

	/**
	 * Defines where the index will be stored.
	 */
	public static enum StorageKind {
		/**
		 * The index will be stored in the RAM.
		 */
		MEMORY,
		
		/**
		 * The index will be stored into a disk file.
		 */
		DISK
	}
	
	/**
	 * @return where the index is stored.
	 */
	public StorageKind getStorageKind();
	
	/**
	 * Receive an encodedSubSequence, that is a sub-sequence 8 bases length encoded into a short, 
	 * and return an Array of integer containing the sequence and position that is <b>exactly equals</b> the subsequence.
	 * @param encodedSubSequence 
	 * @return a list containing the {@link EncoderSubSequenceIndexInfo} encoded, use {@link EncoderSubSequenceIndexInfo} to decode it. 
	 * @throws ValueOutOfBoundsException
	 * @throws IOException 
	 * @throws InvalidHeaderData 
	 */
	public long[] getMatchingSubSequence(int encodedSubSequence) throws ValueOutOfBoundsException, IOException, InvalidHeaderData;
	
	

	/**
	 * Receive an encodedSubSequence,  
	 * and return a {@link List} with the subSequence of integer containing the sequence and position that is <b>similar</b> the subsequence.
	 * 
	 * @param encodedSubSequence  
	 * @return an {@link List} of interger containing the similar sub  sequences.
	 * @throws ValueOutOfBoundsException
	 * @throws InvalidHeaderData 
	 * @throws IOException 
	 */
	public List<Integer> getSimilarSubSequence(int encodedSubSequence) throws ValueOutOfBoundsException, IOException, InvalidHeaderData;
	
	
	/**
	 * Write the respective inverted index into a file.
	 * @throws IOException 
	 */
	public void write() throws IOException;
}
