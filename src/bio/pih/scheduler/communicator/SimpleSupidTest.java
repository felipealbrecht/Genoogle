package bio.pih.scheduler.communicator;

import java.util.LinkedList;
import java.util.List;

import bio.pih.scheduler.Worker;
import bio.pih.scheduler.communicator.message.RequestMessage;

/**
 * Simple stupid test
 * @author albrecht
 *
 */
public class SimpleSupidTest {

	/**
	 * Just execute.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Worker c;
				
		c = new Worker(5000);
		c.start();
		
		c = new Worker(5001);
		c.start();
								
		List<String> wList = new LinkedList<String>();
		wList.add("localhost:5000");
		wList.add("localhost:5001");				
		SchedulerCommunicator s = new SchedulerCommunicator(wList);
		
		s.start();
		
		while (!s.isReady()) {
			System.out.println(".");
			Thread.sleep(10);			
		}
	

		// for (int c = 0; c < 100; c++) {
		// int pos = Math.round( (float) Math.random() % clients.size());
		// Client client = clients.get(pos);
		// System.out.println(client);
		s.sendRequest(new RequestMessage("dummy", "actg"));

		// Servidor envia uma solicitação para todos os trabalhadores
		// Trabalhadores "processam" por um tempo rand(X)
		// Servidor envia segunda solicitação para todos os trabalhadores
		// Trabalhadores retornam 1a solicitação
		// Servidor exibe 1o relatorio
		// Trabalhadores retornam 2a solicitação
		// Servidor exibe 2o relatorio
		// Finaliza.
		// }
		
//		Thread.sleep(10000);
		
		s.stop();
	}

}
