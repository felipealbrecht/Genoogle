package bio.pih.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import bio.pih.index.AbstractSubSequencesInvertedIndex;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.MemorySubSequencesInvertedIndexInteger;
import bio.pih.index.SimilarSubSequencesIndex;
import bio.pih.index.ValueOutOfBoundsException;

/**
 * A data bank witch index its sequences and uses similar subsequences index.
 * 
 * @author albrecht
 * 
 */
public class IndexedDNASequenceDataBank extends DNASequenceDataBank implements IndexedSequenceDataBank {
	
	private final AbstractSubSequencesInvertedIndex index;
	//private final SimilarSubSequencesIndex similarSubSequencesIndex;

	private final StorageKind storageKind;

	/**
	 * Same as public IndexedDNASequenceDataBank(String name, File path, boolean isReadOnly) setting isReadOnly as false.
	 * 
	 * @param name
	 * @param path
	 * @param storageKind 
	 * @param subSequenceLenth 
	 * @param minLengthDropOut 
	 * @throws ValueOutOfBoundsException
	 * @throws InvalidHeaderData 
	 * @throws IOException 
	 */
	public IndexedDNASequenceDataBank(String name, File path, StorageKind storageKind, int subSequenceLenth) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {
		this(name, path, null, storageKind, subSequenceLenth);
	}
	
	/**
	 * 
	 * @param name
	 *            the name of the data bank
	 * @param path
	 *            the path where the data bank is/will be stored
	 * @param parent 
	 * @param storageKind  
	 * @param subSequenceLength 
	 * @param minEvalueDropOut 
	 * @throws ValueOutOfBoundsException
	 * @throws InvalidHeaderData 
	 * @throws IOException 
	 */
	public IndexedDNASequenceDataBank(String name, File path, DatabankCollection<? extends DNASequenceDataBank> parent, StorageKind storageKind, int subSequenceLength) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {
		super(name, path, parent, subSequenceLength);
		this.storageKind = storageKind;
		//this.similarSubSequencesIndex = SimilarSubSequencesIndex.getDefaultInstance(subSequenceLength);
		
		// TODO: Put it into a factory.
		if (storageKind == IndexedSequenceDataBank.StorageKind.MEMORY) {
			index = new MemorySubSequencesInvertedIndexInteger(this, subSequenceLength);
			
		} else { //if (storageKind == IndexedSequenceDataBank.StorageKind.DISK){
			throw new RuntimeException("Storage Kind DISK is Deprecated");
			//index = new PersistentSubSequencesInvertedIndex(this, XMLConfigurationReader.getSubSequenceLength());
		} 
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
	void doSequenceProcessing(int sequenceId, int[] encodedSequence) {
		if (!index.exists()) {
			index.addSequence(sequenceId, encodedSequence);
		}
	}
	
	@Override
	void finishSequencesProcessing() throws IOException {
		if (!index.exists()) {
			index.finishConstruction();
		}
	}

	public long[] getMachingSubSequence(int encodedSubSequence) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {
		return index.getMatchingSubSequence(encodedSubSequence);
	}

	public List<Integer> getSimilarSubSequence(int encodedSubSequence) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {
		//return similarSubSequencesIndex.getSimilarSequences(encodedSubSequence);
		return null;
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
