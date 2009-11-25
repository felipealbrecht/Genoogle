package bio.pih.genoogle.io;

import java.io.IOException;

import bio.pih.genoogle.index.AbstractInvertedIndex;
import bio.pih.genoogle.index.InvalidHeaderData;
import bio.pih.genoogle.index.SubSequenceIndexInfo;
import bio.pih.genoogle.index.ValueOutOfBoundsException;


/**
 * This interface defines how is the access to the DataBank {@link AbstractInvertedIndex}
 * 
 * @author albrecht
 */
public interface IndexedSequenceDataBank {
	
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