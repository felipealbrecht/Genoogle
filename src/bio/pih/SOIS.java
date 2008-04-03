package bio.pih;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.log4j.BasicConfigurator;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalSymbolException;

import bio.pih.alignment.GenoogleSequenceAlignment;
import bio.pih.alignment.GenoogleSmithWaterman;
import bio.pih.encoder.DNASequenceEncoderToShort;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.DatabankCollection;
import bio.pih.io.DuplicateDatabankException;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.MultipleSequencesFoundException;
import bio.pih.io.SequenceInformation;
import bio.pih.search.AlignmentResult;
import bio.pih.search.SearchStatus;
import bio.pih.search.SearcherFactory;
import bio.pih.seq.LightweightSymbolList;

public class SOIS {

	/**
	 * @param args
	 * @throws IOException
	 * @throws NoSuchElementException
	 * @throws BioException
	 * @throws ValueOutOfBoundsException
	 * @throws InvalidHeaderData 
	 * @throws DuplicateDatabankException 
	 */
	public static void main(String[] args) throws IOException, NoSuchElementException, BioException, ValueOutOfBoundsException, InvalidHeaderData, DuplicateDatabankException {
		BasicConfigurator.configure();
		
		IndexedDNASequenceDataBank frogDb = new IndexedDNASequenceDataBank("Frog", new File("frog.rna.fna"), false);
		IndexedDNASequenceDataBank cowDb = new IndexedDNASequenceDataBank("Cow", new File("cow.rna.fna"), false);
		
		DatabankCollection<IndexedDNASequenceDataBank> collection = new DatabankCollection<IndexedDNASequenceDataBank>("RefSeq", DNATools.getDNA(), new File("files/fasta"));
		collection.addDatabank(cowDb);
		collection.addDatabank(frogDb);
		
		collection.loadInformations();
		
		//collection.encodeSequences();


		String seq =   "ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGTTTTTTTTTTTTTTTTTTTTTTTTCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGA";
		LightweightSymbolList sequence = (LightweightSymbolList) LightweightSymbolList.createDNA(seq);

		//frogDb.encodeSequences();
		SearchStatus searchStatus = SearcherFactory.getSearcher(collection).doSearch(sequence, collection);
		while (!searchStatus.isDone()) {
			Thread.yield();
		}
		
		for (AlignmentResult alignment : searchStatus.getResults()) {
			GenoogleSmithWaterman smithWaterman = alignment.getAlignment();
			String databankName = alignment.getDatabankName();
			int queryOffset = alignment.getQueryOffset();
			int targetOffset = alignment.getTargetOffset();
			int sequenceId = alignment.getSequenceId();
			String query = alignment.getQuery();

			try {
				SequenceInformation si = collection.getSequenceInformationFromId(databankName, sequenceId);
				String decodeShortArrayToString = DNASequenceEncoderToShort.getDefaultEncoder().decodeShortArrayToString(si.getEncodedSequence());
				String formatOutput = GenoogleSequenceAlignment.formatOutput("query sequence", si.getDescription() + "@" + databankName, new String[] { smithWaterman.getQueryAligned(), smithWaterman.getTargetAligned() }, smithWaterman.getPath(), smithWaterman.getQueryStart(), smithWaterman.getQueryEnd(), query.length(), smithWaterman.getTargetStart(), smithWaterman.getTargetEnd(), decodeShortArrayToString.length(), smithWaterman.getEditDistance(), smithWaterman.getTime(), queryOffset, targetOffset);
				System.out.println(formatOutput);
			} catch (IllegalSymbolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MultipleSequencesFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		//collection.loadInformations();

		//seq = "TTAGGAGTTCAGCATTAATTTCCAAAATTTTCATGGGGCTTGTGGCAACACGGGCCGTGAATCTGTGTATAAAATTTACTGGCCTTCTTCACTTACCTGCTCTAGTATCGTATCGTGTGTGCGTGCGTGTGTGACGTCAGGCTGCCACGTAAACTTCAGAGAAGAACCTTAAAGCAGACCATCCATTTTTGCATGCTCTCTTCTAAGTAGAATGTTCAATGTAACTAAAACTAAAATTGCATGTCAAAGAGACCTAGGTTCTTTCTTTCTTTCTTTCTCTCTTTCTTTCAGTTTGCTTTTGGTTTCCTGTATATTTGCTTACTGTGCTGTTCTAGTGGTTGT";		
		//seq = "ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGATGAAAAAGTTTTGGAAATTGATAGTGGTAATGCAGCTCAACATTATGAATCTTTTTATAACTATGATGCACGGGGAGCGGATGAACTTTCTTTACAAATAGGAGACGCTGTGCACATCCTGGAAACATACGAAGGGTGGTACAGAGGTTACACCTTAAGAAAAAAGTCTAAGAAGGGTATATTTCCTGCTTCGTACATCCATCTTAAAGAAGCCATAGTTGAAGGAAAAGGGCAACATGA";
		
		
		// 40164
		//seq = "ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGA";
		/*seq = "ATGGACCC        TGCCTGTA        TCTATCCA        TTCAGGAT        CTTCTGTT        
		 *               GGTCACAG        AAGGGCAG        GCAGGGTG        GGATGGGG        TGGAGTGA  */
		/*
                 ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGTTTTTTTTTTTTTTTTTTTTTTTTCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGA
		 */
		
//		seq = SequenceMutator.mutateSequence(seq, 100, 4);
//		sequence = (LightweightSymbolList) LightweightSymbolList.createDNA(seq);
//		search.doSearch(sequence, indexedDNASequenceDataBank);
		
//		search.doSearch(sequence, indexedDNASequenceDataBank);
//		search.doSearch(sequence, indexedDNASequenceDataBank);
	}
}
