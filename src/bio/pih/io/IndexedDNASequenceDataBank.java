package bio.pih.io;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.log4j.Logger;

import bio.pih.index.AbstractSubSequencesInvertedIndex;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.MemorySubSequencesInvertedIndexInteger;
import bio.pih.index.PersistentSubSequencesInvertedIndex;
import bio.pih.index.SimilarSubSequencesIndex;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.proto.Io.StoredSequence;

import com.google.protobuf.ByteString;

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
			index = new MemorySubSequencesInvertedIndexInteger(this, 10);
			
		} else { //if (storageKind == IndexedSequenceDataBank.StorageKind.DISK){
			System.err.println("Storage Kind DISK is Deprecated");
			index = new PersistentSubSequencesInvertedIndex(this, 10);
		} 
	}

	private int[] getIntBuffer(StoredSequence storedSequence) {
		ByteString encodedSequence = storedSequence.getEncodedSequence();
		byte[] byteArray = encodedSequence.toByteArray();
		final int[] ret = new int[byteArray.length/4];
		ByteBuffer.wrap(byteArray).asIntBuffer().get(ret);
		return ret;
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
	void doSequenceProcessing(StoredSequence storedSequence) {
		if (!index.exists()) {
			final int[] ret = getIntBuffer(storedSequence);
			index.addSequence(storedSequence.getId(), ret);
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
