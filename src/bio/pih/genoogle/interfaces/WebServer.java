package bio.pih.genoogle.interfaces;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import bio.pih.genoogle.Genoogle;
import bio.pih.genoogle.GenoogleListener;

/**
 * Genoogle Embedded Web Service.
 * It is used to provide the JSP pages and the Web Services.
 * 
 * @author albrecht
 */
public class WebServer implements Runnable, GenoogleListener {
	
	static Logger logger = Logger.getLogger(WebServer.class.getName());
	Server server;

	/**
	 * @param port http port.
	 * @param path path to the webapp.
	 * @param standAlone if it will run the JSPs pages that will use WebServices to make access Genoogle. 
	 */
	public WebServer(int port, String path, boolean standAlone) throws Exception {
		if (!standAlone) {
			Genoogle.getInstance().addListerner(this);
		}
       
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
		boolean standAlone = false;
		if (args.length == 3) {
			standAlone = Boolean.parseBoolean(args[2]);
		}

		if (!standAlone) {
			new Thread(new Console()).start();
		}
		new WebServer(port, path, standAlone).start();
	}
}
