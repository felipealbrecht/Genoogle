/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import bio.pih.genoogle.Genoogle;
import bio.pih.genoogle.GenoogleListener;
import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.io.InvalidConfigurationException;
import bio.pih.genoogle.io.Output;
import bio.pih.genoogle.search.SearchParams;
import bio.pih.genoogle.search.UnknowDataBankException;
import bio.pih.genoogle.search.SearchParams.Parameter;
import bio.pih.genoogle.search.results.SearchResults;

import com.google.common.collect.Maps;

public class Console implements Runnable, GenoogleListener {
	private static Logger logger = Logger.getLogger(Console.class.getCanonicalName());

	private static final String EXIT = "exit";
	private static final String DEFAULT = "default";
	private static final String LIST = "list";
	private static final String GC = "gc";
	private static final String PARAMETERS = "parameters";
	private static final String SET = "set";
	private static final String BATCH = "batch";
	private static final String SEARCH = "search";
	private static final String PREV = "prev";
	private static final String HELP = "help";

	private static Logger profileLogger = Logger.getLogger("profile");

	private File inputBatch;
	
	private volatile boolean running;

	private final Genoogle genoogle;

	public Console() throws IllegalSymbolException, BioException, InvalidConfigurationException {
		this.genoogle = Genoogle.getInstance();
		genoogle.addListerner(this);
	}

	/**
	 * Starts console informing a batch file to be executed.
	 * @throws InvalidConfigurationException 
	 * @throws BioException 
	 * @throws InvalidHeaderData 
	 * @throws IllegalSymbolException 
	 */
	public Console(File inputBatch) throws IllegalSymbolException, BioException, InvalidConfigurationException {
		this();
		this.inputBatch = inputBatch;
	}

	public void run() {
		running = true;
		if (inputBatch != null) {
			try {
				execute(new InputStreamReader(new FileInputStream(inputBatch)), true);
			} catch (FileNotFoundException e) {
				logger.error(e);
			}
		}
		execute(new InputStreamReader(System.in), false);
	}

	public void execute(InputStreamReader isr, boolean echo) {
		BufferedReader lineReader = new BufferedReader(isr);

		boolean executePrev = false;
		String prev = null;
		String line = null;

		Map<Parameter, Object> consoleParameters = SearchParams.getSearchParamsMap();

		System.out.print("genoogle console> ");

		try {
			while (running &&  (executePrev || (line = lineReader.readLine()) != null) ) {
				long begin = System.currentTimeMillis();
				long end = -1;

				if (echo) {
					System.out.println(line);
				}
				try {
					line = line.trim();
					if (line.length() == 0) {
						continue;
					}

					if (executePrev) {
						if (prev == null) {
							System.out.println("no previous commands.");
							executePrev = false;
							continue;
						}
						line = prev;
						System.out.println(line);
						executePrev = false;
					}

					String[] commands = line.split("[ \t]+");
					if (commands[0].equals(SEARCH)) {
						if (commands.length >= 4) {
							String db = commands[1];
							String queryFile = commands[2];
							String outputFile = commands[3];

							Map<Parameter, Object> searchParameters = Maps.newHashMap();
							searchParameters.putAll(consoleParameters);

							for (int i = 4; i < commands.length; i++) {
								String command = commands[i];
								String[] split = command.split("=");
								if (split.length != 2) {
									System.out.println(command + " is an invalid parameter.");
								}
								String paramName = split[0];
								String paramValue = split[1];

								Parameter p = Parameter.getParameterByName(paramName);
								if (p == null) {
									System.out.println(paramName + " is an invalid parameter name");
									continue;
								}
								Object value = p.convertValue(paramValue);
								searchParameters.put(p, value);
							}

							if (new File(queryFile).exists()) {
								BufferedReader in = new BufferedReader(new FileReader(queryFile));
								profileLogger.info("<" + line + ">");
								List<SearchResults> results = genoogle.doBatchSyncSearch(in, db, searchParameters);
								end = System.currentTimeMillis();
								long total = end - begin;
								profileLogger.info("</" + line + ":" + total + ">");
								Document document = Output.genoogleOutputToXML(results);
								OutputFormat outformat = OutputFormat.createPrettyPrint();
								outformat.setTrimText(false);
								outformat.setEncoding("UTF-8");
								XMLWriter writer = new XMLWriter(new FileOutputStream(new File(outputFile + ".xml")), outformat);
								writer.write(document);
								writer.flush();

							} else {
								System.err.println("query file: " + queryFile + " does not exist.");
							}

						} else {
							System.out.println("SEARCH DB QUERY_FILE OUTPUT_FILE");
						}

					} else if (commands[0].equals(GC)) {
						System.gc();

					} else if (commands[0].equals(LIST)) {
						for (AbstractSequenceDataBank db : genoogle.getDatabanks()) {
							System.out.println(db.toString());
						}

					} else if (commands[0].equals(DEFAULT)) {
						System.out.println(genoogle.getDefaultDatabank());

					} else if (commands[0].equals(PARAMETERS)) {						
						for (Entry<Parameter, Object> entry : consoleParameters.entrySet()) {						
							System.out.println(entry.getKey().getName() + "=" + entry.getValue());
						}

					} else if (commands[0].equals(SET)) {
						String[] split = commands[1].split("=");
						if (split.length != 2) {
							System.out.println(commands[1] + " is invalid set parameters option.");
						}
						String paramName = split[0];
						String paramValue = split[1];

						Parameter p = Parameter.getParameterByName(paramName);
						if (p == null) {
							System.out.println(paramName + " is an invalid parameter name");
							continue;
						}
						Object value = p.convertValue(paramValue);
						consoleParameters.put(p, value);
						System.out.println(paramName + " is " + paramValue);

					} else if (commands[0].equals(PREV) || commands[0].equals("p")) {
						executePrev = true;
						continue;

					} else if (commands[0].endsWith(BATCH)) {
						if (commands.length != 2) {
							System.out.println("BATCH <batchfile>");
							continue;
						}

						File f = new File(commands[1]);
						execute(new InputStreamReader(new FileInputStream(f)), true);
						end = System.currentTimeMillis();

					} else if (commands[0].equals(EXIT)) {
						genoogle.finish();

					} else if (commands[0].equals(HELP)) {
						System.out.println("Commands:");
						System.out.println("search <data bank> <input file> <output file> <parameters>: does the search");
						System.out.println("list : lists the data banks.");
						System.out.println("parameters : shows the search parameters and their values.");
						System.out.println("set <parameter>=<value> : set the parameters value.");
						System.out.println("gc : executes the java garbage collection.");
						System.out.println("prev or l: executes the last command.");
						System.out.println("batch <batch file> : runs the commands listed in this batch file.");
						System.out.println("help: this help.");
						System.out.println("exit : finish Genoogle execution.");
						System.out.println();
						System.out.println("Search Parameters:");

						System.out.println("MaxSubSequenceDistance : maximum index entries distance to be considered in the same HSPs.");
						System.out.println("SequencesExtendDropoff : drop off for sequence extension.");
						System.out.println("MaxHitsResults : maximum quantity of returned results.");
						System.out.println("QuerySplitQuantity : how many slices the input query will be divided.");
						System.out.println("MinQuerySliceLength : minimum size of each input query slice.");
						System.out.println("MaxThreadsIndexSearch : quantity of threads which will be used to index search.");
						System.out.println("MaxThreadsExtendAlign : quantity of threads which will be used to extend and align the HSPs.");						
						System.out.println("MatchScore : score when has a match at the alignment.");
						System.out.println("MismatchScore : score when has a mismatch at the alignment.");
					} else {
						System.err.println("Unknow command: " + commands[0]);
					}

					prev = line;
					System.out.print("genoogle console> ");
				} catch (IndexOutOfBoundsException e) {
					logger.fatal(e);
					continue;
				} catch (UnsupportedEncodingException e) {
					logger.fatal(e);
					continue;
				} catch (FileNotFoundException e) {
					logger.fatal(e);
					continue;
				} catch (IOException e) {
					logger.fatal(e);
					continue;
				} catch (NoSuchElementException e) {
					logger.fatal(e);
					continue;
				} catch (UnknowDataBankException e) {
					logger.error(e);
					continue;
				} catch (InterruptedException e) {
					logger.fatal(e);
					continue;
				} catch (ExecutionException e) {
					logger.fatal(e);
					continue;
				} catch (BioException e) {
					logger.fatal(e);
					continue;
				}
			}
		} catch (IOException e) {
			logger.fatal(e);
			return;
		}
	}

	@Override
	public void finish() {
		logger.info("Genoogle sent a command to finish. Bye!");
		this.running = false;
	}
}
