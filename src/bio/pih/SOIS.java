package bio.pih;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
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

/**
 * Genoogle non distributed and no server implementation. For tests and validation propose.
 * 
 * 
 * @author albrecht
 */
public class SOIS {

	List<SequenceDataBank> dataBanks = null;
	SearchManager sm = null;

	static Logger logger = Logger.getLogger(SOIS.class.getName());

	/**
	 * Simple constructor.
	 */
	public SOIS() {
		PropertyConfigurator.configure("conf/log4j.properties");
	}

	/**
	 * Initialize SOIS.
	 * @throws IOException
	 * @throws ValueOutOfBoundsException
	 */
	public void init() throws IOException, ValueOutOfBoundsException {
		dataBanks = ConfigurationXMLReader.getDataBanks();
		sm = new SearchManager();

		for (SequenceDataBank dataBank : dataBanks) {
			dataBank.load();
			sm.addDatabank(dataBank);
		}
	}

	/**
	 * Do a search
	 * @param seqString
	 * @param dataBankName
	 * @return code of the search.
	 */
	public long doSearch(String seqString, String dataBankName) {
		long code = -1;
		try {
			SymbolList sequence = LightweightSymbolList.createDNA(seqString);
			SearchParams sp = new SearchParams(sequence, dataBankName);
			code = sm.doSearch(sp);
		} catch (UnknowDataBankException e) {
			logger.error(e);
		} catch (IllegalSymbolException e) {
			logger.error(e);
		}
		return code;
	}

	/**
	 * @param code
	 * @return <code>true</code> if the search is finished.
	 */
	public boolean checkStatus(long code) {
		return sm.checkSearch(code);
	}

	/**
	 * @param code
	 * @return
	 */
	public Document getResult(long code) {
		SearchResults result = sm.getResult(code);
		if (result == null) {
			return null;
		}
		Document document = Output.genoogleOutputToXML(result);
		return document;
	}

	/**
	 * Stand alone SOIS. For tests and validation propose.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws NoSuchElementException
	 * @throws BioException
	 * @throws ValueOutOfBoundsException
	 * @throws InvalidHeaderData
	 * @throws DuplicateDatabankException
	 * @throws UnknowDataBankException
	 * @throws ValueOutOfBoundsException
	 * @throws DocumentException
	 */
	public static void main(String[] args) throws IOException, NoSuchElementException, BioException, UnknowDataBankException, ValueOutOfBoundsException {		
		PropertyConfigurator.configure("conf/log4j.properties");		
		
		logger.info("SOIS - Search Over Indexed Sequences.");
		logger.info("Authors: Felipe Felipe Albrecht, Raquel Coelho Gomes Pinto and Claudia Justel.");
		logger.info("Contact at felioe.albrecht@gmail.com");

		if (args.length == 0) {
			showHelp();
			return;
		}		
		
		List<SequenceDataBank> dataBanks = ConfigurationXMLReader.getDataBanks();

		String option = args[0];

		if (option.equals("-g")) {
			logger.info("Searching for non encoded data banks.");

			for (SequenceDataBank dataBank : dataBanks) {
				if (!dataBank.check()) {
					System.out.println("Data bank " + dataBank.getName() + " is not encoded.");
					dataBank.encodeSequences();
				}
			}
			logger.info("All specified data banks are encoded. You can do yours searchs now.");
			return;
		}

		else if (option.equals("-s")) {
			logger.info("Initalizing SOIS for searchs.");
			SearchManager sm = new SearchManager();

			for (SequenceDataBank dataBank : dataBanks) {
				dataBank.load();
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
				logger.info("Searching " + seqString + " in " + databank);
				SymbolList sequence = LightweightSymbolList.createDNA(seqString);
				SearchParams sp = new SearchParams(sequence, databank);
				long code = sm.doSearch(sp);

				while (!sm.checkSearch(code)) {
					Thread.yield();
				}
				results.add(sm.getResult(code));
			}
			logger.info("total time: " + (System.currentTimeMillis() - beginTime));

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
		logger.info("Options for SOIS execution:");
		logger.info(" -g : encode all not encoded databanks specified at conf/genoogle.conf .");
		logger.info(" -s : do a search. Being the first argument the file name with the sequences that will be searched and the second argument is the data bank name which will be searched.");
		logger.info("      Example: -s ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGA SeqRef");
	}
}
