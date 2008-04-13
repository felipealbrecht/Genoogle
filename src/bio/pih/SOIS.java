package bio.pih;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.NoSuchElementException;

import org.apache.log4j.BasicConfigurator;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.DatabankCollection;
import bio.pih.io.DuplicateDatabankException;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.Output;
import bio.pih.search.SearchManager;
import bio.pih.search.SearchParams;
import bio.pih.search.UnknowDataBankException;
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
	 * @throws UnknowDataBankException
	 */
	public static void main(String[] args) throws IOException, NoSuchElementException, BioException, ValueOutOfBoundsException, InvalidHeaderData, DuplicateDatabankException, UnknowDataBankException {
		BasicConfigurator.configure();

		//IndexedDNASequenceDataBank frogDb = new IndexedDNASequenceDataBank("Frog", new File("frog.rna.fna"), false);
		IndexedDNASequenceDataBank cowDb = new IndexedDNASequenceDataBank("Cow", new File("cow.rna.fna"), false);

		DatabankCollection<IndexedDNASequenceDataBank> collection = new DatabankCollection<IndexedDNASequenceDataBank>("RefSeq", DNATools.getDNA(), new File("files/fasta"));
		collection.addDatabank(cowDb);
		//collection.addDatabank(frogDb);

		collection.loadInformations();

		String seq = "ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGTTTTTTTTTTTTTTTTTTTTTTTTCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGA";
//		String seq = "ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGA";
//		
//		char[] c = new char[seq.length()];
//		for (int i = seq.length()-1; i >= 0; i--) {
//			c[i] = seq.charAt(seq.length() - 1 - i);
//		}
//		seq = new String(c);
//		System.out.println(seq);
		
		LightweightSymbolList sequence = (LightweightSymbolList) LightweightSymbolList.createDNA(seq);

		SearchManager sm = new SearchManager();
		sm.addDatabank(collection);
		SearchParams sp = new SearchParams(sequence, "RefSeq");
		long beginTime = System.currentTimeMillis(); 
		long code = sm.doSearch(sp);

		while (!sm.checkSearch(code)) {
			Thread.yield();
		}

		Document document = Output.genoogleOutputToXML(sm.getResult(code));
		System.out.println("total time: " + (System.currentTimeMillis() - beginTime));

		OutputFormat outformat = OutputFormat.createPrettyPrint();
		outformat.setEncoding("UTF-8");
		XMLWriter writer = new XMLWriter(new FileOutputStream(new File("output.xml")), outformat);
		writer.write(document);
		writer.flush();

		// for (HSP alignment : sm.getResult(code)) {
		// GenoogleSmithWaterman smithWaterman = alignment.getAlignment();
		// String databankName = alignment.getDatabankName();
		// int queryOffset = alignment.getQueryOffset();
		// int targetOffset = alignment.getTargetOffset();
		// int sequenceId = alignment.getSequenceId();
		// String query = alignment.getQuery();
		//
		// try {
		// SequenceInformation si = collection.getSequenceInformationFromId(databankName, sequenceId);
		// String decodeShortArrayToString = DNASequenceEncoderToShort.getDefaultEncoder().decodeShortArrayToString(si.getEncodedSequence());
		// String formatOutput = GenoogleSequenceAlignment.formatOutput("query sequence", si.getDescription() + "@" + databankName, new String[] { smithWaterman.getQueryAligned(), smithWaterman.getTargetAligned() }, smithWaterman.getPath(), smithWaterman.getQueryStart(), smithWaterman.getQueryEnd(), query.length(), smithWaterman.getTargetStart(), smithWaterman.getTargetEnd(), decodeShortArrayToString.length(), smithWaterman.getEditDistance(), smithWaterman.getTime(), queryOffset, targetOffset);
		// System.out.println(formatOutput);
		// } catch (IllegalSymbolException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (MultipleSequencesFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

		// collection.loadInformations();

		// seq = "TTAGGAGTTCAGCATTAATTTCCAAAATTTTCATGGGGCTTGTGGCAACACGGGCCGTGAATCTGTGTATAAAATTTACTGGCCTTCTTCACTTACCTGCTCTAGTATCGTATCGTGTGTGCGTGCGTGTGTGACGTCAGGCTGCCACGTAAACTTCAGAGAAGAACCTTAAAGCAGACCATCCATTTTTGCATGCTCTCTTCTAAGTAGAATGTTCAATGTAACTAAAACTAAAATTGCATGTCAAAGAGACCTAGGTTCTTTCTTTCTTTCTTTCTCTCTTTCTTTCAGTTTGCTTTTGGTTTCCTGTATATTTGCTTACTGTGCTGTTCTAGTGGTTGT";
		// seq = "ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGATGAAAAAGTTTTGGAAATTGATAGTGGTAATGCAGCTCAACATTATGAATCTTTTTATAACTATGATGCACGGGGAGCGGATGAACTTTCTTTACAAATAGGAGACGCTGTGCACATCCTGGAAACATACGAAGGGTGGTACAGAGGTTACACCTTAAGAAAAAAGTCTAAGAAGGGTATATTTCCTGCTTCGTACATCCATCTTAAAGAAGCCATAGTTGAAGGAAAAGGGCAACATGA";

		// 40164
		// seq = "ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGA";
		/*
		 * seq = "ATGGACCC TGCCTGTA TCTATCCA TTCAGGAT CTTCTGTT GGTCACAG AAGGGCAG GCAGGGTG GGATGGGG TGGAGTGA
		 */
		/*
		 * ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGTTTTTTTTTTTTTTTTTTTTTTTTCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGA
		 */

		// seq = SequenceMutator.mutateSequence(seq, 100, 4);
		// sequence = (LightweightSymbolList) LightweightSymbolList.createDNA(seq);
		// search.doSearch(sequence, indexedDNASequenceDataBank);
		// search.doSearch(sequence, indexedDNASequenceDataBank);
		// search.doSearch(sequence, indexedDNASequenceDataBank);
	}
}
