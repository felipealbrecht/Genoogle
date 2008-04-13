package bio.pih.io;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.biojava.bio.seq.DNATools;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SubSequencesArrayIndex;
import bio.pih.index.SubSequencesComparer;
import bio.pih.index.ValueOutOfBoundsException;

/**
 * A data bank witch index its sequences.
 * 
 * @author albrecht
 * 
 */
public class IndexedDNASequenceDataBank extends DNASequenceDataBank implements IndexedSequenceDataBank {

	private static Logger logger = Logger.getLogger("pih.bio.io.IndexedDNASequenceDataBank");

	private final SubSequencesArrayIndex index;
	private static final SubSequencesComparer subSequenceComparer;

	static {
		subSequenceComparer = SubSequencesComparer.getDefaultInstance();
		try {
			subSequenceComparer.load();
		} catch (Exception e) {
			logger.fatal("Fatar error while loading default SubSequenceComparer.\n Pay attention if the files " + subSequenceComparer.getDataFileName() + " and " + subSequenceComparer.getIndexFileName() + " exists and are not corrupted.", e);
		}
	}

	/**
	 * Same as public IndexedDNASequenceDataBank(String name, File path, boolean isReadOnly) setting isReadOnly as false.
	 * 
	 * @param name
	 * @param path
	 * @throws ValueOutOfBoundsException
	 * @throws IOException
	 * @throws InvalidHeaderData
	 */
	public IndexedDNASequenceDataBank(String name, File path) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {
		this(name, path, false);
	}

	/**
	 * 
	 * @param name
	 *            the name of the data bank
	 * @param path
	 *            the path where the data bank is/will be stored
	 * @param isReadOnly
	 * @throws IOException
	 * @throws ValueOutOfBoundsException
	 * @throws InvalidHeaderData
	 */
	public IndexedDNASequenceDataBank(String name, File path, boolean isReadOnly) throws IOException, ValueOutOfBoundsException, InvalidHeaderData {
		super(name, path, isReadOnly);
		index = new SubSequencesArrayIndex(8, DNATools.getDNA());
	}

	@Override
	void doSequenceAddingProcessing(SequenceInformation sequenceInformation) {
		index.addSequence(sequenceInformation.getId(), sequenceInformation.getEncodedSequence());
	}

	@Override
	void doSequenceLoadingProcessing(SequenceInformation sequenceInformation) {
		index.addSequence(sequenceInformation.getId(), sequenceInformation.getEncodedSequence());
	}

	public int[] getMachingSubSequence(short encodedSubSequence) throws ValueOutOfBoundsException {
		return index.getMachingSubSequence(encodedSubSequence);
	}

	public int[] getSimilarSubSequence(short encodedSubSequence) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {
		return subSequenceComparer.getSimilarSequences(encodedSubSequence);
	}

}
