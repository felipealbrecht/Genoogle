/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.encoder.SequenceEncoderFactory;
import bio.pih.genoogle.index.IndexConstructionException;
import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.io.proto.Io.StoredSequence;
import bio.pih.genoogle.io.proto.Io.StoredSequenceInfo;
import bio.pih.genoogle.seq.Alphabet;
import bio.pih.genoogle.seq.AminoAcidAlphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.Reduced_AA_8_Alphabet;
import bio.pih.genoogle.seq.RichSequence;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.seq.protein.Converter;
import bio.pih.genoogle.util.SymbolListWindowIteratorFactory;

import com.google.protobuf.ByteString;

/**
 * A data bank witch index its sequences and uses similar subsequences index.
 *
 * @author albrecht
 *
 */
public class RemoteSimilaritySequenceDataBank extends IndexedSequenceDataBank {

	static SymbolListWindowIteratorFactory factory = SymbolListWindowIteratorFactory.getNotOverlappedFactory();
	static SequenceEncoder aaEncoder = SequenceEncoderFactory.getEncoder(AminoAcidAlphabet.SINGLETON, 6);
	static SequenceEncoder reducedEncoder = SequenceEncoderFactory.getEncoder(Reduced_AA_8_Alphabet.SINGLETON, 9);

	public RemoteSimilaritySequenceDataBank(String name, Alphabet alphabet, int subSequenceLength, File path, AbstractDatabankCollection<? extends AbstractSimpleSequenceDataBank> parent) throws ValueOutOfBoundsException {
		super(name, alphabet, subSequenceLength, reducedEncoder, null, path, parent);
	}

	synchronized StoredSequenceInfo[] addSequence(RichSequence s, FileChannel dataBankFileChannel) throws IOException, IndexConstructionException, IllegalSymbolException {
		if (!s.getAlphabet().equals(this.alphabet)) {
			logger.fatal("Invalid symbol in the sequence for sequence " + s.getName() + ". This sequence will be ignored.");
			return new StoredSequenceInfo[] {};
		}

		if (s.getLength() < 8) {
			logger.info(s.getName() + "is too short (" + s.getLength() + ") and will not be stored in this data bank");
			return new StoredSequenceInfo[] {};
		}

		return processReads(s, dataBankFileChannel);
	}

	private StoredSequenceInfo[] processReads(RichSequence s, FileChannel dataBankFileChannel) throws IOException, IndexConstructionException, IllegalSymbolException {
		StoredSequenceInfo info;
		StoredSequenceInfo[] infos = new StoredSequenceInfo[1];

		info = processRead1(s, dataBankFileChannel);
		infos[0] = info;

		return infos;
	}

	private StoredSequenceInfo processRead1(RichSequence s, FileChannel dataBankFileChannel) throws IOException, IndexConstructionException, IllegalSymbolException {
		SymbolList protein = Converter.dnaToProtein1(s);
		return storeInDatabase(s, protein, 1, dataBankFileChannel);
	}

	private StoredSequenceInfo storeInDatabase(RichSequence s, SymbolList converted, int read, FileChannel dataBankFileChannel) throws IOException, IndexConstructionException, IllegalSymbolException {
		long offset = dataBankFileChannel.position();

		final byte[] ret = intArrayToByteArray(converted);

		int id = getNextSequenceId();

		bio.pih.genoogle.io.proto.Io.StoredSequence.Builder builder = StoredSequence.newBuilder()
			.setId(id)
			.setGi(s.getGi())
			.setName(s.getName())
			.setType(s.getType())
			.setAccession(s.getAccession())
			.setDescription(s.getDescription())
			.setRead(read)
			.setEncodedSequence(ByteString.copyFrom(ret));

		StoredSequence storedSequence = builder.build();
		byte[] byteArray = storedSequence.toByteArray();
		dataBankFileChannel.write(ByteBuffer.wrap(byteArray));

		SymbolList reducedAA = Converter.proteinToReducedAA(converted);
		int[] reducedEncoded = reducedEncoder.encodeSymbolListToIntegerArray(reducedAA);
		doSequenceProcessing(id, reducedEncoded);

		this.numberOfSequences++;
		this.dataBankSize += converted.getLength();

		return StoredSequenceInfo.newBuilder().setId(id).setOffset(offset).setLength(byteArray.length).build();
	}

	@Override
	protected byte[] intArrayToByteArray(SymbolList s) {
		int[] encoded = aaEncoder.encodeSymbolListToIntegerArray(s);

		ByteBuffer byteBuf = ByteBuffer.allocate(encoded.length * 4);
		for (int i = 0; i < encoded.length; i++) {
			byteBuf.putInt(encoded[i]);
		}

		return byteBuf.array();
	}

	@Override
	public int doSequenceProcessing(int sequenceId, int[] encodedSequence) throws IndexConstructionException, IllegalSymbolException {
		int size = SequenceEncoder.getSequenceLength(encodedSequence);
		indexBuilder.addSequence(sequenceId, encodedSequence);
		return size;
	}

	@Override
	public int getSubSequencesOffset() {
		return reducedEncoder.getSubSequenceLength();
	}


	public SequenceEncoder getAaEncoder() {
		return aaEncoder;
	}

	public SequenceEncoder getReducedEncoder() {
		return reducedEncoder;
	}
}
