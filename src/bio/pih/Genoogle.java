package bio.pih;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import com.google.common.collect.Lists;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.interfaces.Console;
import bio.pih.io.AbstractSequenceDataBank;
import bio.pih.io.InvalidConfigurationException;
import bio.pih.io.SequencesProvider;
import bio.pih.io.XMLConfigurationReader;
import bio.pih.search.SearchManager;
import bio.pih.search.SearchParams;
import bio.pih.search.UnknowDataBankException;
import bio.pih.search.SearchParams.Parameter;
import bio.pih.search.results.SearchResults;
import bio.pih.seq.LightweightSymbolList;

/**
 * Genoogle non distributed and no server implementation. For tests and validation propose.
 * 
 * 
 * @author albrecht
 */
public class Genoogle {

	public static String SOFTWARE_NAME = "Genoogle BETA";
	public static Double VERSION = 0.72;
	public static String COPYRIGHT_NOTICE = "Felipe Albrecht (felipe.albrecht@gmail.com) - 2009.";
	public static String WEB_PAGE = "http://genoogle.pih.bio.br";

	SearchManager sm = null;
	private static Genoogle singleton = null;

	static Logger logger = Logger.getLogger(Genoogle.class.getName());

	/**
	 * @return {@link Genoogle} Singleton instance.
	 * @throws InvalidHeaderData
	 * @throws BioException
	 * @throws IllegalSymbolException
	 * @throws InvalidConfigurationException
	 */
	public synchronized static Genoogle getInstance() throws InvalidHeaderData, IllegalSymbolException, BioException,
			InvalidConfigurationException {
		if (singleton == null) {
			logger.info("Starting Genoogle .");
			try {
				singleton = new Genoogle();
			} catch (IOException e) {
				logger.fatal(e.getMessage());
				return null;
			} catch (ValueOutOfBoundsException e) {
				logger.fatal(e.getMessage());
				return null;
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
	 * @throws InvalidConfigurationException
	 */
	private Genoogle() throws IOException, ValueOutOfBoundsException, InvalidHeaderData, IllegalSymbolException,
			BioException, InvalidConfigurationException {
		PropertyConfigurator.configure("conf/log4j.properties");
		sm = XMLConfigurationReader.getSearchManager();
	}

	
	// To stop the current searchs too?
	List<GenoogleListener> listerners = Lists.newLinkedList();

	public void addListerner(GenoogleListener listerner) {
		listerners.add(listerner);		
	}
	
	public void finish() {
		for (GenoogleListener listerner: listerners) {
			listerner.finish();
		}		
	}
	
	/**
	 * @param in
	 * @param databank
	 * @return {@link List} of {@link SearchResults} of the given queries.
	 * @throws IOException
	 * @throws UnknowDataBankException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws BioException
	 * @throws NoSuchElementException
	 */
	public List<SearchResults> doBatchSyncSearch(BufferedReader in) throws IOException, UnknowDataBankException,
			InterruptedException, ExecutionException, NoSuchElementException, BioException {
		String defaultDataBankName = sm.getDefaultDataBankName();
		return doBatchSyncSearch(in, defaultDataBankName);
	}

	public Collection<AbstractSequenceDataBank> getDatabanks() {
		return sm.getDatabanks();
	}

	public String getDefaultDatabank() {
		return sm.getDefaultDataBankName();
	}

	public List<SearchResults> doBatchSyncSearch(BufferedReader in, String databank) throws IOException,
			UnknowDataBankException, InterruptedException, ExecutionException, NoSuchElementException, BioException {
		return doBatchSyncSearch(in, databank, null);
	}

	/**
	 * @param in
	 * @param databank
	 * @return {@link List} of {@link SearchResults} of the given queries.
	 * @throws IOException
	 * @throws UnknowDataBankException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws BioException
	 * @throws NoSuchElementException
	 */
	public List<SearchResults> doBatchSyncSearch(BufferedReader in, String databank, Map<Parameter, Object> parameters)
			throws IOException, UnknowDataBankException, InterruptedException, ExecutionException,
			NoSuchElementException, BioException {

		SequencesProvider provider = new SequencesProvider(in);	
		return sm.doSyncSearch(provider, databank, parameters);
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
	 * Finish {@link Genoogle}.
	 * 
	 * @throws InterruptedException
	 */
	public void shutdown() throws InterruptedException {
		sm.shutdown();
	}

	public static void main(String[] args) throws IOException, InvalidHeaderData, ValueOutOfBoundsException,
			IllegalSymbolException, BioException, InvalidConfigurationException {
		PropertyConfigurator.configure("conf/log4j.properties");
		logger.info(SOFTWARE_NAME + " - " + VERSION);
		logger.info(COPYRIGHT_NOTICE);

		List<AbstractSequenceDataBank> dataBanks = XMLConfigurationReader.getDataBanks();

		if (args.length == 0) {
			Console console = new Console();
			new Thread(console).start();
		} else {

			String option = args[0];
			System.out.println("Options: " + option);

			if (option.equals("-h")) {
				showHelp();
			}

			if (option.equals("-g")) {
				logger.info("Searching for non encoded data banks.");

				for (AbstractSequenceDataBank dataBank : dataBanks) {
					if (!dataBank.check()) {
						dataBank.delete();
						logger.info("Data bank " + dataBank.getName() + " is not encoded.");
						try {
							dataBank.encodeSequences();
						} catch (Exception e) {
							logger.fatal(e);
							return;
						}
					}
				}
				logger.info("All specified data banks are encoded. You can do yours searchs now.");
				return;
			}

			else if (args.length >= 2 && option.equals("-b")) {				
				String inputFile = args[1];
				Console console = new Console(new File(inputFile));
				new Thread(console).start();

			} else {
				showHelp();
			}
		}
	}

	private static void showHelp() {
		logger.info("Options for Genoogle console mode execution:");
		logger.info(" -h              : this help.");
		logger.info(" -g              : encode all not encoded databanks specified at conf/genoogle.conf .");
		logger.info(" -b <BATCH_FILE> : starts genoogle and execute the <BATCH_FILE> .");
	}
}
