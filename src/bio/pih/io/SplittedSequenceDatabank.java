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
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojavax.bio.seq.RichSequence;

import bio.pih.index.IndexConstructionException;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.proto.Io.StoredDatabank;
import bio.pih.io.proto.Io.StoredSequenceInfo;
import bio.pih.io.proto.Io.StoredDatabank.SequenceType;
import bio.pih.seq.op.LightweightIOTools;
import bio.pih.seq.op.LightweightStreamReader;

import com.google.common.collect.Lists;

/**
 * the divided sequence databank will receive 1..n diferents fasta files and a integer 1..m where m
 * is multiple of n. it will create m sub-databanks where all should have the most similar size
 * possible. By example: Databank alpha -> 100milions base Databank beta -> 200milions base Databank
 * gama -> 35milions base Databank delta -> 65milions base Databank zeta -> 300milions base
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
 * It is also important to pay attention that each sub sequence databak requires (4**10) * 16)
 * bytes, aprox. 20megabytes * of ram memory just to store the skeleton of the index, without any
 * data. It means if you create 10 databanks, to store a total of 200millions bases, you will use
 * aprox. 10 * (20 + 20) = 400 megabyte, while if you use 4, you will need 4 * (50 + 20) = 280
 * megabytes.
 * 
 * @author Pih
 */
public class SplittedSequenceDatabank extends DatabankCollection<IndexedDNASequenceDataBank> {

	private final int qtdSubBases;
	private final String mask;

	/**
	 * @param name
	 *            name of this databank
	 * @param path
	 *            directory where it will be
	 * @param subSequenceLength
	 * @param qtdSubBases
	 *            how many parts will have this sequence databank
	 * @param minEvalueDropOut
	 * @param mask
	 */
	public SplittedSequenceDatabank(String name, File path, int subSequenceLength, int qtdSubBases, String mask) {
		super(name, DNATools.getDNA(), subSequenceLength, path, null);
		this.qtdSubBases = qtdSubBases;
		this.mask = mask;
	}

	@Override
	public void encodeSequences() throws IOException, NoSuchElementException, BioException, ValueOutOfBoundsException,
			InvalidHeaderData, IndexConstructionException {

		List<FastaFileInfo> fastaFiles = Lists.newLinkedList();
		for (AbstractSequenceDataBank sequence : databanks.values()) {
			fastaFiles.add(new FastaFileInfo(sequence.getFullPath(true)));
		}

		long totalBasesCount = 0;
		for (FastaFileInfo fastaFileInfo : fastaFiles) {
			totalBasesCount += fastaFileInfo.getQtdBases();
		}
		sortFiles(fastaFiles);

		long totalBasesBySubBase = totalBasesCount / qtdSubBases;
		long subCount = 0;

		IndexedDNASequenceDataBank actualSequenceDatank = new IndexedDNASequenceDataBank("Sub_" + subCount, subSequenceLength, mask, new File(getSubDatabankName(subCount)), this);
		actualSequenceDatank.beginIndexBuild();
		int totalSequences = 0;
		long totalBases = 0;
		
		if (!getFilePath().exists()) {
			boolean mkdirs = getFullPath().mkdirs();
			if (!mkdirs) {
				logger.error(getFilePath() + " was not possible to create.");
			}
		}
		FileChannel dataBankFileChannel = new FileOutputStream(getDatabankFile(subCount)).getChannel();
		FileChannel storedSequenceInfoChannel = new FileOutputStream(getStoredDatabakFileName(subCount), true).getChannel();
		bio.pih.io.proto.Io.StoredDatabank.Builder storedDatabankBuilder = StoredDatabank.newBuilder();

		for (FastaFileInfo fastaFile : fastaFiles) {
			logger.info("Adding a FASTA file from " + fastaFile.getFastaFile());
			BufferedReader is = new BufferedReader(new FileReader(fastaFile.getFastaFile()));
			LightweightStreamReader readFastaDNA = LightweightIOTools.readFastaDNA(is, null);
			while (readFastaDNA.hasNext()) {
				RichSequence richSequence = readFastaDNA.nextRichSequence();
				StoredSequenceInfo addSequence = actualSequenceDatank.addSequence(richSequence, dataBankFileChannel);
				storedDatabankBuilder.addSequencesInfo(addSequence);
				totalSequences++;
				totalBases += richSequence.length();

				if (totalBases > totalBasesBySubBase) {
					actualSequenceDatank.endIndexBuild();
					finalizeSubDatabankConstruction(totalSequences, totalBases, dataBankFileChannel,
							storedSequenceInfoChannel, storedDatabankBuilder);
					subCount++;
					logger.info("Wrote " + subCount + " of " + qtdSubBases + " sub databanks.");
					totalSequences = 0;
					totalBases = 0;
					dataBankFileChannel = new FileOutputStream(getDatabankFile(subCount)).getChannel();
					storedSequenceInfoChannel = new FileOutputStream(getStoredDatabakFileName(subCount), true).getChannel();
					storedDatabankBuilder = StoredDatabank.newBuilder();
					actualSequenceDatank = new IndexedDNASequenceDataBank("Sub_" + subCount, subSequenceLength, mask, new File(getSubDatabankName(subCount)), this);
					actualSequenceDatank.beginIndexBuild();
				}
			}
		}
		actualSequenceDatank.endIndexBuild();
		finalizeSubDatabankConstruction(totalSequences, totalBases, dataBankFileChannel, storedSequenceInfoChannel,
				storedDatabankBuilder);
		logger.info("Wrote " + (subCount + 1) + " of " + qtdSubBases + " sub databanks.");
	}

	private File getStoredDatabakFileName(long subCount) {
		return new File(getFullPath(), getStoredDatabankFileName(subCount));
	}

	private File getDatabankFile(long subCount) {
		return new File(getFullPath(), getDatabankFileName(subCount));
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

	private void finalizeSubDatabankConstruction(int totalSequences, long totalBases, FileChannel dataBankFileChannel,
			FileChannel storedSequenceInfoChannel, StoredDatabank.Builder storedDatabankBuilder) throws IOException {
		StoredDatabank storedDatabank = buildStoredDatabank(totalSequences, totalBases, storedDatabankBuilder);
		storedSequenceInfoChannel.write(ByteBuffer.wrap(storedDatabank.toByteArray()));
		storedSequenceInfoChannel.close();
		dataBankFileChannel.close();
	}

	private StoredDatabank buildStoredDatabank(int totalSequences, long totalBases,
			StoredDatabank.Builder storedDatabankBuilder) {
		storedDatabankBuilder.setType(SequenceType.DNA);
		storedDatabankBuilder.setQtdSequences(totalSequences);
		storedDatabankBuilder.setQtdBases(totalBases);
		StoredDatabank storedDatabank = storedDatabankBuilder.build();
		return storedDatabank;
	}
	
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
			try {
				IndexedDNASequenceDataBank actualSequenceDatank = new IndexedDNASequenceDataBank("Sub_" + i, subSequenceLength, mask, new File(getSubDatabankName(i)), this);
				if (!actualSequenceDatank.check()) {
					return false;
				}
			} catch (Exception e) {
				logger.fatal(e);
				return false;
			}
		}
		return true;
	}	
	
	@Override
	public void delete() {
		for (int i = 0; i < qtdSubBases; i++) {
			try {
				IndexedDNASequenceDataBank actualSequenceDatank = new IndexedDNASequenceDataBank("Sub_" + i, subSequenceLength, mask, new File(getSubDatabankName(i)), this);
				actualSequenceDatank.delete();
			} catch (Exception e) {
				logger.fatal(e);
			}
		}
	}	


	@Override
	public void load() throws IOException, ValueOutOfBoundsException, InvalidHeaderData, IllegalSymbolException,
			BioException {
		logger.info("Loading internals databanks");
		long time = System.currentTimeMillis();
		this.clear();
		for (int i = 0; i < qtdSubBases; i++) {
			IndexedDNASequenceDataBank subDataBank = new IndexedDNASequenceDataBank(this.getName() + "_sub_" + i, subSequenceLength, mask, new File(getSubDatabankName(i)), this);
			subDataBank.load();
			try {
				this.addDatabank(subDataBank);
			} catch (DuplicateDatabankException e) {
				logger.info("Fatal error while loading sub databanks.", e);
			}
			logger.info("Loaded " + (i + 1) + " of " + qtdSubBases + " sub-databanks.");
		}
		logger.info("Databanks loaded in " + (System.currentTimeMillis() - time) + "ms.");
	}

	private class FastaFileInfo {
		File fastaFile;
		long qtdBases;
		long qtdSequences;

		public FastaFileInfo(File fastaFile) throws FileNotFoundException, NoSuchElementException, BioException {
			this.fastaFile = fastaFile;
			this.qtdBases = 0;
			this.qtdSequences = 0;

			BufferedReader is = new BufferedReader(new FileReader(fastaFile));
			LightweightStreamReader readFastaDNA = LightweightIOTools.readFastaDNA(is, null);
			logger.info("Reading informations from " + fastaFile);
			while (readFastaDNA.hasNext()) {
				RichSequence sequence = readFastaDNA.nextRichSequence();
				qtdBases += sequence.length();
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

		public File getFastaFile() {
			return fastaFile;
		}
	}
}
