package bio.pih.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import bio.pih.Genoogle;
import bio.pih.io.AbstractSequenceDataBank;
import bio.pih.io.Output;
import bio.pih.search.SearchParams;
import bio.pih.search.SearchParams.Parameter;
import bio.pih.search.results.SearchResults;

import com.google.common.collect.Maps;

public class Console implements Runnable {
	private static Logger logger = Logger.getLogger(Console.class.getCanonicalName());

	private static final String EXIT = "exit";
	private static final String DEFAULT = "default";
	private static final String LIST = "list";
	private static final String GC = "gc";
	private static final String PARAMETERS = "parameters";
	private static final String BATCH = "batch";
	private static final String SEARCH = "search";
	private static final String PREV = "prev";

	private static Logger profileLogger = Logger.getLogger("profile");

	private File inputBatch;

	private final Genoogle genoogle;

	public Console(Genoogle genoogle) {
		this.genoogle = genoogle;
	}

	/**
	 * Starts console informing a batch file to be executed.
	 */
	public Console(Genoogle genoogle, File inputBatch) {
		this(genoogle);
		this.inputBatch = inputBatch;
	}

	public void run() {
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
		PrintStream output = null;

		try {
			File file = new File("timer_output");
			output = new PrintStream(file);
		} catch (FileNotFoundException e1) {
			logger.error(e1);
			return;
		}

		boolean executePrev = false;
		String prev = null;
		String line = null;

		System.out.print("genoogle console> ");

		try {
			while (executePrev || (line = lineReader.readLine()) != null) {
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

							System.out.println("DB: " + db);
							System.out.println("Query: " + queryFile);
							System.out.println("Output: " + outputFile);

							Map<Parameter, Object> parameters = Maps.newHashMap();

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
								parameters.put(p, value);
							}

							if (new File(queryFile).exists()) {
								BufferedReader in = new BufferedReader(new FileReader(queryFile));
								profileLogger.info("<" + line + ">");
								List<SearchResults> results = genoogle.doBatchSyncSearch(in, db, parameters);
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
						for (SearchParams.Parameter param : SearchParams.Parameter.values()) {
							System.out.println(param.getName());
						}

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
						System.out.println("Bye.");
						System.exit(0);

					} else {
						System.err.println("Unknow command: " + commands[0]);
					}

					if (end != -1) {
						output.println(line + "\t" + begin + "\t" + end + "\t" + (end - begin));
					}

					prev = line;
					System.out.print("genoogle console> ");
				} catch (Exception e) {
					logger.fatal(e);
					return;
				}
			}
		} catch (IOException e) {
			logger.fatal(e);
			return;
		}
	}
}
