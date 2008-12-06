package bio.pih.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;

import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;

/**
 * @author albrecht
 * 
 * @param <T>
 *            data bank type
 */
public class DatabankCollection<T extends SequenceDataBank> implements SequenceDataBank {

	Logger logger = Logger.getLogger("bio.pih.io.DataBankCollection");

	protected String name;
	protected final FiniteAlphabet alphabet;
	protected final LinkedHashMap<String, T> collection;
	protected final File path;
	protected final int subSequenceLength;
	private final DNASequenceEncoderToInteger encoder;

	private SequenceDataBank parent;

	/**
	 * @param name
	 * @param alphabet
	 * @param path
	 * @param parent
	 * @param subSequenceLength 
	 * @param maxThreads
	 * @param minEvalueDropOut 
	 */
	public DatabankCollection(String name, FiniteAlphabet alphabet, File path,
			SequenceDataBank parent, int subSequenceLength) {
		this.name = name;
		this.alphabet = alphabet;
		this.path = path;
		this.parent = parent;
		this.subSequenceLength = subSequenceLength;
		this.encoder = DNASequenceEncoderToInteger.getEncoder(subSequenceLength);
		this.collection = new LinkedHashMap<String, T>();
	}

	/**
	 * Add a new databank in the collection;
	 * 
	 * @param databank
	 * @throws DuplicateDatabankException
	 */
	public void addDatabank(T databank) throws DuplicateDatabankException {
		if (this.collection.containsKey(databank.getName())) {
			throw new DuplicateDatabankException(databank.getName(), this.getName());
		}
		this.collection.put(databank.getName(), databank);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public FiniteAlphabet getAlphabet() {
		return alphabet;
	}

	/**
	 * @return quantity of data banks in this collection.
	 */
	public int size() {
		return this.collection.size();
	}

	/**
	 * Check if a data bank is in this data bank collection.
	 * 
	 * @param name
	 * @return <code>true</code> if the data bank is in this data bank
	 *         collection.
	 */
	public boolean containsDatabank(String name) {
		return this.collection.containsKey(name);
	}

	/**
	 * Retrieve a data bank from this collection.
	 * 
	 * @param name
	 * @return data bank retrieved.
	 */
	public T getDatabank(String name) {
		return this.collection.get(name);
	}

	/**
	 * @return {@link Iterator} that iterate over all data banks of this
	 *         collection.
	 */
	public Iterator<T> databanksIterator() {
		return this.collection.values().iterator();
	}

	/**
	 * Remove all data banks of this collection.
	 */
	public void clear() {
		this.collection.clear();
	}

	/**
	 * Check if this data bank collection is empty.
	 * 
	 * @return <code>true</code> if this data bank collection is empty.
	 */
	public boolean isEmpty() {
		return this.collection.isEmpty();
	}

	/**
	 * Remove a data bank from this collection.
	 * 
	 * @param name
	 * @return the removed data bank.
	 */
	public T removeDatabank(String name) {
		return this.collection.remove(name);
	}

	@Override
	public void addFastaFile(File fastaFile) throws FileNotFoundException, NoSuchElementException,
			BioException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public File getFilePath() {
		return path;
	}

	@Override
	public int getNumberOfSequences() {
		int total = 0;

		Iterator<T> iterator = this.collection.values().iterator();
		while (iterator.hasNext()) {
			total += iterator.next().getNumberOfSequences();
		}
		return total;
	}

	@Override
	public void load() throws IOException, ValueOutOfBoundsException, InvalidHeaderData, IllegalSymbolException, BioException {
		logger.info("Loading internals databanks");
		long time = System.currentTimeMillis();
		Iterator<T> iterator = this.collection.values().iterator();
		while (iterator.hasNext()) {
			iterator.next().load();
		}
		logger.info("Databanks loaded in " + (System.currentTimeMillis() - time) + "ms,");
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setPath(File directory) {
		throw new UnsupportedOperationException("The path is imutable for this class");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<T> iterator = this.collection.values().iterator();
		sb.append("Databank Collection: ");
		sb.append(this.getName());
		sb.append("[");
		while (iterator.hasNext()) {
			sb.append(" ");
			sb.append(iterator.next().toString());
			sb.append(" ");
		}
		sb.append("]");

		return sb.toString();
	}

	File fullPath = null;

	@Override
	public File getFullPath() {
		if (fullPath == null) {
			if (getParent() == null) {
				fullPath = getFilePath();
			} else {
				fullPath = new File(getParent().getFullPath(), this.getFilePath().getPath());
			}
		}
		return fullPath;
	}

	@Override
	public SequenceDataBank getParent() {
		return parent;
	}

	@Override
	public void encodeSequences() throws IOException, NoSuchElementException, BioException,
			ValueOutOfBoundsException, InvalidHeaderData {
		logger.info("Encoding internals databanks");
		long time = System.currentTimeMillis();
		Iterator<T> iterator = this.collection.values().iterator();
		while (iterator.hasNext()) {
			T next = iterator.next();
			if (!next.check()) {
				next.encodeSequences();
			}
		}
		logger.info("Databanks encoded in " + (System.currentTimeMillis() - time));
	}

	@Override
	public boolean check() {
		Iterator<T> iterator = this.collection.values().iterator();
		while (iterator.hasNext()) {
			T next = iterator.next();
			if (!next.check()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public DNASequenceEncoderToInteger getEncoder() {
		return encoder;
	}

	@Override
	public long getDataBaseSize() {
		return getTotalDataBaseSize();
	}

	long totalDataBaseSize = -1;

	@Override
	public long getTotalDataBaseSize() {
		if (totalDataBaseSize == -1) {
			synchronized (this) {
				long total = 0;
				for (SequenceDataBank dataBank : collection.values()) {
					total += dataBank.getDataBaseSize();
				}
				this.totalDataBaseSize = total;
			}
		}
		return totalDataBaseSize;
	}

	long totalNumberOfSequences = -1;

	@Override
	public long getTotalNumberOfSequences() {
		if (totalNumberOfSequences == -1) {
			synchronized (this) {
				long total = 0;
				for (SequenceDataBank dataBank : collection.values()) {
					total += dataBank.getNumberOfSequences();
				}
				this.totalNumberOfSequences = total;
			}
		}
		return totalNumberOfSequences;
	}
	
	@Override
	public int getSubSequenceLength() {
		return subSequenceLength;
	}
}