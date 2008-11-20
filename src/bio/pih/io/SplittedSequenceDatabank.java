package bio.pih.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojavax.bio.seq.RichSequence;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.IndexedSequenceDataBank.StorageKind;
import bio.pih.io.proto.Io.StoredDatabank;
import bio.pih.io.proto.Io.StoredSequenceInfo;
import bio.pih.io.proto.Io.StoredDatabank.Builder;
import bio.pih.io.proto.Io.StoredDatabank.SequenceType;
import bio.pih.seq.op.LightweightIOTools;
import bio.pih.seq.op.LightweightStreamReader;

import com.google.common.collect.Lists;

/**
 * the divided sequence databank will receive 1..n diferents fasta files and a
 * integer 1..m where m is multiple of n. it will create m sub-databanks where
 * all should have the most similar size possible. By example: Databank alpha ->
 * 100milions base Databank beta -> 200milions base Databank gama -> 35milions
 * base Databank delta -> 65milions base Databank zeta -> 300milions base
 * 
 * n = 5
 * 
 * if m is 1: one databank with 700milions bases.
 * 
 * if m is 10: ten databanks with 70 milions bases each.
 * 
 * A high value of m is good for paralelism and is recomended a valus of 2 *
 * ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
 * 
 * It is also important to pay attention that each sub sequence databak requires
 * (4**10) * 16) bytes, aprox. 20megabytes * of ram memory just to store the
 * skeleton of the index, without any data. It means if you create 10 databanks,
 * to store a total of 200millions bases, you will use aprox. 10 * (20 + 20) =
 * 400 megabyte, while if you use 4, you will need 4 * (50 + 20) = 280
 * megabytes.
 * 
 * @author Pih
 */
public class SplittedSequenceDatabank extends DatabankCollection<IndexedDNASequenceDataBank> {

	private final int qtdSubBases;

	/**
	 * @param name name of this databank
	 * @param path directory where it will be
	 * @param subSequenceLength 
	 * @param qtdSubBases how many parts will have this sequence databank
	 * @param maxThreads number of maximum simultaneous threads
	 * @param minEvalueDropOut 
	 */
	public SplittedSequenceDatabank(String name, File path, int subSequenceLength, int qtdSubBases, int maxThreads) {
		super(name, DNATools.getDNA(), path, null, subSequenceLength, maxThreads);
		this.qtdSubBases = qtdSubBases;
	}

	@Override
	public void encodeSequences() throws IOException, NoSuchElementException, BioException,
			ValueOutOfBoundsException, InvalidHeaderData {

		List<FastaFileInfo> fastaFiles = Lists.newLinkedList();
		for (SequenceDataBank sequence : collection.values()) {
			fastaFiles.add(new FastaFileInfo(sequence.getFullPath()));
		}

		long totalBasesCount = 0;
		for (FastaFileInfo fastaFileInfo : fastaFiles) {
			totalBasesCount += fastaFileInfo.getQtdBases();
		}
		sortFiles(fastaFiles);

		long totalBasesBySubBase = totalBasesCount / qtdSubBases;
		long subCount = 0;

		IndexedDNASequenceDataBank actualSequenceDatank = new IndexedDNASequenceDataBank("Sub_"
				+ subCount, path, this, StorageKind.MEMORY, subSequenceLength);
		long totalSequences = 0;
		long totalBases = 0;
		FileChannel dataBankFileChannel = new FileOutputStream(getDatabankFile(subCount)).getChannel();
		FileChannel storedSequenceInfoChannel = new FileOutputStream(getStoredDatabakFileName(subCount), true).getChannel();
		bio.pih.io.proto.Io.StoredDatabank.Builder storedDatabankBuilder = StoredDatabank.newBuilder();

		for (FastaFileInfo fastaFile : fastaFiles) {
			BufferedReader is = new BufferedReader(new FileReader(fastaFile.getFastaFile()));
			LightweightStreamReader readFastaDNA = LightweightIOTools.readFastaDNA(is, null);
			while (readFastaDNA.hasNext()) {
				RichSequence richSequence = readFastaDNA.nextRichSequence();
				StoredSequenceInfo addSequence = actualSequenceDatank.addSequence(richSequence,
						dataBankFileChannel);
				storedDatabankBuilder.addSequencesInfo(addSequence);
				totalSequences++;
				totalBases += richSequence.length();

				if (totalBases > totalBasesBySubBase) {
					finalizeSubDatabankConstruction(totalSequences, totalBases,
							dataBankFileChannel, storedSequenceInfoChannel, storedDatabankBuilder);
					subCount++;
					totalSequences = 0;
					totalBases = 0;
					dataBankFileChannel = new FileOutputStream(getDatabankFile(subCount)).getChannel();
					storedSequenceInfoChannel = new FileOutputStream(getStoredDatabakFileName(subCount), true).getChannel();
					storedDatabankBuilder = StoredDatabank.newBuilder();
					actualSequenceDatank = new IndexedDNASequenceDataBank("Sub_"
							+ subCount, path, this, StorageKind.MEMORY, subSequenceLength);
				}
			}
		}
		finalizeSubDatabankConstruction(totalSequences, totalBases, dataBankFileChannel,
				storedSequenceInfoChannel, storedDatabankBuilder);
	}

	private File getStoredDatabakFileName(long subCount) {
		return new File(getFullPath(),
				getStoredDatabankFileName(subCount));
	}

	private File getDatabankFile(long subCount) {
		return new File(getFullPath(),
				getDatabankFileName(subCount));
	}

	private String getStoredDatabankFileName(long subCount) {
		return getSubDatabankName(subCount) + ".ssdb";
	}

	private String getDatabankFileName(long subCount) {
		return getSubDatabankName(subCount) + ".dsdb";
	}
	
	private String getSubDatabankName(long subCount) {
		return this.getName() + "_sub_" + subCount;
	}

	private void finalizeSubDatabankConstruction(long totalSequences, long totalBases,
			FileChannel dataBankFileChannel, FileChannel storedSequenceInfoChannel,
			bio.pih.io.proto.Io.StoredDatabank.Builder storedDatabankBuilder) throws IOException {
		StoredDatabank storedDatabank = buildStoredDatabank(totalSequences, totalBases,
				storedDatabankBuilder);
		storedSequenceInfoChannel.write(ByteBuffer.wrap(storedDatabank.toByteArray()));
		storedSequenceInfoChannel.close();
		dataBankFileChannel.close();
	}

	private StoredDatabank buildStoredDatabank(long totalSequences, long totalBases,
			bio.pih.io.proto.Io.StoredDatabank.Builder storedDatabankBuilder) {
		storedDatabankBuilder.setType(SequenceType.DNA);
		storedDatabankBuilder.setQtdSequences(totalSequences);
		storedDatabankBuilder.setQtdBases(totalBases);
		StoredDatabank storedDatabank = storedDatabankBuilder.build();
		return storedDatabank;
	}

	List<StoredDatabank> subDatabanks = Lists.newLinkedList();
	Builder storedDataBuilder = StoredDatabank.newBuilder();

	private void sortFiles(List<FastaFileInfo> fastaFiles) {
		Collections.sort(fastaFiles, new Comparator<FastaFileInfo>() {
			@Override
			public int compare(final FastaFileInfo o1, final FastaFileInfo o2) {
				final long diff = o1.getQtdBases() - o2.getQtdBases();
				if (diff > 0l) {
					return 1;
				} else if (diff < 0) {
					return -1;
				}
				return 0;
			}
		});
	}
	
	@Override
	public boolean check() {
		for (int i = 0; i < qtdSubBases; i++) {
			if (!getStoredDatabakFileName(i).exists() || getDatabankFile(i).exists()) {			
				return false;
			}
		}
		return true;
	}

	@Override
	public void load() throws IOException, ValueOutOfBoundsException, InvalidHeaderData {
		logger.info("Loading internals databanks");
		long time = System.currentTimeMillis();
		this.clear();
		for (int i = 0; i < qtdSubBases; i++) {
			IndexedDNASequenceDataBank subDataBank = new IndexedDNASequenceDataBank(this.getName() + "_sub_" + i, new File(getSubDatabankName(i)), this, StorageKind.MEMORY, subSequenceLength);
			subDataBank.load();
			try {
				this.addDatabank(subDataBank);
			} catch (DuplicateDatabankException e) {
				logger.info("Fatal error while loading sub databanks.", e);
			}			
			logger.info("Loaded " + (i+1) + " of " + qtdSubBases + " sub-databanks.");
		}
		logger.info("Databanks loaded in " + (System.currentTimeMillis() - time) + "ms.");
	}

	private class FastaFileInfo {
		File fastaFile;
		long qtdBases;
		long qtdSequences;

		public FastaFileInfo(File fastaFile) throws FileNotFoundException, NoSuchElementException,
				BioException {
			this.fastaFile = fastaFile;
			this.qtdBases = 0;
			this.qtdSequences = 0;

			BufferedReader is = new BufferedReader(new FileReader(fastaFile));
			LightweightStreamReader readFastaDNA = LightweightIOTools.readFastaDNA(is, null);
			System.out.println("Reading informations from " + fastaFile);
			while (readFastaDNA.hasNext()) {
				RichSequence sequence = readFastaDNA.nextRichSequence();
				qtdBases += sequence.length();
				if (qtdSequences % 10000 == 0) {
					System.out.println(qtdSequences);
				}
				qtdSequences++;
			}
		}
		
		@Override
		public String toString() {
			return this.fastaFile.toString();
		}

		public long getQtdBases() {
			return qtdBases;
		}

		public long getQtdSequences() {
			return qtdSequences;
		}

		public File getFastaFile() {
			return fastaFile;
		}
	}
}
