package bio.pih.web;

import java.io.File;
import java.net.InetAddress;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;

import bio.pih.SOIS;

public class WebServer {

	private volatile boolean running = true;
	Embedded embedded = null;

	public WebServer(String defaultHost, int port, String path) throws LifecycleException {
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
		Connector connector = embedded.createConnector((InetAddress) null,
				port, false);
		embedded.addConnector(connector);
	}

	public void start() throws LifecycleException {
		embedded.start();
		while (running) {
			Thread.yield();
		}
	}

	public void stop() throws LifecycleException {
		embedded.stop();
		running = false;
	}

	public static void main(String[] args) throws LifecycleException {
		String defaultHost = args[0];
		String path = args[1];
		int port = Integer.parseInt(args[2]);


		new WebServer(defaultHost, port, path).start();
	}
}