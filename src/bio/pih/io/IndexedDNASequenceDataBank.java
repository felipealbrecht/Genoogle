package bio.pih.io;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;

import bio.pih.search.SearchInformation;
import bio.pih.search.SearchParams;

/**
 * A data bank witch index its sequences.
 *  
 * @author albrecht
 * 
 */
public class IndexedDNASequenceDataBank extends DNASequenceDataBank {

	private int subSequenceLength;
	private final String[] extensions = new String[] {"ddbi"}; // Dna Data Bank Index
	private final String[] allExtension;
	
	private final File indexFile;
	
	public static void main(String[] args) throws IOException, NoSuchElementException, BioException {
		File file = new File("sequences_50mb.fsa");
		IndexedDNASequenceDataBank indexedDNASequenceDataBank = new IndexedDNASequenceDataBank("teste", new File("."), 8, false);
		indexedDNASequenceDataBank.loadInformations();
		//indexedDNASequenceDataBank.addFastaFile(file);
	}

	/**
	 * 
	 * @param name the name of the data bank
	 * @param path the path where the data bank is/will be stored
	 * @param subSequenceLenth the length of the sub sequences for the indexing propose. 
	 * @param readOnly 
	 * @throws IOException 
	 */
	public IndexedDNASequenceDataBank(String name, File path, int subSequenceLenth, boolean readOnly) throws IOException {
		super(name, path, readOnly);
		this.subSequenceLength = subSequenceLenth;
		this.allExtension = new String[super.getExtensions().length + extensions.length];
		System.arraycopy(super.getExtensions(), 0, allExtension, 0, super.getExtensions().length);
		System.arraycopy(extensions, 0, allExtension, super.getExtensions().length, extensions.length);
		
		indexFile = new File(path, name + ".ddbi");
		checkFile(indexFile, readOnly);						
	}
	
	public void addSequenceColection(Collection<Sequence> sequences) {
		// TODO Auto-generated method stub

	}

	public SearchInformation requestSearch(Sequence input, SearchParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	public void sync() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public String[] getExtensions() {
		return extensions;
	}
	
}
