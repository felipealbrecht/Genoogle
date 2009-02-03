package bio.pih.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import bio.pih.SOIS;
import bio.pih.index.InvalidHeaderData;
import bio.pih.io.Output;
import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchParams;
import bio.pih.search.UnknowDataBankException;
import bio.pih.search.SearchParams.Parameter;
import bio.pih.search.results.SearchResults;

import com.google.common.collect.Maps;

public class WebServer implements Runnable {

	private volatile boolean running = true;
	Embedded embedded = null;

	public WebServer(String defaultHost, int port, String path) throws LifecycleException,
			InvalidHeaderData, IllegalSymbolException, BioException {
		System.out.println(defaultHost);
		System.out.println(path);

		assert (new File(path).exists());
		System.out.println(SOIS.getInstance().getClass() + " loaded.");
		System.setProperty("catalina.home", path);

		embedded = new Embedded();

		Engine engine = embedded.createEngine();
		engine.setDefaultHost(defaultHost);

		Host host = embedded.createHost(defaultHost, path + "/");
		engine.addChild(host);
		engine.setName("genoogle httpd");

		Context context = embedded.createContext("", path + "/genoogle/");
		host.addChild(context);

		embedded.addEngine(engine);
		Connector connector = embedded.createConnector((InetAddress) null, port, false);
		embedded.addConnector(connector);
	}

	public void start() throws LifecycleException, InterruptedException {
		embedded.start();
		while (running) {
			Thread.sleep(1000);
		}
	}

	public void stop() throws LifecycleException {
		embedded.stop();
		running = false;
	}

	@Override
	public void run() {
		try {
			this.start();
		} catch (LifecycleException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws LifecycleException, InterruptedException,
			InvalidHeaderData, IllegalSymbolException, BioException {
		String defaultHost = args[0];
		String path = args[1];
		int port = Integer.parseInt(args[2]);

		new Thread(new Console()).start();
		new WebServer(defaultHost, port, path).start();
	}
}

class Console implements Runnable {

	private static final String EXIT = "exit";
	private static final String DEFAULT = "default";
	private static final String LIST = "list";
	private static final String GC = "gc";
	private static final String PARAMETERS = "parameters";
	private static final String BATCH = "batch";
	private static String SEARCH = "search";

	public void run() {
		execute(new InputStreamReader(System.in), false);
	}
	
	public void execute(InputStreamReader isr, boolean echo) {
		BufferedReader lineReader = new BufferedReader(isr);
		String line;

		SOIS sois = null;
		try {
			sois = SOIS.getInstance();
		} catch (IllegalSymbolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidHeaderData e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.print("genoogle console> ");

		try {
			while ((line = lineReader.readLine()) != null) {
				if (echo) {
					System.out.println(line);
				}
				try {
					line = line.trim();
					if (line.length() == 0) {
						continue;
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
								List<SearchResults> results = sois.doBatchSyncSearch(in, db, parameters);
								Document document = Output.genoogleOutputToXML(results);
								OutputFormat outformat = OutputFormat.createPrettyPrint();
								outformat.setTrimText(false);
								outformat.setEncoding("UTF-8");
								XMLWriter writer = new XMLWriter(new FileOutputStream(new File(outputFile
										+ ".xml")), outformat);
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
						for (SequenceDataBank db : sois.getDatabanks()) {
							System.out.println(db.toString());
						}

					} else if (commands[0].equals(DEFAULT)) {
						System.out.println(sois.getDefaultDatabank());

					} else if (commands[0].equals(PARAMETERS)) {
						for (SearchParams.Parameter param: SearchParams.Parameter.values()) {
							System.out.println(param.getName());
						}
						
					} else if (commands[0].endsWith(BATCH)) {
						if (commands.length != 2) {
							System.out.println("BATCH <batchfile>");
						}
						
						File f = new File(commands[1]);
						execute(new InputStreamReader(new FileInputStream(f)), true);
						
						
					} else if (commands[0].equals(EXIT)) {
						System.out.println("Bye.");
						System.exit(0);

					} else {
						System.err.println("Unknow command: " + commands[0]);
					}

					System.out.print("genoogle console> ");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalSymbolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknowDataBankException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Saiu");
	}
}
