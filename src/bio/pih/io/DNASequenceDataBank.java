package bio.pih.io;

import java.io.File;
import java.io.IOException;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.FiniteAlphabet;

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

	private final File dataBankFile;

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
	public DNASequenceDataBank(String name, File path, boolean readOnly) throws IOException {
		this.name = name;
		this.path = path;
		this.readOnly = readOnly;

		dataBankFile = new File(path, name);
		checkFile(dataBankFile, readOnly);
	}

	private final String[] extensions = new String[] { "dsdb" }; // Dna Sequences Data Bank

	@Override
	public void setAlphabet(FiniteAlphabet alphabet) {
		throw new IllegalStateException("The alphabet is imutable for this class");
	}

	@Override
	public FiniteAlphabet getAlphabet() {
		return alphabet;
	}

	@Override
	public void setExtensions(String[] extensions) {
		throw new IllegalStateException("The extension name is imutable for this class");
	}

	@Override
	public String[] getExtensions() {
		return extensions;
	}

	@Override
	public void setName(String name) {
		throw new IllegalStateException("The name is imutable for a DataBank");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setPath(File directory) {
		throw new IllegalStateException("The path is imutable for a DataBank");
	}

	@Override
	public File getPath() {
		return path;
	}

	protected File getDataBankFile() {
		return dataBankFile;
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

}
