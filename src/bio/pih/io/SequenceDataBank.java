package bio.pih.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.biojavax.bio.seq.RichSequence;

import bio.pih.search.SearchInformation;
import bio.pih.search.SearchParams;

/**
 * This interface specifies the ways to access a sequence bank.
 * <p>
 * SequenceBank is how the Genoogle stores its sequences data.
 * The methods are divided into 3 classes: general information like name and path,
 * add a sequence file, a single sequence or a collection of them and sync these data
 * and for last, and some way the most important, do searchs. 
 * @author albrecht
 *
 */
public interface SequenceDataBank {
		
	/**
	 * The name is related with the files names too.
	 * @return the name of this sequence bank.
	 */
	String getName();
	
	/**
	 * @param name set the name of this sequence bank.
	 */
	void setName(String name);

	/**
	 * @return the directory where is this SequenceBank.
	 */
	File getPath();
	
	/**
	 * @param directory where is this SequenceBank.
	 */
	void setPath(File directory);
		
	/**
	 * @return the extensions that are related with this SequenceBank.
	 */
	String[] getExtensions();
	
	
	/**
	 * @param extensions for the files of this SequenceDataBank
	 */
	void setExtensions(String[] extensions);
	
	
	/**
	 * Set the {@link FiniteAlphabet} of the sequences of this sequence bank.
	 * @param alphabet
	 */
	void setAlphabet(FiniteAlphabet alphabet);
	
	
	/**
	 * @return the {@link FiniteAlphabet} of the sequences of this sequence bank.
	 */
	FiniteAlphabet getAlphabet();
	
	/**
	 * Add a fasta formated sequence collection into the SequenceBank.
	 * @param fastaFile
	 * @throws FileNotFoundException 
	 * @throws NoSuchElementException 
	 * @throws BioException 
	 * @throws IOException 
	 */
	public void addFastaFile(File fastaFile) throws FileNotFoundException, NoSuchElementException, BioException, IOException;
		
	/**
	 * Load this sequence bank
	 * @throws IOException 
	 * @throws IllegalSymbolException 
	 */
	void loadInformations() throws IOException, IllegalSymbolException;
	
	/**
	 * Synchronize the informations of this sequence bank into disk
	 */
	void sync(); 
	
	/**
	 * Add a new sequence into the sequence bank 
	 * @param s 
	 * @param sequence
	 * @throws BioException 
	 * @throws IOException 
	 */
	public void addSequence(RichSequence s) throws BioException, IOException;
	
	/**
	 * Add a new sequence collection into the sequence bank.
	 * @param sequences
	 */
	void addSequenceColection(Collection<Sequence> sequences);
	
	/**
	 * Get a {@link SymbolList} sequence from a GI
	 * @param gi
	 * @return the symbol list of the given gi
	 * @throws IOException 
	 * @throws IllegalSymbolException 
	 */
	SymbolList getSymbolListFromGi(String gi) throws IOException, IllegalSymbolException;
	
	/**
	 * Request a search in this SequenceBank.
	 * @param input
	 * @param params
	 * @return {@link SearchInformation} of this search.
	 */
	SearchInformation requestSearch(Sequence input, SearchParams params);	
}
