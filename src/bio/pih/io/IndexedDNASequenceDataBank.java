package bio.pih.io;

import java.io.File;
import java.io.IOException;

import org.biojava.bio.seq.DNATools;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SubSequencesArrayIndex;
import bio.pih.index.SubSequencesComparer;
import bio.pih.index.ValueOutOfBoundsException;

/**
 * A data bank witch index its sequences.
 * 
 * @author albrecht
 * 
 */
public class IndexedDNASequenceDataBank extends DNASequenceDataBank implements IndexedSequenceDataBank {

	private final SubSequencesComparer subSequenceComparer;
	private final SubSequencesArrayIndex index;
	
	/**
	 * Same as public IndexedDNASequenceDataBank(String name, SequenceDataBank parent, File path, boolean isReadOnly) without passing parent parameter.
	 * @param name
	 * @param path
	 * @param isReadOnly
	 * @throws ValueOutOfBoundsException
	 * @throws IOException
	 * @throws InvalidHeaderData
	 */
	public IndexedDNASequenceDataBank(String name, File path, boolean isReadOnly) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {
		super(name, null, path, isReadOnly);
		
		index = new SubSequencesArrayIndex(8, DNATools.getDNA());
		
		subSequenceComparer = SubSequencesComparer.getDefaultInstance();
		subSequenceComparer.load();
	}

	/**
	 * 
	 * @param name
	 *            the name of the data bank
	 * @param parent 
	 * @param path
	 *            the path where the data bank is/will be stored
	 * @param isReadOnly
	 * @throws IOException
	 * @throws ValueOutOfBoundsException
	 * @throws InvalidHeaderData 
	 */
	public IndexedDNASequenceDataBank(String name, SequenceDataBank parent, File path, boolean isReadOnly) throws IOException, ValueOutOfBoundsException, InvalidHeaderData {
		super(name, parent, path, isReadOnly);
		index = new SubSequencesArrayIndex(8, DNATools.getDNA());
		
		subSequenceComparer = SubSequencesComparer.getDefaultInstance();
		subSequenceComparer.load();		
	}
	
	@Override
	void doSequenceAddingProcessing(SequenceInformation sequenceInformation) {
		index.addSequence(sequenceInformation.getId(), sequenceInformation.getEncodedSequence());		
	}
	
	@Override
	void doSequenceLoadingProcessing(SequenceInformation sequenceInformation) {
		index.addSequence(sequenceInformation.getId(), sequenceInformation.getEncodedSequence());
	}			

	public int[] getMachingSubSequence(short encodedSubSequence) throws ValueOutOfBoundsException {
		return index.getMachingSubSequence(encodedSubSequence);
	}
	
	public int[] getSimilarSubSequence(short encodedSubSequence) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {		
		return subSequenceComparer.getSimilarSequences(encodedSubSequence);		
	}
	
}
