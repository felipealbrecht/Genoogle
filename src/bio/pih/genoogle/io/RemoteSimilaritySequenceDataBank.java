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

import pih.bio.genoogle.seq.protein.Converter;
import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.encoder.SequenceEncoderFactory;
import bio.pih.genoogle.index.IndexConstructionException;
import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.io.proto.Io.StoredSequence;
import bio.pih.genoogle.io.proto.Io.StoredSequenceInfo;
import bio.pih.genoogle.seq.Alphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.Reduced_AA_8_Alphabet;
import bio.pih.genoogle.seq.RichSequence;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.util.SymbolListWindowIteratorFactory;

import com.google.protobuf.ByteString;

/**
 * A data bank witch index its sequences and uses similar subsequences index.
 * 
 * @author albrecht
 * 
 */
public class RemoteSimilaritySequenceDataBank extends IndexedSequenceDataBank {

	SymbolListWindowIteratorFactory factory = SymbolListWindowIteratorFactory.getNotOverlappedFactory();
	SequenceEncoder reducedEncoder = SequenceEncoderFactory.getEncoder(Reduced_AA_8_Alphabet.SINGLETON, 3);

	public RemoteSimilaritySequenceDataBank(String name, Alphabet alphabet, int subSequenceLength, File path, AbstractDatabankCollection<? extends AbstractSimpleSequenceDataBank> parent) throws ValueOutOfBoundsException {
		super(name, alphabet, subSequenceLength, null, path, parent);
	}

	synchronized StoredSequenceInfo[] addSequence(RichSequence s, FileChannel dataBankFileChannel) throws IOException, IndexConstructionException, IllegalSymbolException {
		if (!s.getAlphabet().equals(this.alphabet)) {
			logger.fatal("Invalid alphabet for sequence " + s.getName());
			return null;
		}

		if (s.getLength() < 8) {
			logger.error(s.getName() + "is too short (" + s.getLength() + ") and will not be stored in this data bank");
			return null;
		}

		return processReads(s, dataBankFileChannel);
	}

	private StoredSequenceInfo[] processReads(RichSequence s, FileChannel dataBankFileChannel) throws IOException, IndexConstructionException, IllegalSymbolException {
		StoredSequenceInfo info;
		StoredSequenceInfo[] infos = new StoredSequenceInfo[6];
		
		info = processRead1(s, dataBankFileChannel);
		infos[0] = info;
		
		info = processRead2(s, dataBankFileChannel);
		infos[1] = info;
		
		info = processRead3(s, dataBankFileChannel);
		infos[2] = info;
		
		info = processRead4(s, dataBankFileChannel);
		infos[3] = info;
		
		info = processRead5(s, dataBankFileChannel);
		infos[4] = info;

		info = processRead6(s, dataBankFileChannel);
		infos[5] = info;
		
		return infos;
	}

	private StoredSequenceInfo processRead1(RichSequence s, FileChannel dataBankFileChannel) throws IOException, IndexConstructionException, IllegalSymbolException {
		SymbolList protein = Converter.dnaToProtein1(s);
		SymbolList reduced = Converter.proteinToReducedAA(protein);
		return storeInDatabase(s, reduced, 1, dataBankFileChannel);
	}

	private StoredSequenceInfo processRead2(RichSequence s, FileChannel dataBankFileChannel) throws IOException, IndexConstructionException, IllegalSymbolException {
		SymbolList protein = Converter.dnaToProtein2(s);
		SymbolList reduced = Converter.proteinToReducedAA(protein);
		return storeInDatabase(s, reduced, 2, dataBankFileChannel);
	}

	private StoredSequenceInfo processRead3(RichSequence s, FileChannel dataBankFileChannel) throws IOException, IndexConstructionException, IllegalSymbolException {
		SymbolList protein = Converter.dnaToProtein3(s);
		SymbolList reduced = Converter.proteinToReducedAA(protein);
		return storeInDatabase(s, reduced, 3, dataBankFileChannel);
	}

	private StoredSequenceInfo processRead4(RichSequence s, FileChannel dataBankFileChannel) throws IOException, IndexConstructionException, IllegalSymbolException {
		SymbolList protein = Converter.dnaToProteinReverse1(s);
		SymbolList reduced = Converter.proteinToReducedAA(protein);
		return storeInDatabase(s, reduced, 4, dataBankFileChannel);
	}

	private StoredSequenceInfo processRead5(RichSequence s, FileChannel dataBankFileChannel) throws IOException, IndexConstructionException, IllegalSymbolException {
		SymbolList protein = Converter.dnaToProteinReverse2(s);
		SymbolList reduced = Converter.proteinToReducedAA(protein);
		return storeInDatabase(s, reduced, 5, dataBankFileChannel);
	}

	private StoredSequenceInfo processRead6(RichSequence s, FileChannel dataBankFileChannel) throws IOException, IndexConstructionException, IllegalSymbolException {
		SymbolList protein = Converter.dnaToProteinReverse3(s);
		SymbolList reduced = Converter.proteinToReducedAA(protein);
		return storeInDatabase(s, reduced, 6, dataBankFileChannel);
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

		doSequenceProcessing(numberOfSequences, storedSequence);

		this.numberOfSequences++;
		this.dataBankSize += s.getLength();

		return StoredSequenceInfo.newBuilder().setId(id).setOffset(offset).setLength(byteArray.length).build();
	}

	@Override
	protected byte[] intArrayToByteArray(SymbolList s) {
		int[] encoded = reducedEncoder.encodeSymbolListToIntegerArray(s);

		ByteBuffer byteBuf = ByteBuffer.allocate(encoded.length * 4);
		for (int i = 0; i < encoded.length; i++) {
			byteBuf.putInt(encoded[i]);
		}

		return byteBuf.array();
	}

	@Override
	public int doSequenceProcessing(int sequenceId, StoredSequence storedSequence) throws IndexConstructionException, IllegalSymbolException {
		int[] encodedSequence = Utils.getEncodedSequenceAsArray(storedSequence);
		int size = SequenceEncoder.getSequenceLength(encodedSequence);
		indexBuilder.addSequence(sequenceId, encodedSequence);
		return size;
	}
}
