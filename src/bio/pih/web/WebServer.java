package bio.pih.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;

import bio.pih.SOIS;
import bio.pih.index.InvalidHeaderData;
import bio.pih.io.SequenceDataBank;
import bio.pih.search.UnknowDataBankException;

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
	private static String SEARCH = "search";

	public void run() {
		BufferedReader lineReader = new BufferedReader(new InputStreamReader(System.in));
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
				line = line.trim();
				String[] commands = line.split(" ");
				if (commands[0].equals(SEARCH)) {
					if (commands.length == 3) {
						String db = commands[1];
						String queryFile = commands[2];
						if (new File(queryFile).exists()) {
							BufferedReader in = new BufferedReader(new FileReader(queryFile));
							sois.doBatchSyncSearch(in, db);
						} else {
							System.err.println(queryFile + " does not exist.");
						}
						
					} else {
						System.out.println("SEARCH DB QUERY_FILE");
					}
					
				
					
				} else if (commands[0].equals(GC)) {
					System.gc();
					
				} else if (commands[0].equals(LIST)) {
					for (SequenceDataBank db : sois.getDatabanks()) {
						System.out.println(db.toString());
					}
					
				} else if (commands[0].equals(DEFAULT)) {
					System.out.println(sois.getDefaultDatabank());
					
				} else if (commands[0].equals(EXIT)) {
					System.out.println("Bye.");
					System.exit(0);
					
				} else {
					System.err.println("Unknow command: " + commands[0]);
				}
				
				
								
				System.out.print("genoogle console> ");
			}
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
}
