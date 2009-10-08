package bio.pih;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

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
import bio.pih.search.SearchParams.Parameter;
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
	 * @throws InvalidHeaderData 
	 * @throws BioException 
	 * @throws IllegalSymbolException 
	 */
	public synchronized static SOIS getInstance() throws InvalidHeaderData, IllegalSymbolException, BioException {
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
	 * @throws InvalidHeaderData 
	 * @throws BioException 
	 * @throws IllegalSymbolException 
	 */
	private SOIS() throws IOException, ValueOutOfBoundsException, InvalidHeaderData, IllegalSymbolException, BioException {
		PropertyConfigurator.configure("conf/log4j.properties");
		sm = XMLConfigurationReader.getSearchManager();
	}
	
	/**
	 * @param in
	 * @param databank
	 * @return {@link List} of {@link SearchResults} of the given queries.
	 * @throws IOException
	 * @throws IllegalSymbolException
	 * @throws UnknowDataBankException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public List<SearchResults> doBatchSyncSearch(BufferedReader in)
			throws IOException, IllegalSymbolException, UnknowDataBankException,
			InterruptedException, ExecutionException {
		String defaultDataBankName = sm.getDefaultDataBankName();
		return doBatchSyncSearch(in, defaultDataBankName);
	}
	
	public Collection<SequenceDataBank> getDatabanks() {
		return sm.getDatabanks();
	}
	
	public String getDefaultDatabank() {
		return sm.getDefaultDataBankName();
	}

	public List<SearchResults> doBatchSyncSearch(BufferedReader in, String databank) throws IllegalSymbolException, IOException, UnknowDataBankException, InterruptedException, ExecutionException {
		return doBatchSyncSearch(in, databank, null);
	}
	
	/**
	 * @param in
	 * @param databank
	 * @return {@link List} of {@link SearchResults} of the given queries.
	 * @throws IOException
	 * @throws IllegalSymbolException
	 * @throws UnknowDataBankException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public List<SearchResults> doBatchSyncSearch(BufferedReader in, String databank, Map<Parameter, Object> parameters)
			throws IOException, IllegalSymbolException, UnknowDataBankException,
			InterruptedException, ExecutionException {

		List<SearchParams> batch = Lists.newLinkedList();
		while (in.ready()) {
			String seqString = in.readLine();
			seqString = seqString.trim();
			if (seqString.length() == 0) {
				continue;
			}
			SymbolList sequence = LightweightSymbolList.createDNA(seqString);
			SearchParams sp;
			if (parameters == null) {
				sp = new SearchParams(sequence, databank);
			} else {
				sp = new SearchParams(sequence, databank, parameters);
			}
			batch.add(sp);
		}
		return sm.doSyncSearch(batch);
	}

	/**
	 * @param seqString
	 * @return {@link SearchResults} of the search using the default databank.
	 */	
	public SearchResults doSyncSearch(String seqString) {
		String defaultDataBankName = sm.getDefaultDataBankName();
		return doSyncSearch(seqString, defaultDataBankName);
	}
	
	
	/**
	 * @param seqString
	 * @param dataBankName
	 * @return {@link SearchResults} of the search
	 */
	public SearchResults doSyncSearch(String seqString, String dataBankName) {
		SearchResults sr = null;
		seqString = seqString.trim();
		try {
			SymbolList sequence = LightweightSymbolList.createDNA(seqString);
			SearchParams sp = new SearchParams(sequence, dataBankName);
			sr = sm.doSyncSearch(sp);
		} catch (UnknowDataBankException e) {
			logger.error(e.getMessage(), e);
		} catch (IllegalSymbolException e) {
			logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		} catch (ExecutionException e) {
			logger.error(e.getMessage(), e);			
		}

		return sr;
	}

	/**
	 * Finish {@link SOIS}.
	 * 
	 * @throws InterruptedException
	 */
	public void shutdown() throws InterruptedException {
		sm.shutdown();
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
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws InvalidHeaderData 
	 * @throws DocumentException
	 */
	public static void main(String[] args) throws IOException, NoSuchElementException,
			BioException, UnknowDataBankException, ValueOutOfBoundsException, TransformerException,
			InterruptedException, ExecutionException, InvalidHeaderData {
		PropertyConfigurator.configure("conf/log4j.properties");
		logger.info("SOIS - Search Over Indexed Sequences.");
		logger.info("Authors: Felipe Felipe Albrecht, Raquel Coelho Gomes Pinto and Claudia Justel.");
		logger.info("Contact at felioe.albrecht@gmail.com");

		if (args.length == 0) {
			showHelp();
			return;
		}

		List<SequenceDataBank> dataBanks = XMLConfigurationReader.getDataBanks();

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
			SOIS sois = new SOIS();
			logger.info("Initalizing SOIS for searchs.");

			String inputFile = args[1];
			String databank = args[2];

			File f = new File(inputFile);
			BufferedReader in = new BufferedReader(new FileReader(f));
			long beginTime = System.currentTimeMillis();

			List<SearchResults> results = sois.doBatchSyncSearch(in, databank);
			sois.shutdown();

			boolean hasError = false;
			for (SearchResults result : results) {
				if (result.hasFail()) {
					hasError = true;
					for (Exception e : result.getFails()) {
						logger.fatal("Fail while doing searching process", e);
					}
				}
			}
			if (hasError) {
				System.out.println("The seach processing had some errors.");
				return;
			}

			logger.info("total time: " + (System.currentTimeMillis() - beginTime));

			Document document = Output.genoogleOutputToXML(results);
			OutputFormat outformat = OutputFormat.createPrettyPrint();
			outformat.setTrimText(false);
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
		logger.info(" -g : encode all not encoded databanks specified at conf/genoogle.conf .");
		logger.info(" -s : do a search. Being the first argument the file name with the sequences that will be searched and the second argument is the data bank name which will be searched.");
		logger.info("      Example: -s ATGGACCCGGTCACAGTGCCTGTAAAGGGCAGTCTATCCAGCAGGGTGTTCAGGATGGATGGGGCTTCTGTTTGGAGTGA SeqRef");
	}
}