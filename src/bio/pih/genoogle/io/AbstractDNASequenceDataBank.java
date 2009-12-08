/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import bio.pih.genoogle.index.IndexConstructionException;
import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.io.proto.Io.StoredDatabank;
import bio.pih.genoogle.io.proto.Io.StoredSequence;
import bio.pih.genoogle.io.proto.Io.StoredSequenceInfo;
import bio.pih.genoogle.io.proto.Io.StoredDatabank.SequenceType;
import bio.pih.genoogle.io.reader.IOTools;
import bio.pih.genoogle.io.reader.ParseException;
import bio.pih.genoogle.io.reader.RichSequenceStreamReader;
import bio.pih.genoogle.seq.DNAAlphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.RichSequence;

import com.google.protobuf.ByteString;

/**
 * Abstract class for Sequence Banks which stores DNA sequences
 * 
 * @author albrecht
 * 
 */
public abstract class AbstractDNASequenceDataBank extends AbstractSequenceDataBank {

	private volatile int nextSequenceId;
	private int numberOfSequences;
	private long dataBankSize;
	private StoredDatabank storedDatabank;

	private File dataBankFile = null;
	private File storedDataBankInfoFile = null;

	Logger logger = Logger.getLogger(AbstractSequenceDataBank.class.getCanonicalName());

	public AbstractDNASequenceDataBank(String name, int subSequenceLength, File path,
			DatabankCollection<? extends AbstractDNASequenceDataBank> parent, int lowComplexityFilter) {
		super(name, DNAAlphabet.SINGLETON, subSequenceLength, path, parent, lowComplexityFilter);
		this.nextSequenceId = 0;
		this.numberOfSequences = 0;
		this.dataBankSize = 0;
		this.storedDatabank = null;
	}

	@Override
	public synchronized boolean load() throws IOException, ValueOutOfBoundsException {
		logger.info("Loading databank '" + getDataBankFile() + "'.");

		long begin = System.currentTimeMillis();
		if (!getDataBankFile().exists() || !getStoredDataBankInfoFile().exists()) {
			logger.fatal("Databank " + this.getName() + " is not encoded. Please encode it.");
			return false;
		}

		this.storedDatabank = StoredDatabank.parseFrom(new FileInputStream(getStoredDataBankInfoFile()));

		logger.info("Databank with : " + storedDatabank.getQtdSequences() + " sequences.");
		logger.info("Databank with : " + storedDatabank.getQtdBases() + " bases.");
		logger.info("Databank with : " + storedDatabank.getQtdBases() / 11 + " sub-sequences bases aprox.");

		this.numberOfSequences = storedDatabank.getQtdSequences();
		this.dataBankSize = storedDatabank.getQtdBases();

		logger.info("Databank loaded in " + (System.currentTimeMillis() - begin) + "ms with " + this.numberOfSequences
				+ " sequences.");
		return true;
	}

	/**
	 * @param sequenceId
	 * @return {@link StoredSequence} of the given sequenceId.
	 */
	public synchronized StoredSequence getSequenceFromId(int sequenceId) throws IOException {
		MappedByteBuffer mappedIndexFile = getMappedIndexFile();
		StoredSequenceInfo storedSequenceInfo = storedDatabank.getSequencesInfo(sequenceId);

		byte[] data = new byte[storedSequenceInfo.getLength()];
		mappedIndexFile.position(storedSequenceInfo.getOffset());
		mappedIndexFile.get(data);

		return StoredSequence.parseFrom(data);
	}

	WeakReference<MappedByteBuffer> mappedIndexFile = new WeakReference<MappedByteBuffer>(null);

	private MappedByteBuffer getMappedIndexFile() throws IOException {
		if (mappedIndexFile.get() == null) {
			FileChannel channel = new FileInputStream(getDataBankFile()).getChannel();
			mappedIndexFile = new WeakReference<MappedByteBuffer>(channel.map(MapMode.READ_ONLY, 0,
					getDataBankFile().length()));
		}
		return mappedIndexFile.get();
	}

	public void encodeSequences() throws IOException, NoSuchElementException, ValueOutOfBoundsException,
			IndexConstructionException, ParseException, IllegalSymbolException {
		if (getDataBankFile().exists()) {
			throw new IOException("File " + getDataBankFile()
					+ " already exists. Please remove it before creating another file.");
		}
		addFastaFile(getFullPath());
	}

	public synchronized void addFastaFile(File fastaFile) throws NoSuchElementException, IOException,
			IndexConstructionException, ParseException, IllegalSymbolException {
		logger.info("Adding a FASTA file from " + fastaFile);
		long begin = System.currentTimeMillis();
		FileChannel dataBankFileChannel = new FileOutputStream(getDataBankFile(), true).getChannel();
		FileChannel storedSequenceInfoChannel = new FileOutputStream(getStoredDataBankInfoFile(), true).getChannel();
		bio.pih.genoogle.io.proto.Io.StoredDatabank.Builder storedDatabankBuilder = StoredDatabank.newBuilder();

		BufferedReader is = new BufferedReader(new FileReader(fastaFile));
		RichSequenceStreamReader readFastaDNA = IOTools.readFastaDNA(is);

		while (readFastaDNA.hasNext()) {
			RichSequence s = readFastaDNA.nextRichSequence();
			StoredSequenceInfo addSequence = addSequence(s, dataBankFileChannel);
			storedDatabankBuilder.addSequencesInfo(addSequence);
		}

		storedDatabankBuilder.setType(SequenceType.DNA);
		storedDatabankBuilder.setQtdSequences(numberOfSequences);
		storedDatabankBuilder.setQtdBases(dataBankSize);

		storedDatabank = storedDatabankBuilder.build();
		storedSequenceInfoChannel.write(ByteBuffer.wrap(storedDatabank.toByteArray()));

		storedSequenceInfoChannel.close();
		dataBankFileChannel.close();
		logger.info("FASTA file added in " + (System.currentTimeMillis() - begin) + "ms");
	}

	synchronized StoredSequenceInfo addSequence(RichSequence s, FileChannel dataBankFileChannel) throws IOException,
			IndexConstructionException, IllegalSymbolException {
		if (!s.getAlphabet().equals(this.alphabet)) {
			logger.fatal("Invalid alphabet for sequence " + s.getName());
			return null;
		}

		if (s.getLength() < 8) {
			logger.error(s.getName() + "is too short (" + s.getLength() + ") and will not be stored in this data bank");
			return null;
		}

		long offset = dataBankFileChannel.position();

		final byte[] ret = intArrayToByteArray(s);

		int id = getNextSequenceId();
		bio.pih.genoogle.io.proto.Io.StoredSequence.Builder builder = StoredSequence.newBuilder().setId(id).setGi(
				s.getIdentifier()).setName(s.getName()).setAccession(s.getAccession()).setVersion(s.getVersion()).setDescription(
				s.getDescription()).setEncodedSequence(ByteString.copyFrom(ret));

		StoredSequence storedSequence = builder.build();

		byte[] byteArray = storedSequence.toByteArray();
		dataBankFileChannel.write(ByteBuffer.wrap(byteArray));

		doSequenceProcessing(numberOfSequences, storedSequence);

		numberOfSequences++;

		if (offset > Integer.MAX_VALUE) {
			throw new IOException("The offset position is too big.");
		}

		return StoredSequenceInfo.newBuilder().setId(id).setOffset((int) offset).setLength(byteArray.length).build();
	}

	private byte[] intArrayToByteArray(RichSequence s) {
		int[] encoded = encoder.encodeSymbolListToIntegerArray(s);

		ByteBuffer byteBuf = ByteBuffer.allocate(encoded.length * 4);
		for (int i = 0; i < encoded.length; i++) {
			byteBuf.putInt(encoded[i]);
		}

		return byteBuf.array();
	}

	abstract public int doSequenceProcessing(int sequenceId, StoredSequence storedSequence)
			throws IndexConstructionException, IllegalSymbolException;

	protected static void checkFile(File file, boolean readOnly) throws IOException {
		if (file.exists()) {
			if (!file.canRead()) {
				throw new IOException("File " + file.getCanonicalPath() + " exists but is not readable");
			}
			if (!readOnly & !file.canWrite()) {
				throw new IOException("File " + file.getCanonicalPath() + " exists but is not writable");
			}
		} else if (readOnly) {
			throw new IOException("File " + file.getCanonicalPath()
					+ " does not exist and can not be marked as read-only");
		}
	}

	protected int getNextSequenceId() {
		int id = nextSequenceId;
		nextSequenceId++;
		return id;
	}

	public synchronized int getNumberOfSequences() {
		return numberOfSequences;
	}

	protected synchronized File getDataBankFile() {
		if (dataBankFile == null) {
			dataBankFile = new File(getFullPath() + ".dsdb");
		}
		return dataBankFile;
	}

	protected synchronized File getStoredDataBankInfoFile() {
		if (storedDataBankInfoFile == null) {
			storedDataBankInfoFile = new File(getFullPath() + ".ssdb");
		}
		return storedDataBankInfoFile;
	}

	@Override
	public String toString() {
		return this.name + "@" + this.getFullPath();
	}

	public boolean check() {
		if (getDataBankFile().exists() && getStoredDataBankInfoFile().exists()) {
			return true;
		}
		return false;
	}

	@Override
	public void delete() {
		if (getDataBankFile().exists()) {
			boolean delete = getDataBankFile().delete();
			if (!delete) {
				logger.error(getDataBankFile() + " can not be deleted.");
			}
		}

		if (getStoredDataBankInfoFile().exists()) {
			boolean delete = getStoredDataBankInfoFile().delete();
			if (!delete) {
				logger.error(getStoredDataBankInfoFile() + " can not be deleted.");
			}
		}

	}

	public long getDataBaseSize() {
		return dataBankSize;
	}

	@Override
	public long getTotalDataBaseSize() {
		if (parent == null) {
			return getDataBaseSize();
		}
		return parent.getTotalDataBaseSize();
	}

	@Override
	public long getTotalNumberOfSequences() {
		if (parent == null) {
			return getNumberOfSequences();
		}
		return parent.getTotalNumberOfSequences();
	}
}
