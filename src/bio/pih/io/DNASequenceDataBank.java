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
	private final boolean readOnly;

	private File dataBankFile;
	
	FileChannel dataBankFileChannel = null;

	// Map the sequence gi to its position 
	HashMap<String, Integer> giToSequenceInformationOffset = Maps.newHashMap();

	private volatile static boolean isRecording = false;

	private final String[] extensions = new String[] { "dsdb" }; // Dna Sequences Data Bank

	static DNASequenceEncoderToShort encoder = DNASequenceEncoderToShort.getDefaultEncoder();

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
	}

	public void loadInformations() throws IOException {
		dataBankFile = new File(path, name + ".dsdb");
		checkFile(dataBankFile, readOnly);
		if (readOnly) {
			dataBankFile.setReadOnly();
		}
		dataBankFileChannel = new FileInputStream(dataBankFile).getChannel();
		loadData();
	}
	

	void loadData() throws IOException {	
		MappedByteBuffer mappedIndexFile = this.dataBankFileChannel.map(MapMode.READ_ONLY, 0, dataBankFile.length());
		
		SequenceInformation sequenceInformation  = null;
		int variableLength = 0;
		int sequenceInformationPosition;
		while (mappedIndexFile.position() + SequenceInformation.getUnvariableCapacity() < mappedIndexFile.capacity()) {
			sequenceInformationPosition = mappedIndexFile.position();
			variableLength = mappedIndexFile.getInt();
			sequenceInformation = SequenceInformation.informationFromByteBuffer(mappedIndexFile, variableLength);
			giToSequenceInformationOffset.put(sequenceInformation.getGi(), sequenceInformationPosition);
			System.out.println(sequenceInformation.getGi());
			
			try {
				getSymbolListFromGi(sequenceInformation.getGi());
			} catch (IllegalSymbolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public SymbolList getSymbolListFromGi(String gi) throws IOException, IllegalSymbolException {
		int position = giToSequenceInformationOffset.get(gi);
		
		MappedByteBuffer mappedIndexFile = this.dataBankFileChannel.map(MapMode.READ_ONLY, 0, dataBankFile.length());
		mappedIndexFile.position(position);
		
		int variableLength = mappedIndexFile.getInt();
		SequenceInformation sequenceInformation = SequenceInformation.informationFromByteBuffer(mappedIndexFile, variableLength);						
		String decodeShortArrayToString = DNASequenceEncoderToShort.getDefaultEncoder().decodeShortArrayToString(sequenceInformation.getEncodedSequence());

		return LightweightSymbolList.createDNA(decodeShortArrayToString);
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

	protected File getDataBankFile() {
		return dataBankFile;
	}

	public void addFastaFile(File fastaFile) throws NoSuchElementException, BioException, IOException {
		if (readOnly) {
			throw new IOException("The file " + dataBankFile + " is marked as read only");
		}

		FileChannel dataBankFileChannel = new FileOutputStream(dataBankFile).getChannel();

		BufferedReader is = new BufferedReader(new FileReader(fastaFile));

		LightweightStreamReader readFastaDNA = LightweightIOTools.readFastaDNA(is, null);
		RichSequence s;

		long begin = System.currentTimeMillis();

		int count = 0;
		//for (count = 0; count < 1000; count++) {
		while (readFastaDNA.hasNext()) {
			s = readFastaDNA.nextRichSequence();
			addSequence(s, dataBankFileChannel);
			count++;
		}
		System.out.println(count);

		dataBankFileChannel.close();
		System.out.println(System.currentTimeMillis() - begin);
	}

	public synchronized void addSequence(RichSequence s) throws IOException, BioException {
		FileChannel dataBankFileChannel = new FileOutputStream(dataBankFile).getChannel();

		addSequence(s, dataBankFileChannel);

		dataBankFileChannel.close();
	}

	synchronized void addSequence(RichSequence s, FileChannel dataBankFileChannel) throws BioException, IOException {
		if (readOnly) {
			throw new IOException("The file " + dataBankFile + " is marked as read only");
		}

		if (!s.getAlphabet().equals(this.alphabet)) {
			throw new BioException("Invalid alphabet for sequence " + s.getName());
		}

		if (s.length() < 8) {
			System.out.println(s.getName() + "is too short (" + s.length() + ") and will not be stored in this data bank");
			return;
		}
		
		beginRecordind();

		short[] encodedSequence = encoder.encodeSymbolListToShortArray(s);
		SequenceInformation si = new SequenceInformation(s.getIdentifier(), s.getName(), s.getAccession(), (byte) s.getVersion(), s.getDescription(), encodedSequence);
		ByteBuffer bb = si.toByteBuffer();
		bb.rewind();
		
		dataBankFileChannel.write(bb);
		
		endRecordind();		
	}

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
