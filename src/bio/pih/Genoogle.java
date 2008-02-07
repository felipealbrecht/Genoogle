package bio.pih;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import bio.pih.index.InvalidHeaderData;
import bio.pih.index.SubSequencesComparer;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.scheduler.AbstractWorker;
import bio.pih.scheduler.Scheduler;
import bio.pih.scheduler.interfaces.CommandLine;
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
	 * -g [gerar dados]
	 * @throws Exception 
	 * @throws InvalidHeaderData 
	 * @throws ValueOutOfBoundsException 
	 */
	public static void main(String[] args) throws ValueOutOfBoundsException, InvalidHeaderData, Exception {
		if (args.length < 1) {
			System.out.println("argumentos invÃ¡lidos");
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
			CommandLine commandLine = new CommandLine(s, System.in);
			new Thread(commandLine, "Command Line").start();
			
		} else if (args[0].equals("-w")) {
			int port = 5000;			
			if (args.length == 2) {
				port = Integer.parseInt(args[1]);				
			}
			System.out.println("worker na porta: " + port);
			AbstractWorker worker = new MockWorker(port);
			worker.start();
			
		} else if (args[0].equals("-g")) {
			SubSequencesComparer.getDefaultInstance().generateData(true);
			
		} else {
			System.out.println("-s para scheduler ou -w [port] para worker ou -g para gerar dados necessários.");
		}

	}
}
