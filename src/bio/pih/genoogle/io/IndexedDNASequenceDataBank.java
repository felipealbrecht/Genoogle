package bio.pih.genoogle.io;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.genoogle.encoder.DNAMaskEncoder;
import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.index.IndexConstructionException;
import bio.pih.genoogle.index.InvalidHeaderData;
import bio.pih.genoogle.index.MemoryInvertedIndex;
import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.index.builder.InvertedIndexBuilder;
import bio.pih.genoogle.io.proto.Io.StoredSequence;

/**
 * A data bank witch index its sequences and uses similar subsequences index.
 * 
 * @author albrecht
 * 
 */
public class IndexedDNASequenceDataBank extends AbstractDNASequenceDataBank implements IndexedSequenceDataBank {

	private final MemoryInvertedIndex index;
	private InvertedIndexBuilder indexBuilder;
	protected final DNAMaskEncoder maskEncoder;

	public IndexedDNASequenceDataBank(String name, int subSequenceLength, String mask,
			File path, DatabankCollection<? extends AbstractDNASequenceDataBank> parent, int lowComplexityFilter) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {
		super(name, subSequenceLength, path, parent, lowComplexityFilter);

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

	public void encodeSequences() throws IOException, NoSuchElementException, BioException, ValueOutOfBoundsException, IndexConstructionException {
		beginIndexBuild();
		super.encodeSequences();
		endIndexBuild();
	}

	public void beginIndexBuild() throws IndexConstructionException {
		indexBuilder = new InvertedIndexBuilder(index);
		indexBuilder.constructIndex();
	}

	public void endIndexBuild() throws IndexConstructionException {
		indexBuilder.finishConstruction();
		indexBuilder = null;
	}

	@Override
	public int doSequenceProcessing(int sequenceId, StoredSequence storedSequence) throws IllegalSymbolException,
			BioException, IndexConstructionException {
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
