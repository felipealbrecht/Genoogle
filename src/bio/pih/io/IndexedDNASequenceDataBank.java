package bio.pih.io;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.biojava.bio.seq.DNATools;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.MemorySubSequencesInvertedIndex;
import bio.pih.index.PersistentSubSequencesInvertedIndex;
import bio.pih.index.SimilarSubSequencesIndex;
import bio.pih.index.ValueOutOfBoundsException;

/**
 * A data bank witch index its sequences.
 * 
 * @author albrecht
 * 
 */
public class IndexedDNASequenceDataBank extends DNASequenceDataBank implements IndexedSequenceDataBank {

	private static Logger logger = Logger.getLogger("pih.bio.io.IndexedDNASequenceDataBank");

	private final MemorySubSequencesInvertedIndex index;
	private static final SimilarSubSequencesIndex subSequenceComparer;

	private final StorageKind storageKind;

	static {
		subSequenceComparer = SimilarSubSequencesIndex.getDefaultInstance();
		try {
			subSequenceComparer.load();
		} catch (Exception e) {
			logger.fatal("Fatar error while loading default SubSequenceComparer.\n Pay attention if the files " + subSequenceComparer.getDataFileName() + " and " + subSequenceComparer.getIndexFileName() + " exists and are not corrupted.", e);
		}
	}

	/**
	 * Same as public IndexedDNASequenceDataBank(String name, File path, boolean isReadOnly) setting isReadOnly as false.
	 * 
	 * @param name
	 * @param path
	 * @param storageKind 
	 * @throws ValueOutOfBoundsException
	 */
	public IndexedDNASequenceDataBank(String name, File path, StorageKind storageKind) throws ValueOutOfBoundsException {
		this(name, path, storageKind, false);
	}

	/**
	 * 
	 * @param name
	 *            the name of the data bank
	 * @param path
	 *            the path where the data bank is/will be stored
	 * @param isReadOnly
	 * @param storageKind  
	 * @throws ValueOutOfBoundsException
	 */
	public IndexedDNASequenceDataBank(String name, File path, StorageKind storageKind, boolean isReadOnly) throws ValueOutOfBoundsException {
		super(name, path, isReadOnly);
		this.storageKind = storageKind;
		
		// TODO: Put it into a factory.
		if (storageKind == IndexedSequenceDataBank.StorageKind.MEMORY) {
			index = new MemorySubSequencesInvertedIndex(8, DNATools.getDNA());
			
		} else { //if (storageKind == IndexedSequenceDataBank.StorageKind.DISK){
			index = new PersistentSubSequencesInvertedIndex(8, DNATools.getDNA());
		} 
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

	@Override
	public StorageKind getStorageKind() {
		return storageKind;
	}
	
	@Override
	public void write() throws IOException {
		index.write();
	}

}
