package bio.pih.interfaces;

import java.io.File;
import java.net.InetAddress;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;

import bio.pih.Genoogle;
import bio.pih.index.InvalidHeaderData;

public class WebServer implements Runnable {
	
	private volatile boolean running = true;
	Embedded embedded = null;

	public WebServer(String defaultHost, int port, String path) throws LifecycleException,
			InvalidHeaderData, IllegalSymbolException, BioException {
		System.out.println(defaultHost);
		System.out.println(path);

		assert (new File(path).exists());
		System.out.println(Genoogle.getInstance().getClass() + " loaded.");
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

	public static void main(String[] args) throws IllegalSymbolException, InvalidHeaderData, BioException, LifecycleException, InterruptedException {
		String defaultHost = args[0];
		String path = args[1];
		int port = Integer.parseInt(args[2]);

		Genoogle genoogle = Genoogle.getInstance();
		new Thread(new Console(genoogle)).start();
		new WebServer(defaultHost, port, path).start();
	}
}
