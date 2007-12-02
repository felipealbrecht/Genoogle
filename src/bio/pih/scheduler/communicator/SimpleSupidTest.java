package bio.pih.scheduler.communicator;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

public class SimpleSupidTest {

	public static void main(String[] args) throws Exception {
		Server s = new Server();
		
		s.start();
		List<Client> clients = new LinkedList<Client>();
		
		clients.add(new Client(InetAddress.getLocalHost(), 5555));
		clients.add(new Client(InetAddress.getLocalHost(), 5555));
		clients.add(new Client(InetAddress.getLocalHost(), 5555));
		clients.add(new Client(InetAddress.getLocalHost(), 5555));
		clients.add(new Client(InetAddress.getLocalHost(), 5555));
		
		s.stop();
		
		for (int c = 0; c < 100; c++) {
			int pos = Math.round( (float) Math.random() % clients.size());
			Client client = clients.get(pos);
			System.out.println(client);
		}
		
	}
	
	
}
