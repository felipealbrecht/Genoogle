package bio.pih.io;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import bio.pih.index.AbstractSubSequencesInvertedIndex;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.MemorySubSequencesInvertedIndex;
import bio.pih.index.PersistentSubSequencesInvertedIndex;
import bio.pih.index.SimilarSubSequencesIndex;
import bio.pih.index.ValueOutOfBoundsException;

/**
 * A data bank witch index its sequences and uses similar subsequences index.
 * 
 * @author albrecht
 * 
 */
public class IndexedDNASequenceDataBank extends DNASequenceDataBank implements IndexedSequenceDataBank {

	private static Logger logger = Logger.getLogger("pih.bio.io.IndexedDNASequenceDataBank");

	private final AbstractSubSequencesInvertedIndex index;
	private static final SimilarSubSequencesIndex similarSubSequencesIndex;

	private final StorageKind storageKind;

	static {
		similarSubSequencesIndex = SimilarSubSequencesIndex.getDefaultInstance();
		try {
			similarSubSequencesIndex.load();
		} catch (Exception e) {
			logger.fatal("Fatar error while loading default SimilarSubSequencesIndex.\n Pay attention if the files " + similarSubSequencesIndex.getDataFileName() + " and " + similarSubSequencesIndex.getIndexFileName() + " exists and are not corrupted.", e);
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
		this(name, path, null, storageKind, false);
	}
	
	/**
	 * Same as public IndexedDNASequenceDataBank(String name, File path, boolean isReadOnly) setting isReadOnly as false.
	 * 
	 * @param name
	 * @param path
	 * @param parent 
	 * @param storageKind 
	 * @throws ValueOutOfBoundsException
	 */
	public IndexedDNASequenceDataBank(String name, File path, DatabankCollection<? extends DNASequenceDataBank> parent, StorageKind storageKind) throws ValueOutOfBoundsException {
		this(name, path, parent, storageKind, false);
	}

	/**
	 * 
	 * @param name
	 *            the name of the data bank
	 * @param path
	 *            the path where the data bank is/will be stored
	 * @param parent 
	 * @param isReadOnly
	 * @param storageKind  
	 * @throws ValueOutOfBoundsException
	 */
	public IndexedDNASequenceDataBank(String name, File path, DatabankCollection<? extends DNASequenceDataBank> parent, StorageKind storageKind, boolean isReadOnly) throws ValueOutOfBoundsException {
		super(name, path, parent, isReadOnly);
		this.storageKind = storageKind;
		
		// TODO: Put it into a factory.
		if (storageKind == IndexedSequenceDataBank.StorageKind.MEMORY) {
			index = new MemorySubSequencesInvertedIndex(this, 8);
			
		} else { //if (storageKind == IndexedSequenceDataBank.StorageKind.DISK){
			index = new PersistentSubSequencesInvertedIndex(this, 8);
		} 
	}

	@Override
	void doSequenceAddingProcessing(SequenceInformation sequenceInformation) {
		index.addSequence(sequenceInformation.getId(), sequenceInformation.getEncodedSequence());
	}
	
	@Override
	void loadInformations() throws IOException {
		if (index.exists()) {
			index.load();
		}
	}
	
	@Override
	void beginSequencesProcessing() throws IOException, ValueOutOfBoundsException {
		if (!index.exists()) { 
			index.constructIndex();
			if (index.exists()) {
				index.load();
			}
		}
	}

	@Override
	void doSequenceProcessing(SequenceInformation sequenceInformation) {
		if (!index.exists()) {
			index.addSequence(sequenceInformation.getId(), sequenceInformation.getEncodedSequence());
		}
	}
	
	@Override
	void finishSequencesProcessing() throws IOException {
		if (!index.exists()) {
			index.finishConstruction();
		}
	}

	public int[] getMachingSubSequence(short encodedSubSequence) throws ValueOutOfBoundsException, IOException {
		return index.getMachingSubSequence(encodedSubSequence);
	}

	public int[] getSimilarSubSequence(short encodedSubSequence) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {
		return similarSubSequencesIndex.getSimilarSequences(encodedSubSequence);
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
