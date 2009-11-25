package bio.pih.genoogle.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;

import bio.pih.genoogle.encoder.DNASequenceEncoderToInteger;
import bio.pih.genoogle.index.IndexConstructionException;
import bio.pih.genoogle.index.InvalidHeaderData;
import bio.pih.genoogle.index.ValueOutOfBoundsException;

/**
 * This abstract class which specifies the ways to access a sequences data banks. The methods are
 * divided into 3 classes: general information like name and path, add a sequence file, a single
 * sequence or a collection of them and sync these data and for last, and some way the most
 * important, do searchs.
 * 
 * @author albrecht
 */
public abstract class AbstractSequenceDataBank {

	protected final String name;
	protected final FiniteAlphabet alphabet;
	protected final int subSequenceLength;
	protected final DNASequenceEncoderToInteger encoder;

	protected final File path;
	protected final DatabankCollection<? extends AbstractDNASequenceDataBank> parent;
	protected final int lowComplexityFilter;

	protected AbstractSequenceDataBank(String name, FiniteAlphabet alphabet, int subSequenceLength, File path,
			DatabankCollection<? extends AbstractDNASequenceDataBank> parent, int lowComplexityFilter) {
		this.name = name;
		this.alphabet = alphabet;
		this.subSequenceLength = subSequenceLength;
		this.lowComplexityFilter = lowComplexityFilter;
		this.encoder = DNASequenceEncoderToInteger.getEncoder(subSequenceLength);
		this.path = path;
		this.parent = parent;
	}

	/**
	 * The name is related with the files names too.
	 * 
	 * @return the name of this sequence bank.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the file name and directory where is this SequenceDataBank. It should used at run
	 *         time and not formating db time.
	 */
	public File getFilePath() {
		return getFilePath(false);
	}

	/**
	 * @param formating informs if it was called during formating db time.
	 * @return the file name and directory where is this SequenceDataBank.
	 */
	public File getFilePath(boolean formating) {
		return path;
	}

	/**
	 * @return the file name and directory where is this SequenceDataBank considering its parent. It
	 *         should used at run time and not formating db time.
	 */
	public File getFullPath() {
		return getFullPath(false);
	}

	/**
	 * @param formating informs if it was called during formating db time.
	 * @return the file name and directory where is this SequenceDataBank considering its parent.
	 */
	public File getFullPath(boolean formating) {
		if (getParent() == null) {
			return getFilePath(formating);
		} else {
			return new File(getParent().getFullPath(formating), this.getFilePath(formating).getPath());
		}
	}

	/**
	 * @return the number of sequences stored in this SequenceDataBank
	 */
	abstract public int getNumberOfSequences();

	/**
	 * @return the total number of sequences stored in this SequenceDataBank and all siblings. <b>To
	 *         calculate statistics, this value should be used</b>.
	 */
	abstract public long getTotalNumberOfSequences();

	/**
	 * @return the number of nucleotides (DNA) or amino acids (Protein) stored in this
	 *         SequenceDataBank.
	 */
	abstract public long getDataBaseSize();

	/**
	 * @return the number of bases stored in this SequenceDataBank and all siblings. <b>This value
	 *         should be used to calculate statistics, </b>.
	 */
	abstract public long getTotalDataBaseSize();

	/**
	 * @return the {@link FiniteAlphabet} of the sequences of this sequence bank.
	 */
	public FiniteAlphabet getAlphabet() {
		return alphabet;
	}

	/**
	 * Add a fasta formated sequence collection into the SequenceBank.
	 * 
	 * @param fastaFile
	 * @throws FileNotFoundException
	 * @throws NoSuchElementException
	 * @throws BioException
	 * @throws IOException
	 * @throws IndexConstructionException
	 */
	abstract public void addFastaFile(File fastaFile) throws FileNotFoundException, NoSuchElementException,
			BioException, IOException, IndexConstructionException;

	/**
	 * Load this sequence bank
	 * 
	 * @throws IOException
	 * @throws ValueOutOfBoundsException
	 * @throws InvalidHeaderData
	 * @throws BioException
	 * @throws IllegalSymbolException
	 */
	abstract public void load() throws IOException, ValueOutOfBoundsException, InvalidHeaderData,
			IllegalSymbolException, BioException;

	/**
	 * Encode the sequences into a computer legible mode
	 * 
	 * @throws IOException
	 * @throws BioException
	 * @throws NoSuchElementException
	 * @throws ValueOutOfBoundsException
	 * @throws InvalidHeaderData
	 * @throws IndexConstructionException
	 */
	abstract public void encodeSequences() throws IOException, NoSuchElementException, BioException,
			ValueOutOfBoundsException, InvalidHeaderData, IndexConstructionException;

	/**
	 * @return the parent of this {@link AbstractSequenceDataBank} or <code>null</code> if it do not
	 *         have parent
	 */
	protected AbstractSequenceDataBank getParent() {
		return parent;
	}
	
	public int getLowComplexityFilter() {
		return lowComplexityFilter;
	}

	/**
	 * @return <code>true</code> if the data bank files and its data are okay. This method do
	 *         <b>not</b> check file consistency.
	 */
	abstract public boolean check();

	/**
	 * @return {@link DNASequenceEncoderToInteger} witch is responsible to encode the sequences in
	 *         this data bank.
	 */
	public DNASequenceEncoderToInteger getEncoder() {
		return encoder;
	}

	/**
	 * @return length of the sub sequences stored in this data bank.
	 */
	public int getSubSequenceLength() {
		return subSequenceLength;
	}

	/**
	 * Delete all file informations of this data bank.
	 */
	abstract public void delete();

}
