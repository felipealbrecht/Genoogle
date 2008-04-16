package bio.pih;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.BasicConfigurator;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.SymbolList;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.ConfigurationXMLReader;
import bio.pih.io.DuplicateDatabankException;
import bio.pih.io.Output;
import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchManager;
import bio.pih.search.SearchParams;
import bio.pih.search.UnknowDataBankException;
import bio.pih.search.results.SearchResults;
import bio.pih.seq.LightweightSymbolList;

import com.google.common.collect.Lists;

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
	 * @throws DocumentException
	 */
	public static void main(String[] args) throws IOException, NoSuchElementException, BioException, UnknowDataBankException, DocumentException {
		System.out.println("SOIS - Search Over Indexed Sequences.");
		System.out.println("Authors: Felipe Felipe Albrecht, Raquel Coelho Gomes Pinto and Claudia Justel.");
		System.out.println("Contact at felioe.albrecht@gmail.com");
		
		BasicConfigurator.configure();

		
		List<SequenceDataBank> dataBanks = ConfigurationXMLReader.getDataBanks();

		if (args.length == 0) {
			showHelp();
			return;
		}
		
		String option = args[0];
		
		if (option.equals("-g")) {
			System.out.println("Searching for non encoded data banks.");

			for (SequenceDataBank dataBank : dataBanks) {
				if (!dataBank.check()) {
					System.out.println("Data bank " + dataBank.getName() + " is not encoded.");
					dataBank.encodeSequences();
				}
			}
			System.out.println("All specified data banks are encoded. You can do yours searchs now.");
			return;
		}

		else if (option.equals("-s")) {
			System.out.println("Initalizing SOIS for searchs.");
			SearchManager sm = new SearchManager();
			
			for (SequenceDataBank dataBank : dataBanks) {
				dataBank.loadInformations();
				sm.addDatabank(dataBank);
			}
			String inputFile = args[1];
			String databank = args[2];

			File f = new File(inputFile);
			BufferedReader in = new BufferedReader(new FileReader(f));

			List<SearchResults> results = Lists.newLinkedList();

			long beginTime = System.currentTimeMillis();
			while (in.ready()) {
				String seqString = in.readLine();
				System.out.println("Searching " + seqString + " in " + databank);
				SymbolList sequence = LightweightSymbolList.createDNA(seqString);
				SearchParams sp = new SearchParams(sequence, databank);
				long code = sm.doSearch(sp);

				while (!sm.checkSearch(code)) {
					Thread.yield();
				}
				results.add(sm.getResult(code));
			}
			System.out.println("total time: " + (System.currentTimeMillis() - beginTime));

			Document document = Output.genoogleOutputToXML(results);
			OutputFormat outformat = OutputFormat.createPrettyPrint();
			outformat.setEncoding("UTF-8");
			XMLWriter writer = new XMLWriter(new FileOutputStream(new File(inputFile + "_results.xml")), outformat);
			writer.write(document);
			writer.flush();
		} else {
			showHelp();
		}
	}

	private static void showHelp() {
		System.out.println("Options for SOIS execution:");
		System.out.println(" -g : encode all not encoded databanks specified at conf/genoogle.conf .");
		System.out.println(" -s : do a search. Being the first argument the file name with the sequences that will be searched and the second argument is the data bank name which will be searched.");
		System.out.println("      Example: -s ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGA SeqRef");
	}
}
