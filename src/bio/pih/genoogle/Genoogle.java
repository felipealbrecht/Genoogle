/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle;

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

import bio.pih.genoogle.index.InvalidHeaderData;
import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.interfaces.Console;
import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.io.InvalidConfigurationException;
import bio.pih.genoogle.io.SequencesProvider;
import bio.pih.genoogle.io.XMLConfigurationReader;
import bio.pih.genoogle.search.SearchManager;
import bio.pih.genoogle.search.SearchParams;
import bio.pih.genoogle.search.UnknowDataBankException;
import bio.pih.genoogle.search.SearchParams.Parameter;
import bio.pih.genoogle.search.results.SearchResults;
import bio.pih.genoogle.seq.LightweightSymbolList;

import com.google.common.collect.Lists;

/**
 * The main class of Genoogle. To get a Genoogle instance, use the getInstance() method.
 * 
 * @author albrecht
 */
public final class Genoogle {

	public static String line = System.getProperty("line.separator");

	public static String SOFTWARE_NAME = "Genoogle BETA";
	public static Double VERSION = 0.74;
	public static String AUTHOR = "Felipe Albrecht (felipe.albrecht@gmail.com).";
	public static String WEB_PAGE = "http://genoogle.pih.bio.br";
	public static String COPYRIGHT = "Copyright (C) 2008,2009  Felipe Fernandes Albrecht";

	public static String COPYRIGHT_NOTICE = line
			+ "-----------------------------------------------------------------------------------------" + line
			+ SOFTWARE_NAME + " Copyright (C) 2008,2009  " + AUTHOR + line
			+ "This program comes with ABSOLUTELY NO WARRANTY;" + line
			+ "This is free software, and you are welcome to redistribute it under certain conditions;" + line
			+ "See the LICENCE file or check at http://www.gnu.org/licenses/gpl-3.0.html for full license." + line
			+ "-------------------------------------------------------------------------------------------";

	SearchManager sm = null;
	private static Genoogle singleton = null;

	static Logger logger = Logger.getLogger(Genoogle.class.getName());

	/**
	 * Get the {@link Genoogle} execution instance.
	 * 
	 * @return {@link Genoogle} singleton instance or <code>null</code> if an error did happen.
	 */
	public synchronized static Genoogle getInstance() {
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
			} catch (InvalidHeaderData e) {
				logger.fatal(e.getMessage());
				return null;
			} catch (IllegalSymbolException e) {
				logger.fatal(e.getMessage());
				return null;
			} catch (BioException e) {
				logger.fatal(e.getMessage());
				return null;
			} catch (InvalidConfigurationException e) {
				logger.fatal(e.getMessage());
				return null;
			}

		}
		return singleton;
	}

	/**
	 * Private constructor.
	 */
	private Genoogle() throws IOException, ValueOutOfBoundsException, InvalidHeaderData, IllegalSymbolException,
			BioException, InvalidConfigurationException {
		PropertyConfigurator.configure("conf/log4j.properties");
		sm = XMLConfigurationReader.getSearchManager();
	}

	/**
	 * Classes which use Genoogle and should be notified about changes.
	 */
	private List<GenoogleListener> listerners = Lists.newLinkedList();

	/**
	 * Add a new listener to Genoogle which will be notified about changes.
	 * 
	 * @param listerner
	 */
	public void addListerner(GenoogleListener listerner) {
		listerners.add(listerner);
	}

	/**
	 * Finish {@link Genoogle} and notify the listeners to finish.
	 */
	public void finish() throws InterruptedException {
		for (GenoogleListener listerner : listerners) {
			listerner.finish();
		}
		sm.shutdown();
	}

	/**
	 * Get the data bank name where the searches are performed when the data bank is not specified.
	 * 
	 * @return Default data bank name
	 */
	public String getDefaultDatabank() {
		return sm.getDefaultDataBankName();
	}

	/**
	 * Get a {@link Collection} of all available data banks
	 * 
	 * @return {@link Collection} of all {@link AbstractSequenceDataBank} which it is possible to
	 *         execute a query.
	 */
	public Collection<AbstractSequenceDataBank> getDatabanks() {
		return sm.getDatabanks();
	}

	/**
	 * Do the search at the default data bank, reading the queries from the given
	 * {@link BufferedReader} and returning the execution line only after all searches are finished.
	 * 
	 * @param in
	 *            {@link BufferedReader} where the sequences are read.
	 * @return {@link List} of {@link SearchResults}, being one {@link SearchResults} for each input
	 *         sequence inside the given {@link BufferedReader}.
	 */
	public List<SearchResults> doBatchSyncSearch(BufferedReader in) throws IOException, UnknowDataBankException,
			InterruptedException, ExecutionException, NoSuchElementException, BioException {
		String defaultDataBankName = sm.getDefaultDataBankName();
		return doBatchSyncSearch(in, defaultDataBankName);
	}

	/**
	 * Do the search at the specified data bank, reading the queries from the given
	 * {@link BufferedReader} and returning the execution line only after all searches are finished.
	 * 
	 * @param in
	 *            {@link BufferedReader} where the sequences are read.
	 * @param databankName
	 *            Data bank name where the search will be made.
	 * @return {@link List} of {@link SearchResults}, being one {@link SearchResults} for each input
	 *         sequence inside the given {@link BufferedReader}.
	 */
	public List<SearchResults> doBatchSyncSearch(BufferedReader in, String databankName) throws IOException,
			UnknowDataBankException, InterruptedException, ExecutionException, NoSuchElementException, BioException {
		return doBatchSyncSearch(in, databankName, null);
	}

	/**
	 * Do the search at the specified data bank, reading the queries from the given
	 * {@link BufferedReader}, using the specified {@link Map} of {@link Parameter} as parameters,
	 * and returning the execution line only after all searches are finished.
	 * 
	 * @param in
	 *            {@link BufferedReader} where the sequences are read.
	 * @param databankName
	 *            Data bank name where the search will be made.
	 * @param parameters
	 *            {@link Map} of {@link Parameter} which will be used in these searches.
	 * 
	 * @return {@link List} of {@link SearchResults}, being one {@link SearchResults} for each input
	 *         sequence inside the given {@link BufferedReader}.
	 */
	public List<SearchResults> doBatchSyncSearch(BufferedReader in, String databankName,
			Map<Parameter, Object> parameters) throws IOException, UnknowDataBankException, InterruptedException,
			ExecutionException, NoSuchElementException, BioException {

		SequencesProvider provider = new SequencesProvider(in);
		return sm.doSyncSearch(provider, databankName, parameters);
	}

	/**
	 * Do the search of the given sequence at the default data bank and returning the execution line
	 * only after all searches are finished.
	 * 
	 * @param inputSequence
	 *            input sequence for the searching.
	 * @return A {@link SearchResults} containing the results of this search.
	 */
	public SearchResults doSyncSearch(String inputSequence) {
		String defaultDataBankName = sm.getDefaultDataBankName();
		return doSyncSearch(inputSequence, defaultDataBankName);
	}

	/**
	 * Do the search of the given sequence at the informed data bank and returning the execution
	 * line only after all searches are finished.
	 * 
	 * @param seqString
	 *            input sequence for the searching.
	 * @param dataBankName
	 *            data bank name where the search will be performed.
	 * 
	 * @return A {@link SearchResults} containing the results of this search.
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
	 * Do the search of the given sequence at the informed data bank, using the specified
	 * {@link Map} of {@link Parameter} as parameters, and returning the execution line only after
	 * all searches are finished.
	 * 
	 * @param seqString
	 *            input sequence for the searching.
	 * @param dataBankName
	 *            data bank name where the search will be performed.
	 * @param parameters
	 *            {@link Map} of {@link Parameter} which will be used in these searches.
	 * 
	 * @return A {@link SearchResults} containing the results of this search.
	 */
	public SearchResults doSyncSearch(String seqString, String dataBankName, Map<Parameter, Object> parameters) {
		SearchResults sr = null;
		seqString = seqString.trim();
		try {
			SymbolList sequence = LightweightSymbolList.createDNA(seqString);
			SearchParams sp = new SearchParams(sequence, dataBankName, parameters);
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
	 * Main method: Use the "-g" option to encode and create inverted index for the data banks or
	 * "-b file" to execute the commands specified at the file or do not use parameters and use the
	 * console.
	 */
	public static void main(String[] args) throws IOException, InvalidHeaderData, ValueOutOfBoundsException,
			IllegalSymbolException, BioException, InvalidConfigurationException {
		PropertyConfigurator.configure("conf/log4j.properties");
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
