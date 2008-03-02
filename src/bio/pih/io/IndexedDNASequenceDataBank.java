package bio.pih.io;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.BasicConfigurator;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SubSequencesArrayIndex;
import bio.pih.index.SubSequencesComparer;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.search.DNASearcher;
import bio.pih.seq.LightweightSymbolList;

import com.google.common.collect.Maps;

/**
 * A data bank witch index its sequences.
 * 
 * @author albrecht
 * 
 */
public class IndexedDNASequenceDataBank extends DNASequenceDataBank implements IndexedSequenceDataBank {

	private final SubSequencesComparer subSequenceComparer;
	private final SubSequencesArrayIndex index;

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
		
		//File fastaFile = new File("sequences_50mb.fsa");
		IndexedDNASequenceDataBank indexedDNASequenceDataBank = new IndexedDNASequenceDataBank("teste", new File("."), 8, false);
		indexedDNASequenceDataBank.loadInformations();
		//indexedDNASequenceDataBank.addFastaFile(fastaFile);
								
		String seq = "TTAGGAGTTCAGCATTAATTTCCAAAATTTTCATGGGGCTTGTGGCAACACGGGCCGTGAATCTGTGTATAAAATTTACTGGCCTTCTTCACTTACCTGCTCTAGTATCGTATCGTGTGTGCGTGCGTGTGTGACGTCAGGCTGCCACGTAAACTTCAGAGAAGAACCTTAAAGCAGACCATCCATTTTTGCATGCTCTCTTCTAAGTAGAATGTTCAATGTAACTAAAACTAAAATTGCATGTCAAAGAGACCTAGGTTCTTTCTTTCTTTCTTTCTCTCTTTCTTTCAGTTTGCTTTTGGTTTCCTGTATATTTGCTTACTGTGCTGTTCTAGTGGTTGT";
		LightweightSymbolList sequence = (LightweightSymbolList) LightweightSymbolList.createDNA(seq);
		
		DNASearcher search = new DNASearcher();
		long init = System.currentTimeMillis();
		search.doSearch(sequence, indexedDNASequenceDataBank);
		System.out.println("Total:" + (System.currentTimeMillis() - init));
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
		index.addSequence(sequenceInformation.getId(), sequenceInformation.getEncodedSequence());
		index.optime();
	}
	
	@Override
	void doSequenceLoadingProcessing(SequenceInformation sequenceInformation) {
		index.addSequence(sequenceInformation.getId(), sequenceInformation.getEncodedSequence());
	}
	
	@Override
	void doOptimizations() {
		index.optime();	
	}
			

	public int[] getMachingSubSequence(short encodedSubSequence) throws ValueOutOfBoundsException {
		return index.getMachingSubSequence(encodedSubSequence);
	}

	public Map<Short, int[]> getSimilarSubSequence(short encodedSubSequence, int threshold) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {
		Map<Short, int[]> similarSubSequences = Maps.newHashMap();
		
		int[] alignmentIntRepresentations = subSequenceComparer.getSimilarSequences(encodedSubSequence);
		short scoreFromIntRepresentation;
		short sequenceFromIntRepresentation;
		
		for (int alignmentIntRepresentation : alignmentIntRepresentations) {
			scoreFromIntRepresentation = SubSequencesComparer.getScoreFromIntRepresentation(alignmentIntRepresentation);
			if (scoreFromIntRepresentation < threshold) {
				break;
			}
			sequenceFromIntRepresentation = SubSequencesComparer.getSequenceFromIntRepresentation(alignmentIntRepresentation);
			int[] machingSubSequence = index.getMachingSubSequence(sequenceFromIntRepresentation);
			if (machingSubSequence != null) {
				similarSubSequences.put(sequenceFromIntRepresentation, machingSubSequence);
			}
		}

		return similarSubSequences;
	}
	
}
