/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import bio.pih.genoogle.encoder.MaskEncoder;
import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.index.IndexConstructionException;
import bio.pih.genoogle.index.MemoryInvertedIndex;
import bio.pih.genoogle.index.SubSequenceIndexInfo;
import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.index.builder.InvertedIndexBuilder;
import bio.pih.genoogle.io.proto.Io.StoredDatabank;
import bio.pih.genoogle.io.proto.Io.StoredSequence;
import bio.pih.genoogle.io.reader.ParseException;
import bio.pih.genoogle.seq.Alphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.SymbolList;

/**
 * A data bank witch index its sequences and uses similar subsequences index.
 * 
 * @author albrecht
 * 
 */
public class IndexedSequenceDataBank extends AbstractSimpleSequenceDataBank {

	protected final MemoryInvertedIndex index;
	protected InvertedIndexBuilder indexBuilder;
	protected final MaskEncoder maskEncoder;
	private final String mask;

	public IndexedSequenceDataBank(String name, Alphabet alphabet, int subSequenceLength, String mask, File path,
			AbstractDatabankCollection<? extends AbstractSimpleSequenceDataBank> parent)
			throws ValueOutOfBoundsException {
		super(name, alphabet, subSequenceLength, path, parent);
		this.mask = mask;

		if (mask != null) {			
			maskEncoder = new MaskEncoder(mask, encoder);
		} else {
			maskEncoder = null;
		}

		index = new MemoryInvertedIndex(this, subSequenceLength);
	}

	@Override
	public synchronized boolean load() throws IOException, ValueOutOfBoundsException {
		boolean b = super.load();
		if (b == false) {
			return false;
		}
		index.loadFromFile();
		return true;
	}

	public void encodeSequences(boolean forceFormating) throws IOException, NoSuchElementException, ValueOutOfBoundsException,
			IndexConstructionException, ParseException, IllegalSymbolException {
		beginIndexBuild();
		super.encodeSequences(forceFormating);
		endIndexBuild();
	}

	public void beginIndexBuild() throws IndexConstructionException {
		indexBuilder = new InvertedIndexBuilder(this);
		indexBuilder.constructIndex();
	}

	public void endIndexBuild() throws IndexConstructionException {
		indexBuilder.finishConstruction();
		indexBuilder = null;
	}

	@Override
	public int doSequenceProcessing(int sequenceId, StoredSequence storedSequence) throws IndexConstructionException, IllegalSymbolException {
		int[] encodedSequence = Utils.getEncodedSequenceAsArray(storedSequence);
		int size = SequenceEncoder.getSequenceLength(encodedSequence);
		if (maskEncoder == null) {
			indexBuilder.addSequence(sequenceId, encodedSequence);
		} else {
			SymbolList sequence = encoder.decodeIntegerArrayToSymbolList(encodedSequence);
			int[] filteredSequence = maskEncoder.applySequenceMask(sequence);
			indexBuilder.addSequence(sequenceId, filteredSequence);
		}

		return size;
	}
	
	public MemoryInvertedIndex getIndex() {
		return index;
	}
	
	/**
	 * The offset between each subsequence of this data bank.
	 * @return offset between the begin of two together subsequences. 
	 */
	@Override
	public int getSubSequencesOffset() {
		if (maskEncoder == null) {
			return subSequenceLength;
		} else {
			return maskEncoder.getPatternLength();
		}
	}
	
	public MaskEncoder getMaskEncoder() {
		return maskEncoder;
	}
	
	/**
	 * Receive an encodedSubSequence, that is a sub-sequence 8 bases length encoded into a short, 
	 * and return an Array of integer containing the sequence and position that is <b>exactly equals</b> the subsequence.
	 * @param encodedSubSequence 
	 * @return a list containing the {@link SubSequenceIndexInfo} encoded, use {@link SubSequenceIndexInfo} to decode it. 
	 */
	
	public long[] getMatchingSubSequence(int encodedSubSequence) throws ValueOutOfBoundsException, IOException {
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
	
	protected void setStoredDatabankInfo(StoredDatabank.Builder storedDatabankBuilder) {
		super.setStoredDatabankInfo(storedDatabankBuilder);
		if (mask == null) {
			storedDatabankBuilder.setMask("");
		} else {
			storedDatabankBuilder.setMask(mask);
		}
		storedDatabankBuilder.setLowComplexityFilter(getLowComplexityFilter());
	}

}
