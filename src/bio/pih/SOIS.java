package bio.pih;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.transform.TransformerException;

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
import bio.pih.io.DuplicateDatabankException;
import bio.pih.io.Output;
import bio.pih.io.SequenceDataBank;
import bio.pih.io.XMLConfigurationReader;
import bio.pih.search.SearchManager;
import bio.pih.search.SearchParams;
import bio.pih.search.UnknowDataBankException;
import bio.pih.search.results.SearchResults;
import bio.pih.seq.LightweightSymbolList;

import com.google.common.collect.Lists;

/**
 * Genoogle non distributed and no server implementation. For tests and
 * validation propose.
 * 
 * 
 * @author albrecht
 */
public class SOIS {
	SearchManager sm = null;

	private static SOIS singleton = null;

	static Logger logger = Logger.getLogger(SOIS.class.getName());

	/**
	 * @return SOIS Singleton instance.
	 */
	public static SOIS getInstance() {
		if (singleton == null) {
			try {
				singleton = new SOIS();
			} catch (IOException e) {
				logger.fatal(e.getMessage());
			} catch (ValueOutOfBoundsException e) {
				logger.fatal(e.getMessage());
			}
		}
		return singleton;
	}

	/**
	 * Simple constructor.
	 * 
	 * @throws ValueOutOfBoundsException
	 * @throws IOException
	 */
	private SOIS() throws IOException, ValueOutOfBoundsException {
		PropertyConfigurator.configure("conf/log4j.properties");
		sm = XMLConfigurationReader.getSearchManager();
	}

	/**
	 * Do a search
	 * 
	 * @param seqString
	 * @param dataBankName
	 * @return code of the search.
	 */
	public long doSearch(String seqString, String dataBankName) {
		long code = -1;
		seqString = seqString.trim();
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
	 * @return {@link Document} containing the results of the search.
	 */
	public SearchResults getResult(long code) {
		SearchResults result = sm.getResult(code);
		if (result == null) {
			return null;
		}
		return result;
		// Document document = Output.genoogleOutputToXML(result);
		// return document;
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
	 * @throws TransformerException 
	 * @throws DocumentException
	 */
	public static void main(String[] args) throws IOException,
			NoSuchElementException, BioException, UnknowDataBankException,
			ValueOutOfBoundsException, TransformerException {
		PropertyConfigurator.configure("conf/log4j.properties");

		logger.info("SOIS - Search Over Indexed Sequences.");
		logger
				.info("Authors: Felipe Felipe Albrecht, Raquel Coelho Gomes Pinto and Claudia Justel.");
		logger.info("Contact at felioe.albrecht@gmail.com");

		if (args.length == 0) {
			showHelp();
			return;
		}

		List<SequenceDataBank> dataBanks = XMLConfigurationReader
				.getDataBanks();

		String option = args[0];

		if (option.equals("-g")) {
			logger.info("Searching for non encoded data banks.");

			for (SequenceDataBank dataBank : dataBanks) {
				if (!dataBank.check()) {
					System.out.println("Data bank " + dataBank.getName()
							+ " is not encoded.");
					dataBank.encodeSequences();
				}
			}
			logger
					.info("All specified data banks are encoded. You can do yours searchs now.");
			return;
		}

		else if (option.equals("-s")) {
			logger.info("Initalizing SOIS for searchs.");

			String inputFile = args[1];
			String databank = args[2];

			File f = new File(inputFile);
			BufferedReader in = new BufferedReader(new FileReader(f));

			SOIS sois = SOIS.getInstance();

			List<SearchResults> results = Lists.newLinkedList();

			List<Long> codes = Lists.newLinkedList();
			long beginTime = System.currentTimeMillis();
			
			while (in.ready()) {
				String seqString = in.readLine();
				long code = sois.doSearch(seqString, databank);
				codes.add(code);
			}
			                       
			Collections.sort(codes);
			
			while (sois.sm.hasPeding()) {
				Thread.yield();
			}
			
			boolean hasError = false;
			for (Long code: codes) {
				SearchResults result = sois.getResult(code);
				if (result.hasFail()) {
					hasError = true;
					for(Exception e: result.getFails()) {
						logger.fatal("Fail while doing searching process", e); 						
					}
				}
				results.add(result);
			}
			if (hasError) {
				System.out.println("The seach processing had some errors.");
				return;
			}
			
			logger.info("total time: "
					+ (System.currentTimeMillis() - beginTime));

			Document document = Output.genoogleOutputToXML(results);
			 OutputFormat outformat = OutputFormat.createPrettyPrint();
			outformat.setEncoding("UTF-8");
			XMLWriter writer = new XMLWriter(new FileOutputStream(new File(inputFile
					+ "_results.xml")), outformat);
			writer.write(document);
			writer.flush();
			
	
		} else {
			showHelp();
		}
	}

	private static void showHelp() {
		logger.info("Options for SOIS execution:");
		logger
				.info(" -g : encode all not encoded databanks specified at conf/genoogle.conf .");
		logger
				.info(" -s : do a search. Being the first argument the file name with the sequences that will be searched and the second argument is the data bank name which will be searched.");
		logger
				.info("      Example: -s ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGA SeqRef");
	}
}
