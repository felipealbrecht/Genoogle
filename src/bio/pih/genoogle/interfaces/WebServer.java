/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.interfaces;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
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
			Genoogle genoogle = Genoogle.getInstance();
			genoogle.addListerner(this);			
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
		PropertyConfigurator.configure(Genoogle.CONF_LOG4J_PROPERTIES_FILE.getAbsolutePath());
		
		logger.info(Genoogle.COPYRIGHT_NOTICE);
		
		String path = args[0];
		int port = Integer.parseInt(args[1]);
		boolean standAlone = false;
		if (args.length == 3) {
			standAlone = Boolean.parseBoolean(args[2]);
		}

		if (standAlone) {
			logger.info("Starting stand alone web server.");
			new WebServer(port, path, standAlone).start();
		} else {
			if (Genoogle.getInstance().getDatabanks().size() == 0) {
				logger.fatal("Genoogle does not have any data bank to perform the searches.");
				Genoogle.getInstance().finish();
				return;
			}
			logger.info("Starting web server for WebServices and Web pages.");
			new Thread(new Console()).start();			
			new WebServer(port, path, standAlone).start();
		}
	}
}


