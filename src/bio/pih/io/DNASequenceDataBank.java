package bio.pih.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.biojavax.bio.seq.RichSequence;

import bio.pih.encoder.DNASequenceEncoderToShort;
import bio.pih.seq.LightweightSymbolList;
import bio.pih.seq.op.LightweightIOTools;
import bio.pih.seq.op.LightweightStreamReader;

import com.google.common.collect.Maps;

/**
 * An abstract class for Sequence Banks that uses DNA sequences
 * 
 * @author albrecht
 * 
 */
public abstract class DNASequenceDataBank implements SequenceDataBank {

	private final FiniteAlphabet alphabet = DNATools.getDNA();
	private final String name;
	private final File path;
	
	private volatile int nextSequenceId;
	private int totalSequences;
	
	protected final boolean readOnly;
	private File dataBankFile;		
	private FileChannel dataBankFileChannel;
	private volatile static boolean isRecording = false;
	
	Logger logger = Logger.getLogger("bio.pih.io.DNASequenceDataBank");

	static DNASequenceEncoderToShort encoder = DNASequenceEncoderToShort.getDefaultEncoder();
	
	private final String[] extensions = new String[] { "dsdb" }; // Dna Sequences Data Bank
	
	// Map the sequence id to its position 
	HashMap<Integer, Integer> sequenceIdToSequenceInformationOffset = Maps.newHashMap();

	/**
	 * Default constructor for all DNASequenceDataBank.
	 * 
	 * @param name
	 *            the name of the data bank.
	 * @param path
	 *            the path where will be stored.
	 * @param readOnly
	 *            if the data will be read only, no new sequences added.
	 * @throws IOException
	 */
	public DNASequenceDataBank(String name, File path, boolean readOnly) {
		this.name = name;
		this.path = path;
		this.readOnly = readOnly;
		this.nextSequenceId = 0;
		this.totalSequences = 0;
	}

	public synchronized void loadInformations() throws IOException {
		
		dataBankFile = new File(path, name + ".dsdb");
		checkFile(dataBankFile, readOnly);
		if (readOnly) {
			dataBankFile.setReadOnly();
		}
		dataBankFileChannel = new FileInputStream(dataBankFile).getChannel();
		loadData();			
	}
	

	synchronized void loadData() throws IOException {
		logger.info("Loading databank from " + getDataBankFile());
		long begin = System.currentTimeMillis();
		
		MappedByteBuffer mappedIndexFile = this.dataBankFileChannel.map(MapMode.READ_ONLY, 0, getDataBankFile().length());
		
		SequenceInformation sequenceInformation  = null;
		int variableLength = 0;
		int sequenceInformationPosition;
		while (mappedIndexFile.position() + SequenceInformation.getUnvariableCapacity() < mappedIndexFile.capacity()) {
			sequenceInformationPosition = mappedIndexFile.position();
			variableLength = mappedIndexFile.getInt();
			sequenceInformation = SequenceInformation.informationFromByteBuffer(mappedIndexFile, variableLength);
			if (sequenceInformation.getId() > this.nextSequenceId) {
				this.nextSequenceId = sequenceInformation.getId() + 1;
			}
			sequenceIdToSequenceInformationOffset.put(sequenceInformation.getId(), sequenceInformationPosition);
			totalSequences++;
			doSequenceLoadingProcessing(sequenceInformation);
		}
		doOptimizations();
		logger.info("Databank loaded in " + (System.currentTimeMillis() - begin) + "ms with " +totalSequences+" sequences." );
	}
	
	abstract void doSequenceLoadingProcessing(SequenceInformation sequenceInformation);	

	public SymbolList getSymbolListFromSequenceId(int sequenceId) throws IOException, IllegalSymbolException {
		int position = sequenceIdToSequenceInformationOffset.get(sequenceId);
		
		MappedByteBuffer mappedIndexFile = this.dataBankFileChannel.map(MapMode.READ_ONLY, 0, getDataBankFile().length());
		mappedIndexFile.position(position);
		
		int variableLength = mappedIndexFile.getInt();
		SequenceInformation sequenceInformation = SequenceInformation.informationFromByteBuffer(mappedIndexFile, variableLength);						
		String decodeShortArrayToString = DNASequenceEncoderToShort.getDefaultEncoder().decodeShortArrayToString(sequenceInformation.getEncodedSequence());
		
		return LightweightSymbolList.createDNA(decodeShortArrayToString);
	}


	public void addFastaFile(File fastaFile) throws NoSuchElementException, BioException, IOException {
		logger.info("Adding a FASTA file from " + fastaFile);
		long begin = System.currentTimeMillis();
		
		FileChannel dataBankFileChannel = new FileOutputStream(getDataBankFile(), true).getChannel();

		BufferedReader is = new BufferedReader(new FileReader(fastaFile));

		LightweightStreamReader readFastaDNA = LightweightIOTools.readFastaDNA(is, null);
		RichSequence s;

		while (readFastaDNA.hasNext()) {
			s = readFastaDNA.nextRichSequence();
			addSequence(s, dataBankFileChannel);
		}

		dataBankFileChannel.close();
		doOptimizations();
		logger.info("FASTA file added in " + (System.currentTimeMillis() - begin) + "ms");
	}
	
	abstract void doOptimizations();

	public synchronized int addSequence(RichSequence s) throws IOException, BioException {
		
		FileChannel dataBankFileChannel = new FileOutputStream(getDataBankFile()).getChannel();
		int sequenceId = addSequence(s, dataBankFileChannel);
		dataBankFileChannel.close();
		
		return sequenceId;
	}

	private synchronized int addSequence(RichSequence s, FileChannel dataBankFileChannel) throws BioException, IOException {
		if (readOnly) {
			throw new IOException("The file " + getDataBankFile() + " is marked as read only");
		}

		if (!s.getAlphabet().equals(this.alphabet)) {
			throw new BioException("Invalid alphabet for sequence " + s.getName());
		}

		if (s.length() < 8) {
			System.out.println(s.getName() + "is too short (" + s.length() + ") and will not be stored in this data bank");
			return -1;
		}
		
		beginRecordind();

		short[] encodedSequence = encoder.encodeSymbolListToShortArray(s);		
		SequenceInformation si = new SequenceInformation(getNextSequenceId(), s.getIdentifier(), s.getName(), s.getAccession(), (byte) s.getVersion(), s.getDescription(), encodedSequence);		
		ByteBuffer bb = si.toByteBuffer();
		bb.rewind();		
		dataBankFileChannel.write(bb);	
		doSequenceAddingProcessing(si);
		totalSequences++;
		
		endRecordind();		
		
		return si.getId();
	}
	
	abstract void doSequenceAddingProcessing(SequenceInformation sequenceInformation);

	protected static void checkFile(File file, boolean readOnly) throws IOException {
		if (file.exists()) {
			if (!file.canRead()) {
				throw new IOException("File " + file.getCanonicalPath() + " exists but is not readable");
			}
			if (!readOnly & !file.canWrite()) {
				throw new IOException("File " + file.getCanonicalPath() + " exists but is not writable");
			}
		} else if (readOnly) {
			throw new IOException("File " + file.getCanonicalPath() + " does not exist and can not be marked as read-only");
		} else {
			file.createNewFile();
		}
	}

	protected int getNextSequenceId() {
		int id = nextSequenceId;
		nextSequenceId++;
		return id;
	}
	
	public int getTotalSequences() {
		return totalSequences;
	}
	
	public void setAlphabet(FiniteAlphabet alphabet) {
		throw new IllegalStateException("The alphabet is imutable for this class");
	}
	
	public FiniteAlphabet getAlphabet() {
		return alphabet;
	}
	
	public void setExtensions(String[] extensions) {
		throw new IllegalStateException("The extension name is imutable for this class");
	}
	
	public String[] getExtensions() {
		return extensions;
	}
	
	public void setName(String name) {
		throw new IllegalStateException("The name is imutable for a DataBank");
	}
	
	public String getName() {
		return name;
	}
	
	public void setPath(File directory) {
		throw new IllegalStateException("The path is imutable for a DataBank");
	}
	
	public File getPath() {
		return path;
	}
	
	private File getDataBankFile() {
		return dataBankFile;
	}
	
	synchronized void beginRecordind() {
		isRecording = true;
	}

	synchronized void endRecordind() {
		isRecording = false;
	}

	synchronized boolean checkRecording() {
		return isRecording;
	}

}
