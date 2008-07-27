package bio.pih.web;

import java.io.File;
import java.net.InetAddress;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;

public class WebServer {

	private volatile boolean running = true;
	Embedded embedded = null;
	private final String path;
	
	public WebServer(String path) throws LifecycleException {
		this.path = path;
		System.out.println(path);
		System.out.println(new File(".").getAbsoluteFile());
		assert(new File(path).exists());
		
		System.setProperty("catalina.home", path);
		
		embedded = new Embedded();
		
		Engine engine = embedded.createEngine();
		engine.setDefaultHost("localhost");
		
		Host host = embedded.createHost("localhost", path+"/");
		engine.addChild(host);
		engine.setName("genoogle httpd");
		
		Context context = embedded.createContext("", path+"/ROOT/");
		host.addChild(context);
		
		embedded.addEngine(engine);
		Connector connector = embedded.createConnector((InetAddress) null,
				8080, false);
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
		String path = args[0];

		new WebServer(path).start();
	}
}