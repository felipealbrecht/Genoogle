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

import bio.pih.index.IndexConstructionException;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;

/**
 * @author albrecht
 * 
 * @param <T>
 *            data bank type
 */
public class DatabankCollection<T extends AbstractSequenceDataBank> extends AbstractSequenceDataBank {

	Logger logger = Logger.getLogger("bio.pih.io.DataBankCollection");
	
	protected final LinkedHashMap<String, T> databanks;

	/**
	 * @param name
	 * @param alphabet
	 * @param path
	 * @param parent
	 * @param subSequenceLength 
	 * @param maxThreads
	 * @param minEvalueDropOut 
	 */
	public DatabankCollection(String name, FiniteAlphabet alphabet, int subSequenceLength, File path,
			DatabankCollection<? extends AbstractDNASequenceDataBank> parent) {
		super(name, alphabet, subSequenceLength, path, parent);
		this.databanks = new LinkedHashMap<String, T>();
	}

	/**
	 * Add a new databank in the collection;
	 * 
	 * @param databank
	 * @throws DuplicateDatabankException
	 */
	public void addDatabank(T databank) throws DuplicateDatabankException {
		if (this.databanks.containsKey(databank.getName())) {
			throw new DuplicateDatabankException(databank.getName(), this.getName());
		}
		this.databanks.put(databank.getName(), databank);
	}

	/**
	 * @return quantity of data banks in this collection.
	 */
	public int size() {
		return this.databanks.size();
	}

	/**
	 * Check if a data bank is in this data bank collection.
	 * 
	 * @param name
	 * @return <code>true</code> if the data bank is in this data bank
	 *         collection.
	 */
	public boolean containsDatabank(String name) {
		return this.databanks.containsKey(name);
	}

	/**
	 * Retrieve a data bank from this collection.
	 * 
	 * @param name
	 * @return data bank retrieved.
	 */
	public T getDatabank(String name) {
		return this.databanks.get(name);
	}

	/**
	 * @return {@link Iterator} that iterate over all data banks of this
	 *         collection.
	 */
	public Iterator<T> databanksIterator() {
		return this.databanks.values().iterator();
	}

	/**
	 * Remove all data banks of this collection.
	 */
	public void clear() {
		this.databanks.clear();
	}

	/**
	 * Check if this data bank collection is empty.
	 * 
	 * @return <code>true</code> if this data bank collection is empty.
	 */
	public boolean isEmpty() {
		return this.databanks.isEmpty();
	}

	/**
	 * Remove a data bank from this collection.
	 * 
	 * @param name
	 * @return the removed data bank.
	 */
	public T removeDatabank(String name) {
		return this.databanks.remove(name);
	}

	@Override
	public void addFastaFile(File fastaFile) throws FileNotFoundException, NoSuchElementException,
			BioException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public File getFilePath(boolean formating) {
		if (formating) {
			return path;
		}
		return new File(path, name);
	}

	@Override
	public int getNumberOfSequences() {
		int total = 0;

		Iterator<T> iterator = this.databanks.values().iterator();
		while (iterator.hasNext()) {
			total += iterator.next().getNumberOfSequences();
		}
		return total;
	}

	@Override
	public void load() throws IOException, ValueOutOfBoundsException, InvalidHeaderData, IllegalSymbolException, BioException {
		logger.info("Loading internals databanks");
		long time = System.currentTimeMillis();
		Iterator<T> iterator = this.databanks.values().iterator();
		while (iterator.hasNext()) {
			iterator.next().load();
		}
		logger.info("Databanks loaded in " + (System.currentTimeMillis() - time) + "ms,");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<T> iterator = this.databanks.values().iterator();
		sb.append("Databank Collection: ");
		sb.append(this.getName());
		sb.append(" [");
		while (iterator.hasNext()) {
			sb.append(iterator.next().toString());
		}
		sb.append("]");

		return sb.toString();
	}
	
	@Override
	public void encodeSequences() throws IOException, NoSuchElementException, BioException,
			ValueOutOfBoundsException, InvalidHeaderData, IndexConstructionException {
		logger.info("Encoding internals databanks");
		long time = System.currentTimeMillis();
		Iterator<T> iterator = this.databanks.values().iterator();
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
		Iterator<T> iterator = this.databanks.values().iterator();
		while (iterator.hasNext()) {
			T next = iterator.next();
			if (!next.check()) {
				return false;
			}
		}
		return true;
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
				for (AbstractSequenceDataBank dataBank : databanks.values()) {
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
				for (AbstractSequenceDataBank dataBank : databanks.values()) {
					total += dataBank.getNumberOfSequences();
				}
				this.totalNumberOfSequences = total;
			}
		}
		return totalNumberOfSequences;
	}

	@Override
	public void delete() {
		for (AbstractSequenceDataBank dataBank: databanks.values()) {
			dataBank.delete();			
		}		
	}
}