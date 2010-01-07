/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;

import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.encoder.SequenceEncoderFactory;
import bio.pih.genoogle.index.IndexConstructionException;
import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.io.reader.ParseException;
import bio.pih.genoogle.seq.Alphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;

/**
 * This abstract class which specifies the ways to access a sequences data banks. The methods are
 * divided into 3 classes: general information like name and path, add a sequence file, a single
 * sequence or a collection of them and sync these data and for last, and some way the most
 * important, do searches.
 * 
 * @author albrecht
 */
public abstract class AbstractSequenceDataBank {

	protected final String name;
	protected final Alphabet alphabet;
	protected final int subSequenceLength;
	protected final SequenceEncoder encoder;

	protected final File path;
	protected final DatabankCollection<? extends AbstractSimpleSequenceDataBank> parent;
	protected final int lowComplexityFilter;

	protected AbstractSequenceDataBank(String name, Alphabet alphabet, int subSequenceLength, File path,
			DatabankCollection<? extends AbstractSimpleSequenceDataBank> parent, int lowComplexityFilter) {
		this.name = name;
		this.alphabet = alphabet;
		this.subSequenceLength = subSequenceLength;
		this.lowComplexityFilter = lowComplexityFilter;
		this.encoder = SequenceEncoderFactory.getEncoder(alphabet, subSequenceLength);
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
	 * @param formating
	 *            informs if it was called during formating db time.
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
	 * @param formating
	 *            informs if it was called during formating db time.
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
	 * @return the {@link Alphabet} of the sequences of this sequence bank.
	 */
	public Alphabet getAlphabet() {
		return alphabet;
	}

	/**
	 * Add a fasta formated sequence collection into the SequenceBank.
	 * 
	 * @param fastaFile
	 *            file which contains the sequences
	 * @param forceFormatting
	 *            <code>true</code> if it should continue the formatting process even some sequence
	 *            has invalid character. This sequences will be ignored.
	 */
	abstract public void addFastaFile(File fastaFile, boolean forceFormatting) throws FileNotFoundException,
			NoSuchElementException, IOException, IndexConstructionException, ParseException, IllegalSymbolException;

	/**
	 * Load this sequence bank
	 * 
	 * @return <true> if the data bank was loaded correctly, or <code>false</code> otherwise.
	 */
	abstract public boolean load() throws IOException, ValueOutOfBoundsException;

	/**
	 * Encode the sequences into a computer legible mode
	 * 
	 * @param forceFormatting
	 *            continues if some sequence had invalid character. This invalid sequence will be
	 *            ignored.
	 */
	abstract public void encodeSequences(boolean forceFormatting) throws IOException, NoSuchElementException,
			ValueOutOfBoundsException, IndexConstructionException, ParseException, IllegalSymbolException;

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
	public SequenceEncoder getEncoder() {
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
