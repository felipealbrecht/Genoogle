package bio.pih.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.alignment.GenoogleNeedlemanWunsch;
import bio.pih.alignment.GenoogleSequenceAlignment;
import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.io.XMLConfigurationReader;
import bio.pih.io.proto.Io.StoredComparationResultInfo;
import bio.pih.io.proto.Io.StoredSimilarSubSequences;
import bio.pih.io.proto.Io.StoredSimilarSubSequencesIndex;
import bio.pih.io.proto.Io.StoredSimilarSubSequences.Builder;
import bio.pih.seq.LightweightSymbolList;

import com.google.common.collect.Lists;
import com.google.protobuf.CodedInputStream;

/**
 * Given two sub-sequences, return the *global* alignment between they.
 * 
 * @author albrecht
 * 
 * 
 *         <p>
 *         How to use:
 * 
 *         Create a instance, check if exists, if not createData(), else
 *         loadData().
 * 
 */
public class SimilarSubSequencesIndex {

	private static final char[] ALPHABET = new char[] { 'A', 'C', 'G', 'T' };
	private final GenoogleNeedlemanWunsch aligner;
	private final DNASequenceEncoderToInteger encoder;

	private static final int defaultThreshold = 3;
	private static final int defaultDeeper = 1;
	private static final int defaultMatch = -1;
	private static final int defaultDismatch = 1;
	private static final int defaultGapOpen = 0;
	private static final int defaultGapExtend = 1;
	private static final int defaultSubSequenceLength = XMLConfigurationReader.getSubSequenceLength();

	private final int subSequenceLength;
	private final int threshold;
	private final int deeper;
	private final int match;
	private final int dismatch;
	private final int gapOpen;
	private final int gapExtend;
	private final int maxEncodedSequenceValue;

	private File indexFile = null;
	private File dataFile = null;
	private FileChannel dataFileChannel = null;

	private long[] dataOffsetIndex;
	private int[] dataLengthIndex;

	private boolean isLoad = false;

	private static SimilarSubSequencesIndex defaultInstance = null;

	static Logger logger = Logger.getLogger("bio.pih.index.SubSequencesComparer");

	/**
	 * @return the default instance of {@link SimilarSubSequencesIndex}
	 * @throws ValueOutOfBoundsException
	 */
	public static SimilarSubSequencesIndex getDefaultInstance() {
		if (defaultInstance == null) {
			try {
				defaultInstance = new SimilarSubSequencesIndex(DNATools.getDNA(),
						defaultSubSequenceLength, defaultMatch, defaultDismatch, defaultGapOpen,
						defaultGapExtend, defaultThreshold, defaultDeeper);
			} catch (ValueOutOfBoundsException e) {
				logger.fatal(
						"Fatar error in loading the default SubSequenceComparer. Probably related with subSequenceLength",
						e);
			}
		}
		return defaultInstance;
	}

	/**
	 * Auxiliar main class for generate default data.
	 * 
	 * @param args
	 * @throws IllegalSymbolException
	 * @throws IOException
	 * @throws BioException
	 * @throws InvalidHeaderData
	 * @throws ValueOutOfBoundsException
	 */
	public static void main(String[] args) throws IllegalSymbolException, IOException,
			BioException, InvalidHeaderData {
//		getDefaultInstance().deleteData();
//		getDefaultInstance().generateData(true);
//		getDefaultInstance().load();
//		getDefaultInstance().checkDataData(true);
	}

	/**
	 * @param alphabet
	 * @param subSequenceLength
	 * @param match
	 * @param dismatch
	 * @param gapOpen
	 * @param gapExtend
	 * @param threshold
	 * @param deeper
	 * @throws ValueOutOfBoundsException
	 */
	public SimilarSubSequencesIndex(FiniteAlphabet alphabet, int subSequenceLength, int match,
			int dismatch, int gapOpen, int gapExtend, int threshold, int deeper)
			throws ValueOutOfBoundsException {
		this.subSequenceLength = subSequenceLength;
		this.match = match;
		this.dismatch = dismatch;
		this.gapOpen = gapOpen;
		this.gapExtend = gapExtend;
		this.threshold = threshold;
		this.deeper = deeper;

		SubstitutionMatrix substitutionMatrix = new SubstitutionMatrix(alphabet, match * -1,
				dismatch * -1); // values
		this.aligner = new GenoogleNeedlemanWunsch(match, dismatch, gapOpen, gapOpen, gapExtend,
				substitutionMatrix);
		this.encoder = new DNASequenceEncoderToInteger(subSequenceLength);
		this.maxEncodedSequenceValue = (int) Math.pow(alphabet.size(), subSequenceLength) - 1;
	}

	/**
	 * @return the maximum value for a sequence that is possible for this actual
	 *         alphabet and sequence length
	 */
	public int getMaxEncodedSequenceValue() {
		return this.maxEncodedSequenceValue;
	}

	private String indexFileName = null;

	/**
	 * @return name of the file containing the index of the data of this
	 *         {@link SimilarSubSequencesIndex} score scheme.
	 */
	public String getIndexFileName() {
		if (this.indexFileName == null) {
			StringBuilder sb = new StringBuilder("subSequencesComparer");
			sb.append("_");
			sb.append(subSequenceLength);
			sb.append("_");
			sb.append(match);
			sb.append("_");
			sb.append(dismatch);
			sb.append("_");
			sb.append(gapOpen);
			sb.append("_");
			sb.append(gapExtend);
			sb.append("_");
			sb.append(threshold);
			sb.append(".idx");
			this.indexFileName = sb.toString();
		}
		return this.indexFileName;
	}

	private String dataFileName = null;

	/**
	 * @return name of the file containing the the data of this
	 *         {@link SimilarSubSequencesIndex} score scheme.
	 */
	public String getDataFileName() {
		if (this.dataFileName == null) {
			StringBuilder sb = new StringBuilder("subSequencesComparer");
			sb.append("_");
			sb.append(subSequenceLength);
			sb.append("_");
			sb.append(match);
			sb.append("_");
			sb.append(dismatch);
			sb.append("_");
			sb.append(gapOpen);
			sb.append("_");
			sb.append(gapExtend);
			sb.append("_");
			sb.append(threshold);
			sb.append(".bin");
			this.dataFileName = sb.toString();
		}
		return this.dataFileName;
	}

	private File getIndexFile() {
		if (indexFile == null) {
			indexFile = new File(getIndexFileName());
		}
		return indexFile;
	}

	/**
	 * TODO: check if the file is *really* a index file
	 * 
	 * @return if a file having the appropriate name for index exists.
	 */
	public boolean hasIndexFile() {
		return getIndexFile().isFile() && getIndexFile().exists();
	}

	private File getDataFile() {
		if (dataFile == null) {
			dataFile = new File(getDataFileName());
		}
		return dataFile;
	}

	/**
	 * TODO: check if the file is *really* a data file
	 * 
	 * @return if a file having the appropriate name for data exists.
	 */
	public boolean hasDataFile() {
		return getDataFile().isFile() && getDataFile().exists();
	}

	private FileChannel getDataFileChannel() throws FileNotFoundException {
		if (dataFileChannel == null) {
			dataFileChannel = new FileInputStream(getDataFile()).getChannel();
		}
		return dataFileChannel;
	}

	/**
	 * Check the data consistency.
	 * 
	 * @param verbose
	 * @throws IOException
	 * @throws IllegalSymbolException
	 * @throws BioException
	 * @throws InvalidHeaderData
	 */
	public void checkDataData(boolean verbose) throws IOException, IllegalSymbolException,
			BioException, InvalidHeaderData {

		long initialTime = System.currentTimeMillis();

		System.out.println("Checking data of " + this.toString());
		for (int encodedSequence1 = 0; encodedSequence1 <= maxEncodedSequenceValue; encodedSequence1++) {
			LinkedList<String> resultsString = Lists.newLinkedList();
			LinkedList<Integer> resultsInteger = Lists.newLinkedList();
			SymbolList sequence = getEncoder().decodeIntegerToSymbolList(encodedSequence1);
			long time = System.currentTimeMillis();
			List<String> similarSequences = generateSimilar(sequence.seqString(), deeper, ALPHABET);
			for (String similar : similarSequences) {
				LightweightSymbolList similarSequence = (LightweightSymbolList) LightweightSymbolList.createDNA(similar);
				int encodedSimilar = getEncoder().encodeSubSymbolListToInteger(similarSequence);
				String decodeIntegerToString = DNASequenceEncoderToInteger.getDefaultEncoder()
						.decodeIntegerToString(encodedSimilar);
				assert decodeIntegerToString.equals(similar);
				resultsString.add(similar);
				resultsInteger.add(encodedSimilar);
			}
			List<Integer> retrievedSimilarSequences = getSimilarSequences(encodedSequence1);
			
			assert retrievedSimilarSequences.size() == resultsString.size();
			assert resultsInteger.equals(retrievedSimilarSequences);


			if (encodedSequence1 % 10000 == 0) {
			System.out.println(sequence.seqString() + "\t" + (System.currentTimeMillis() - time)
					+ "\t" + resultsString.size() + "\t" + encodedSequence1 + "\t"
					+ Integer.toHexString(encodedSequence1) + "\t"
					+ Integer.toBinaryString(encodedSequence1));
			}
		}

		if (verbose) {
			System.out.println("-----------------------------");
			System.out.println(" tempo total: " + (System.currentTimeMillis() - initialTime));
			System.out.println("");
		}
	}

	/**
	 * Same <code>load(false)</code>
	 * 
	 * @throws IOException
	 * @throws InvalidHeaderData
	 */
	public void load() throws IOException, InvalidHeaderData {
		load(false);
	}

	/**
	 * Load the data (index and the data itself) for utilize it.
	 * 
	 * @param checkConsitency
	 *            <code>true</code> if the consistency must be checked
	 * @throws IOException
	 * @throws InvalidHeaderData
	 */
	public void load(boolean checkConsitency) throws IOException, InvalidHeaderData {
		logger.info("Loading " + this.toString() + " data");
		if (this.isLoad) {
			logger.info(this.toString() + " is already loaded.");
			return;
		}

		long begin = System.currentTimeMillis();

		this.dataLengthIndex = new int[maxEncodedSequenceValue + 1];
		this.dataOffsetIndex = new long[maxEncodedSequenceValue + 1];

		FileInputStream fileInputStream = new FileInputStream(getIndexFile());
		StoredSimilarSubSequencesIndex similarIndex = StoredSimilarSubSequencesIndex.parseFrom(fileInputStream);
		for (int i = 0; i < similarIndex.getStoredComparationResultInfosCount(); i++) {
			StoredComparationResultInfo storedComparationResultInfo = similarIndex.getStoredComparationResultInfos(i);
			int sequence = storedComparationResultInfo.getEncodedSubSequence();
			int length = storedComparationResultInfo.getLength();
			long offset = storedComparationResultInfo.getOffset();

			if (checkConsitency) {
				getSimilarSequences(sequence, length, offset);
			}

			if ((sequence) != i) {
				throw new InvalidHeaderData("Sequence (" + sequence
						+ ") count do not match sequence position (" + i + ").");
			}
			this.dataLengthIndex[sequence] = length;
			this.dataOffsetIndex[sequence] = offset;
		}
		this.isLoad = true;
		logger.info("SubSequencesComparer data loaded in " + (System.currentTimeMillis() - begin)
				+ "ms");
	}

	/**
	 * @param encodedSubSequence
	 *            an int that where is read <b>only</b> its first 16bits. If the
	 *            code has a short, use: <br>
	 *            <code>getSimilarSequences(shortVariable & 0xFFFF)</code>
	 * @return the similar subsequences that are equal or higher than threshold.
	 * @throws IOException
	 * @throws InvalidHeaderData
	 */
	public List<Integer> getSimilarSequences(int encodedSubSequence) throws IOException, InvalidHeaderData {
		long offset = dataOffsetIndex[encodedSubSequence];
		int length = dataLengthIndex[encodedSubSequence];

		return getSimilarSequences(encodedSubSequence, length, offset);
	}

	private List<Integer> getSimilarSequences(int encodedSequence, int length, long offset)
			throws IOException, InvalidHeaderData {

		MappedByteBuffer map = getDataFileChannel().map(MapMode.READ_ONLY, offset, length);
		byte[] data = new byte[length];
		map.get(data);		
		StoredSimilarSubSequences similarSubSequences = StoredSimilarSubSequences.parseFrom(data);

		if (similarSubSequences.getEncodedSequence() != encodedSequence) {
			throw new InvalidHeaderData("the value " + similarSubSequences.getEncodedSequence() + " is different from "
					+ encodedSequence);
		}

		return similarSubSequences.getSimilarSequenceList();
	}

	/**
	 * Same as generateData(false)
	 * 
	 * @throws IOException
	 * @throws IllegalSymbolException
	 * @throws BioException
	 */
	public void generateData() throws IOException, IllegalSymbolException, BioException {
		generateData(false);
	}

	/**
	 * Delete the data. Use with caution!
	 * 
	 * @return <code>true</code> if the data was deleted sucesfull.
	 */
	public boolean deleteData() {
		boolean r = true;
		if (getDataFile().delete() == false) {
			r = false;
		}
		if (getIndexFile().delete() == false) {
			r = false;
		}
		return r;
	}

	/**
	 * Calculate and store data for the actual score schema.
	 * 
	 * @param verbose
	 * @throws IOException
	 * @throws BioException
	 * @throws IllegalSymbolException
	 * @throws Exception
	 */
	public void generateData(boolean verbose) throws IOException, IllegalSymbolException,
			BioException {

		long initialTime = System.currentTimeMillis();

		if (getIndexFile().createNewFile() == false) {
			throw new IOException(
					"File "
							+ getIndexFile()
							+ " alread exists. Delete it before if you *really* want to generate a new data set");
		}

		if (getDataFile().createNewFile() == false) {
			throw new IOException(
					"File "
							+ getDataFile()
							+ " alread exists. Delete it before if you *really* want to generate a new data set");
		}

		FileChannel dataFileChannel = new FileOutputStream(getDataFile()).getChannel();
		bio.pih.io.proto.Io.StoredSimilarSubSequencesIndex.Builder indexInfoBuilder = StoredSimilarSubSequencesIndex.newBuilder();

		System.out.println("Gerating data for  " + this.toString());
		for (int encodedSequence1 = 0; encodedSequence1 <= maxEncodedSequenceValue; encodedSequence1++) {
			Builder similarSubSequencesBuilder = StoredSimilarSubSequences.newBuilder();
			similarSubSequencesBuilder.setEncodedSequence(encodedSequence1);

			SymbolList sequence = getEncoder().decodeIntegerToSymbolList(encodedSequence1);
			long time = System.currentTimeMillis();
			List<String> similarSequences = generateSimilar(sequence.seqString(), deeper, ALPHABET);
			for (String similar : similarSequences) {
				LightweightSymbolList similarSequence = (LightweightSymbolList) LightweightSymbolList.createDNA(similar);
				int encodedSimilar = getEncoder().encodeSubSymbolListToInteger(similarSequence);
				similarSubSequencesBuilder.addSimilarSequence(encodedSimilar);
			}

			StoredSimilarSubSequences storedSimilarSubSequences = similarSubSequencesBuilder.build();

			if (verbose & encodedSequence1 % 10000 == 0) {
				System.out.println(sequence.seqString() + "\t"
						+ (System.currentTimeMillis() - time) + "\t"
						+ storedSimilarSubSequences.getSimilarSequenceCount() + "\t"
						+ encodedSequence1 + "\t" + Integer.toHexString(encodedSequence1) + "\t"
						+ Integer.toBinaryString(encodedSequence1));
			}

			byte[] storedSimilarSubSequencesByteArray = storedSimilarSubSequences.toByteArray();

			StoredComparationResultInfo storedComparationResultInfo = StoredComparationResultInfo.newBuilder()
					.setEncodedSubSequence(encodedSequence1)
					.setLength(storedSimilarSubSequencesByteArray.length)
					.setOffset(dataFileChannel.position())
					.build();

			indexInfoBuilder.addStoredComparationResultInfos(storedComparationResultInfo);
			dataFileChannel.write(ByteBuffer.wrap(storedSimilarSubSequencesByteArray));
		}
		dataFileChannel.close();

		FileChannel indexFileChannel = new FileOutputStream(getIndexFile()).getChannel();
		indexFileChannel.write(ByteBuffer.wrap(indexInfoBuilder.build().toByteArray()));
		indexFileChannel.close();

		if (verbose) {
			System.out.println("-----------------------------");
			System.out.println(" total time: " + (System.currentTimeMillis() - initialTime));
			System.out.println("");
		}
	}

	private static List<String> generateSimilar(String sequence, int level, char[] alphabet) {
		LinkedList<String> result = Lists.newLinkedList();
		result.add(sequence);
		if (level == 0) {
			return result;
		}

		for (int i = 0; i < sequence.length(); i++) {
			for (char c : alphabet) {
				char[] newSequenceChar = sequence.toCharArray();
				if (newSequenceChar[i] != c) {
					newSequenceChar[i] = c;
					String newSequence = new String(newSequenceChar);
					result.add(newSequence);
					if (level > 1) {
						List<String> deeper = generateSimilar(newSequence, level - 1, alphabet);
						result.addAll(deeper);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Compare and align two sub-sequences
	 * 
	 * @param sequence1
	 * @param sequence2
	 * @return the alignment score
	 * @throws IllegalSymbolException
	 * @throws BioException
	 */
	public int compareCompactedSequences(SymbolList sequence1, SymbolList sequence2)
			throws IllegalSymbolException, BioException {
		return (int) aligner.fastPairwiseAlignment(sequence1, sequence2) * -1;
	}

	/**
	 * @return the default threshold
	 */
	public static int getDefaultTreadshould() {
		return defaultThreshold;
	}

	/**
	 * @return the default dismatch cost
	 */
	public static int getDefaultDismatch() {
		return defaultDismatch;
	}

	/**
	 * @return the default gap extend cost
	 */
	public static int getDefaultGapExtend() {
		return defaultGapExtend;
	}

	/**
	 * @return the default gap open cost
	 */
	public static int getDefaultGapOpen() {
		return defaultGapOpen;
	}

	/**
	 * @return the default match value
	 */
	public static int getDefaultMatch() {
		return defaultMatch;
	}

	/**
	 * @return the default sub-sequence length
	 */
	public static int getDefaultSubSequenceLength() {
		return defaultSubSequenceLength;
	}

	/**
	 * @return the aligner used
	 */
	public GenoogleSequenceAlignment getAligner() {
		return aligner;
	}

	/**
	 * @return the encoder used
	 */
	public DNASequenceEncoderToInteger getEncoder() {
		return encoder;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SubSequenceComparer ");
		sb.append(match);
		sb.append('/');
		sb.append(dismatch);
		sb.append('/');
		sb.append(gapOpen);
		sb.append('/');
		sb.append(gapExtend);
		sb.append('/');
		sb.append(threshold);
		return sb.toString();
	}
}