package bio.pih;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.interfaces.Console;
import bio.pih.io.AbstractSequenceDataBank;
import bio.pih.io.XMLConfigurationReader;
import bio.pih.search.SearchManager;
import bio.pih.search.SearchParams;
import bio.pih.search.UnknowDataBankException;
import bio.pih.search.SearchParams.Parameter;
import bio.pih.search.results.SearchResults;
import bio.pih.seq.LightweightSymbolList;

import com.google.common.collect.Lists;

/**
 * Genoogle non distributed and no server implementation. For tests and validation propose.
 * 
 * 
 * @author albrecht
 */
public class Genoogle {

	public static String SOFTWARE_NAME = "Genoogle BETA";
	public static Double VERSION = 0.71;
	public static String COPYRIGHT_NOTICE = "Felipe Albrecht (felipe.albrecht@gmail.com) - 2009.";
	public static String WEB_PAGE = "http://genoogle.pih.bio.br";

	SearchManager sm = null;
	private static Genoogle singleton = null;

	static Logger logger = Logger.getLogger(Genoogle.class.getName());

	/**
	 * @return Genoogle Singleton instance.
	 * @throws InvalidHeaderData
	 * @throws BioException
	 * @throws IllegalSymbolException
	 */
	public synchronized static Genoogle getInstance() throws InvalidHeaderData, IllegalSymbolException, BioException {
		if (singleton == null) {
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
	 */
	private Genoogle() throws IOException, ValueOutOfBoundsException, InvalidHeaderData, IllegalSymbolException,
			BioException {
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
	public List<SearchResults> doBatchSyncSearch(BufferedReader in) throws IOException, IllegalSymbolException,
			UnknowDataBankException, InterruptedException, ExecutionException {
		String defaultDataBankName = sm.getDefaultDataBankName();
		return doBatchSyncSearch(in, defaultDataBankName);
	}

	public Collection<AbstractSequenceDataBank> getDatabanks() {
		return sm.getDatabanks();
	}

	public String getDefaultDatabank() {
		return sm.getDefaultDataBankName();
	}

	public List<SearchResults> doBatchSyncSearch(BufferedReader in, String databank) throws IllegalSymbolException,
			IOException, UnknowDataBankException, InterruptedException, ExecutionException {
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
			throws IOException, IllegalSymbolException, UnknowDataBankException, InterruptedException,
			ExecutionException {

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
	 * Finish {@link Genoogle}.
	 * 
	 * @throws InterruptedException
	 */
	public void shutdown() throws InterruptedException {
		sm.shutdown();
	}

	public static void main(String[] args) throws IOException, InvalidHeaderData, ValueOutOfBoundsException,
			IllegalSymbolException, BioException {
		PropertyConfigurator.configure("conf/log4j.properties");
		logger.info(SOFTWARE_NAME + " - " + VERSION);
		logger.info(COPYRIGHT_NOTICE);

		List<AbstractSequenceDataBank> dataBanks = XMLConfigurationReader.getDataBanks();

		if (args.length == 0) {
			Genoogle genoogle = Genoogle.getInstance();
			Console console = new Console(genoogle);
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
				logger.info("Starting Genoogle .");
				Genoogle genoogle = Genoogle.getInstance();

				String inputFile = args[1];
				Console console = new Console(genoogle, new File(inputFile));
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
