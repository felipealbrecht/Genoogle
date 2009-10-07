package bio.pih.io;

import java.io.File;
import java.io.IOException;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.DNAMaskEncoder;
import bio.pih.encoder.SequenceEncoder;
import bio.pih.index.AbstractInvertedIndex;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.MemoryInvertedIndex;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.proto.Io.StoredSequence;

/**
 * A data bank witch index its sequences and uses similar subsequences index.
 * 
 * @author albrecht
 * 
 */
public class IndexedDNASequenceDataBank extends DNASequenceDataBank implements IndexedSequenceDataBank {

	private final AbstractInvertedIndex index;
	// private final SimilarSubSequencesIndex similarSubSequencesIndex;
	protected final DNAMaskEncoder maskEncoder;

	private final StorageKind storageKind;

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
	public IndexedDNASequenceDataBank(String name, File path, DatabankCollection<? extends DNASequenceDataBank> parent,
			StorageKind storageKind, int subSequenceLength, String mask) throws ValueOutOfBoundsException, IOException,
			InvalidHeaderData {
		super(name, path, parent, subSequenceLength);
		this.storageKind = storageKind;
		// this.similarSubSequencesIndex =
		// SimilarSubSequencesIndex.getDefaultInstance(subSequenceLength);
		if (mask != null) {
			maskEncoder = new DNAMaskEncoder(mask, subSequenceLength);
		} else {
			maskEncoder = null;
		}
		// TODO: Put it into a factory.
		if (storageKind == IndexedSequenceDataBank.StorageKind.MEMORY) {
			index = new MemoryInvertedIndex(this, subSequenceLength);

		} else { // if (storageKind == IndexedSequenceDataBank.StorageKind.DISK){
			throw new RuntimeException("Storage Kind DISK is Deprecated");
			// index = new PersistentSubSequencesInvertedIndex(this,
			// XMLConfigurationReader.getSubSequenceLength());
		}
	}

	@Override
	boolean loadInformations() throws IOException {
		if (index.fileExists()) {
			index.loadFromFile();
			return true;
		}
		return false;
	}

	public DNAMaskEncoder getMaskEncoder() {
		return maskEncoder;
	}

	@Override
	void beginSequencesProcessing() throws IOException, ValueOutOfBoundsException {
		index.constructIndex();
	}

	@Override
	int doSequenceProcessing(int sequenceId, StoredSequence storedSequence) throws IllegalSymbolException, BioException {
		int[] encodedSequence = Utils.getEncodedSequenceAsArray(storedSequence);
		int size = SequenceEncoder.getSequenceLength(encodedSequence);
		if (maskEncoder == null) {
			index.addSequence(sequenceId, encodedSequence, subSequenceLength);
		} else {
			SymbolList sequence = encoder.decodeIntegerArrayToSymbolList(encodedSequence);
			int[] filteredSequence = maskEncoder.applySequenceMask(sequence);
			index.addSequence(sequenceId, filteredSequence, maskEncoder.getPatternLength());
		}

		return size;
	}

	@Override
	void finishSequencesProcessing() throws IOException {
		index.finishConstruction();
		if (!index.fileExists()) {
			index.saveToFile();
		}
	}

	public long[] getMatchingSubSequence(int encodedSubSequence) throws IOException, InvalidHeaderData {
		return index.getMatchingSubSequence(encodedSubSequence);
	}

	@Override
	public StorageKind getStorageKind() {
		return storageKind;
	}

	@Override
	public void write() throws IOException {
		index.saveToFile();
	}
}
