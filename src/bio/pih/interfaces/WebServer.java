package bio.pih.interfaces;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import bio.pih.Genoogle;

public class WebServer implements Runnable {
	
	static Logger logger = Logger.getLogger(WebServer.class.getName());
	private volatile boolean running = true;
	Server server;

	public WebServer(int port, String path) throws Exception {
        
        server = new Server(port);
         
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar(path);        
        
        server.setHandler(webapp); 
	}

	public void start() throws Exception {
		server.start();
		while (running) {
			Thread.sleep(1000);
		}
	}

	public void stop() throws Exception  {
		server.stop();
		running = false;
	}

	@Override
	public void run() {
		try {
			this.start();
		} catch (Exception e) {
			logger.fatal(e);
		}
	}

	public static void main(String[] args) throws Exception {
		String path = args[0];
		int port = Integer.parseInt(args[1]);

		
		Genoogle genoogle = Genoogle.getInstance();
		new Thread(new Console(genoogle)).start();
		new WebServer(port, path).start();
	}
}
