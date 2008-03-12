package bio.pih.io;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SubSequencesArrayIndex;
import bio.pih.index.SubSequencesComparer;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.search.DNASearcher;
import bio.pih.seq.LightweightSymbolList;

/**
 * A data bank witch index its sequences.
 * 
 * @author albrecht
 * 
 */
public class IndexedDNASequenceDataBank extends DNASequenceDataBank implements IndexedSequenceDataBank {

	private final SubSequencesComparer subSequenceComparer;
	private final SubSequencesArrayIndex index;
	
	Logger logger = Logger.getLogger("pih.bio.io.IndexedDNASequenceDataBank");

	/**
	 * @param args
	 * @throws IOException
	 * @throws NoSuchElementException
	 * @throws BioException
	 * @throws ValueOutOfBoundsException
	 * @throws InvalidHeaderData 
	 */
	public static void main(String[] args) throws IOException, NoSuchElementException, BioException, ValueOutOfBoundsException, InvalidHeaderData {
		BasicConfigurator.configure();
		
		//IndexedDNASequenceDataBank indexedDNASequenceDataBank = new IndexedDNASequenceDataBank("full_rna", new File("."), 8, false);
		IndexedDNASequenceDataBank indexedDNASequenceDataBank = new IndexedDNASequenceDataBank("mouse_cow", new File("."), 8, false);
		indexedDNASequenceDataBank.loadInformations();
		
//		indexedDNASequenceDataBank.addFastaFile(new File("files/fasta/full_rna.fna"));
//		indexedDNASequenceDataBank.addFastaFile(new File("cow.rna.fna"));
		
		String seq;

		//seq = "TTAGGAGTTCAGCATTAATTTCCAAAATTTTCATGGGGCTTGTGGCAACACGGGCCGTGAATCTGTGTATAAAATTTACTGGCCTTCTTCACTTACCTGCTCTAGTATCGTATCGTGTGTGCGTGCGTGTGTGACGTCAGGCTGCCACGTAAACTTCAGAGAAGAACCTTAAAGCAGACCATCCATTTTTGCATGCTCTCTTCTAAGTAGAATGTTCAATGTAACTAAAACTAAAATTGCATGTCAAAGAGACCTAGGTTCTTTCTTTCTTTCTTTCTCTCTTTCTTTCAGTTTGCTTTTGGTTTCCTGTATATTTGCTTACTGTGCTGTTCTAGTGGTTGT";
		
		seq = "ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGATGAAAAAGTTTTGGAAATTGATAGTGGTAATGCAGCTCAACATTATGAATCTTTTTATAACTATGATGCACGGGGAGCGGATGAACTTTCTTTACAAATAGGAGACGCTGTGCACATCCTGGAAACATACGAAGGGTGGTACAGAGGTTACACCTTAAGAAAAAAGTCTAAGAAGGGTATATTTCCTGCTTCGTACATCCATCTTAAAGAAGCCATAGTTGAAGGAAAAGGGCAACATGA";
		LightweightSymbolList sequence = (LightweightSymbolList) LightweightSymbolList.createDNA(seq);
		
		DNASearcher search = new DNASearcher();
		long init = System.currentTimeMillis();
		search.doSearch(sequence, indexedDNASequenceDataBank);
//		search.doSearch(sequence, indexedDNASequenceDataBank);
//		search.doSearch(sequence, indexedDNASequenceDataBank);
	}

	/**
	 * 
	 * @param name
	 *            the name of the data bank
	 * @param path
	 *            the path where the data bank is/will be stored
	 * @param subSequenceLenth
	 *            the length of the sub sequences for the indexing propose.
	 * @param readOnly
	 * @throws IOException
	 * @throws ValueOutOfBoundsException
	 * @throws InvalidHeaderData 
	 */
	public IndexedDNASequenceDataBank(String name, File path, int subSequenceLenth, boolean readOnly) throws IOException, ValueOutOfBoundsException, InvalidHeaderData {
		super(name, path, readOnly);

		index = new SubSequencesArrayIndex(subSequenceLenth, DNATools.getDNA());

		subSequenceComparer = SubSequencesComparer.getDefaultInstance();
		subSequenceComparer.load();
	}
	
	@Override
	void doSequenceAddingProcessing(SequenceInformation sequenceInformation) {
		short[] encodedSequence = sequenceInformation.getEncodedSequence();
		int id = sequenceInformation.getId();
		index.addSequence(id, encodedSequence);		
	}
	
	@Override
	void doSequenceLoadingProcessing(SequenceInformation sequenceInformation) {
		index.addSequence(sequenceInformation.getId(), sequenceInformation.getEncodedSequence());
	}
	
	@Override
	void doOptimizations() {		
		index.optimize();	
	}
			

	public long[] getMachingSubSequence(short encodedSubSequence) throws ValueOutOfBoundsException {
		return index.getMachingSubSequence(encodedSubSequence);
	}
	
	public int[] getSimilarSubSequence(short encodedSubSequence) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {		
		return subSequenceComparer.getSimilarSequences(encodedSubSequence);		
	}
	
}
