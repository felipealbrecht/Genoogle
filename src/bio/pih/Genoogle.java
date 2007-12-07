package bio.pih;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import bio.pih.scheduler.AbstractWorker;
import bio.pih.scheduler.Scheduler;
import bio.pih.tests.scheduler.MockWorker;

/**
 * Genoogle main class
 * @author albrecht
 *
 */
public class Genoogle {

	/**
	 * @param args
	 * <p>Options: <br>
	 * -s Scheduler
	 * -w [port] Worker
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		if (args.length < 1) {
			System.out.println("argumentos inválidos");
			return;
		}

		
		if (args[0].equals("-s") && args.length == 2) {
			System.out.println("iniciando scheduler");
			String fileName = args[1];
			BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
			List<String> workers = new LinkedList<String>();
			String line;
			while ((line = br.readLine()) != null) {
				workers.add(line);
			}
			Scheduler s = new Scheduler(workers.toArray(new String[workers.size()]));
			s.start();
			
		} else if (args[0].equals("-w")) {
			int port = 5000;			
			if (args.length == 2) {
				port = Integer.parseInt(args[1]);				
			}
			System.out.println("worker na porta: " + port);
			AbstractWorker worker = new MockWorker(port);
			worker.start();
			
		} else {
			System.out.println("-s para scheduler ou -w [port] para worker");
		}

	}
}
