package bio.pih.genoogle.interfaces;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import bio.pih.genoogle.Genoogle;
import bio.pih.genoogle.GenoogleListener;

public class WebServer implements Runnable, GenoogleListener {
	
	static Logger logger = Logger.getLogger(WebServer.class.getName());
	Server server;

	public WebServer(int port, String path) throws Exception {
        Genoogle.getInstance().addListerner(this);
       
        server = new Server(port);        
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar(path);        
        
        server.setHandler(webapp); 
	}

	public void start() throws Exception {
		server.start();
	}

	public void stop() throws Exception  {
		server.stop();
	}

	@Override
	public void run() {
		try {
			this.start();
		} catch (Exception e) {
			logger.fatal(e);
		}
	}
	
	@Override
	public void finish() {
		logger.info("Genoogle sent a command to finish. Bye!");
		try {
			this.stop();
		} catch (Exception e) {
			logger.error(e);
		}		
	}

	public static void main(String[] args) throws Exception {
		String path = args[0];
		int port = Integer.parseInt(args[1]);

		new Thread(new Console()).start();
		new WebServer(port, path).start();
	}
}
