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
import org.biojavax.bio.seq.RichSequence;

/**
 * @author albrecht
 * 
 * @param <T>
 *            data bank type
 */
public class DatabankCollection<T extends SequenceDataBank> implements SequenceDataBank {

	Logger logger = Logger.getLogger("bio.pih.io.DataBankCollection");

	private String name;

	private final FiniteAlphabet alphabet;
	private final LinkedHashMap<String, T> collection;
	private final File path;

	private SequenceDataBank parent;

	/**
	 * @param name
	 * @param alphabet
	 * @param path
	 */
	public DatabankCollection(String name, FiniteAlphabet alphabet, File path) {
		this.name = name;
		this.alphabet = alphabet;
		this.path = path;
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
		databank.setParent(this);
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
	 * @return <code>true</code> if the data bank is in this data bank collection.
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
	 * @return {@link Iterator} that iterate over all data banks of this collection.
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
	 * @return <code>true</code> if this data bank collection is empty.
	 */
	public boolean isEmpty() {
		return this.collection.isEmpty();
	}

	/**
	 * Remove a data bank from this collection.
	 * @param name
	 * @return the removed data bank.
	 */
	public T removeDatabank(String name) {
		return this.collection.remove(name);
	}

	@Override
	public void addFastaFile(File fastaFile) throws FileNotFoundException, NoSuchElementException, BioException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int addSequence(RichSequence s) throws BioException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public File getPath() {
		return path;
	}
	
	/**
	 * Load a {@link SequenceInformation} from a specified {@link SequenceDataBank}.
	 * @param databankName 
	 * @param sequenceId
	 * @return SequenceInformation from the given sequenceId or <code>null</code> if this sequenceId was not found.
	 * @throws IllegalSymbolException
	 * @throws IOException
	 * @throws MultipleSequencesFoundException
	 */
	public SequenceInformation getSequenceInformationFromId(String databankName, int sequenceId) throws IllegalSymbolException, IOException, MultipleSequencesFoundException {
		T t = collection.get(databankName);
		if (t == null) {
			return null;
		}
		return t.getSequenceInformationFromId(sequenceId);		
	}

	@Override
	public SequenceInformation getSequenceInformationFromId(int sequenceId) throws IOException, IllegalSymbolException, MultipleSequencesFoundException {
		SequenceInformation foundSi = null;
		String databankFound = null;

		Iterator<T> iterator = this.collection.values().iterator();
		while (iterator.hasNext()) {
			T next = iterator.next();
			SequenceInformation si = next.getSequenceInformationFromId(sequenceId);

			if (si != null) {
				if (foundSi != null) {
					throw new MultipleSequencesFoundException(sequenceId, next.getName(), databankFound);
				}
				databankFound = next.getName();
				foundSi = next.getSequenceInformationFromId(sequenceId);
			}
		}
		return foundSi;
	}

	@Override
	public int getTotalSequences() {
		int total = 0;

		Iterator<T> iterator = this.collection.values().iterator();
		while (iterator.hasNext()) {
			total += iterator.next().getTotalSequences();
		}
		return total;
	}

	@Override
	public void load() throws IOException {
		logger.info("Loading internals databanks");
		long time = System.currentTimeMillis();
		Iterator<T> iterator = this.collection.values().iterator();
		while (iterator.hasNext()) {
			iterator.next().load();
		}
		logger.info("Databanks loaded in " + (System.currentTimeMillis() - time));
	}

	@Override
	public void setAlphabet(FiniteAlphabet alphabet) {
		throw new UnsupportedOperationException("The alphabet is imutable for this class");
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

	@Override
	public File getFullPath() {
		return this.getPath();
	}

	@Override
	public SequenceDataBank getParent() {
		return parent;
	}
	
	@Override
	public void setParent(SequenceDataBank parent) {
		this.parent = parent;
	}

	@Override
	public void encodeSequences() throws IOException, NoSuchElementException, BioException {
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

}
