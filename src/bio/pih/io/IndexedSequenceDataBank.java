package bio.pih.io;

import java.io.IOException;

import bio.pih.index.EncoderSubSequenceIndexInfo;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SimilarSubSequencesIndex;
import bio.pih.index.ValueOutOfBoundsException;


/**
 * This interface works like a Façade for the DataBank access the {@link SubSequencesInvertedIndex} with the {@link SimilarSubSequencesIndex}
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
	 * and return a List of integer containing the sequence and position that is <b>exactly equals</b> the subsequence.
	 * @param encodedSubSequence 
	 * @return a list containing the {@link EncoderSubSequenceIndexInfo} encoded, use {@link EncoderSubSequenceIndexInfo} to decode it. 
	 * @throws ValueOutOfBoundsException
	 * @throws IOException 
	 */
	public int[] getMachingSubSequence(short encodedSubSequence) throws ValueOutOfBoundsException, IOException;
	
	

	/**
	 * Receive an encodedSubSequence, that is a sub-sequence 8 bases length encoded into a short, 
	 * and return a Map with the subSequence and a list of integer containing the sequence and position that is <b>similar</b> the subsequence.
	 * 
	 * <p>For more informations about the threshold and score, take a look into {@link SimilarSubSequencesIndex} and it <code>getDefaultInstance</code> method.
	 * @param encodedSubSequence 
	 * @param threshold  the minimum or equal score of the similar sub sequences resulted 
	 * @return an array containing the {@link EncoderSubSequenceIndexInfo} encoded, use {@link EncoderSubSequenceIndexInfo} to decode it. 
	 * @throws ValueOutOfBoundsException
	 * @throws InvalidHeaderData 
	 * @throws IOException 
	 */
	public int[] getSimilarSubSequence(short encodedSubSequence) throws ValueOutOfBoundsException, IOException, InvalidHeaderData;
	
	
	/**
	 * Write the respective inverted index into a file.
	 * @throws IOException 
	 */
	public void write() throws IOException;
}
