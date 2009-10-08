package bio.pih.io;

import java.io.IOException;

import bio.pih.index.AbstractInvertedIndex;
import bio.pih.index.SubSequenceIndexInfo;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;


/**
 * This interface defines how is the access to the DataBank {@link AbstractInvertedIndex}
 * 
 * @author albrecht
 */
public interface IndexedSequenceDataBank extends SequenceDataBank {

	/**
	 * Defines where the index will be stored at runtime.
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
	 * @return a list containing the {@link SubSequenceIndexInfo} encoded, use {@link SubSequenceIndexInfo} to decode it. 
	 * @throws ValueOutOfBoundsException
	 * @throws IOException 
	 * @throws InvalidHeaderData 
	 */
	public long[] getMatchingSubSequence(int encodedSubSequence) throws ValueOutOfBoundsException, IOException, InvalidHeaderData;		
}
