package bio.pih.io;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.DNAMaskEncoder;
import bio.pih.encoder.SequenceEncoder;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.MemoryInvertedIndex;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.index.builder.InvertedIndexBuilder;
import bio.pih.io.proto.Io.StoredSequence;

/**
 * A data bank witch index its sequences and uses similar subsequences index.
 * 
 * @author albrecht
 * 
 */
public class IndexedDNASequenceDataBank extends DNASequenceDataBank implements IndexedSequenceDataBank {

	private final MemoryInvertedIndex index;
	private InvertedIndexBuilder indexBuilder;
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

		index = new MemoryInvertedIndex(this, subSequenceLength);
	}

	@Override
	public synchronized void load() throws IOException, ValueOutOfBoundsException, IllegalSymbolException, BioException {
		super.load();
		index.loadFromFile();
	}

	public void encodeSequences() throws IOException, NoSuchElementException, BioException, ValueOutOfBoundsException {
		beginIndexBuild();
		super.encodeSequences();
		endIndexBuild();
	}

	public void beginIndexBuild() {
		indexBuilder = new InvertedIndexBuilder(index);
		indexBuilder.constructIndex();
	}

	public void endIndexBuild() {
		indexBuilder.finishConstruction();
		indexBuilder = null;
	}

	@Override
	public int doSequenceProcessing(int sequenceId, StoredSequence storedSequence) throws IllegalSymbolException,
			BioException {
		int[] encodedSequence = Utils.getEncodedSequenceAsArray(storedSequence);
		int size = SequenceEncoder.getSequenceLength(encodedSequence);
		if (maskEncoder == null) {
			indexBuilder.addSequence(sequenceId, encodedSequence, subSequenceLength);
		} else {
			SymbolList sequence = encoder.decodeIntegerArrayToSymbolList(encodedSequence);
			int[] filteredSequence = maskEncoder.applySequenceMask(sequence);
			indexBuilder.addSequence(sequenceId, filteredSequence, maskEncoder.getPatternLength());
		}

		return size;
	}

	public DNAMaskEncoder getMaskEncoder() {
		return maskEncoder;
	}

	public long[] getMatchingSubSequence(int encodedSubSequence) throws IOException, InvalidHeaderData {
		return index.getMatchingSubSequence(encodedSubSequence);
	}

	@Override
	public StorageKind getStorageKind() {
		return storageKind;
	}
	
	@Override
	public boolean check() {
		if (!index.check()) {
			return false;
		}
		return super.check();
	}
	
	@Override
	public void delete() {
		super.delete();
		index.delete();
	}
	
}
