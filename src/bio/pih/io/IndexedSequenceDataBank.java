package bio.pih.io;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import bio.pih.index.EncodedSubSequencesIndex;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SubSequenceIndexInfo;
import bio.pih.index.SubSequencesComparer;
import bio.pih.index.ValueOutOfBoundsException;


/**
 * This interface aims to work like a Façade for the DataBank access the {@link EncodedSubSequencesIndex} with the {@link SubSequencesComparer}
 * 
 * @author albrecht
 */
public interface IndexedSequenceDataBank extends SequenceDataBank {

	
	/**
	 * Receive an encodedSubSequence, that is a sub-sequence 8 bases length encoded into a short, 
	 * and return a List of integer containing the sequence and position that is <b>exactly equals</b> the subsequence.
	 * @param encodedSubSequence 
	 * @return a list containing the {@link SubSequenceIndexInfo} encoded, use {@link SubSequenceIndexInfo} to decode it. 
	 * @throws ValueOutOfBoundsException
	 */
	public List<Integer> getMachingSubSequence(short encodedSubSequence) throws ValueOutOfBoundsException;
	
	

	/**
	 * Receive an encodedSubSequence, that is a sub-sequence 8 bases length encoded into a short, 
	 * and return a Map with the subSequence and a list of integer containing the sequence and position that is <b>similar</b> the subsequence.
	 * 
	 * <p>For more informations about the threshold and score, take a look into {@link SubSequencesComparer} and it <code>getDefaultInstance</code> method.
	 * @param encodedSubSequence 
	 * @param threshold  the minimum or equal score of the similar sub sequences resulted 
	 * @return a list containing the {@link SubSequenceIndexInfo} encoded, use {@link SubSequenceIndexInfo} to decode it. 
	 * @throws ValueOutOfBoundsException
	 * @throws InvalidHeaderData 
	 * @throws IOException 
	 */
	public Map<Short, List<Integer>> getSimilarSubSequence(short encodedSubSequence, int threshold) throws ValueOutOfBoundsException, IOException, InvalidHeaderData;
}
