package bio.pih.scheduler.communicator;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

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
		Server s = new Server();
		s.start();

		while (!s.isReady()) {
			Thread.yield();
		}
		
		List<Client> clients = new LinkedList<Client>();
		Client c;

		c = new Client(InetAddress.getLocalHost(), 5555);
		c.start();
		clients.add(c);

		c = new Client(InetAddress.getLocalHost(), 5555);
		c.start();
		clients.add(c);

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
		Thread.sleep(10000);
		for (Client client: clients) {
			client.stop();		
		}
		s.stop();
	}

}
