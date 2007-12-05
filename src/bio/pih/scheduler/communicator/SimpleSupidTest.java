package bio.pih.scheduler.communicator;

import java.util.LinkedList;
import java.util.List;

import bio.pih.scheduler.AbstractWorker;
import bio.pih.scheduler.communicator.message.RequestMessage;
import bio.pih.tests.scheduler.MockWorker;

/**
 * Simple stupid test
 * @author albrecht
 * TODO: do it in a junit test!
 */
public class SimpleSupidTest {

	/**
	 * Just execute.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		AbstractWorker c;
				
		c = new MockWorker(5000);
		c.start();
		
		c = new MockWorker(5001);
		c.start();
		
		c = new MockWorker(5002);
		c.start();
		
		c = new MockWorker(5003);
		c.start();
		
		c = new MockWorker(5004);
		c.start();
								
		List<String> wList = new LinkedList<String>();
		wList.add("localhost:5000");
		wList.add("localhost:5001");
		wList.add("localhost:5002");
		wList.add("localhost:5003");
		wList.add("localhost:5004");
		SchedulerCommunicator s = new SchedulerCommunicator(wList);
		
		s.start();
		
		while (!s.isReady()) {
			Thread.sleep(10);			
		}

		s.sendRequest(new RequestMessage("dummy", "actg_1"));
		s.sendRequest(new RequestMessage("hey", "actg_2"));
		s.sendRequest(new RequestMessage("ho", "actg_3"));
		s.sendRequest(new RequestMessage("lets", "actg_4"));
		s.sendRequest(new RequestMessage("go", "actg_5"));
		s.sendRequest(new RequestMessage("pet", "actg_6"));		
		Thread.sleep(1000);
		s.sendRequest(new RequestMessage("sematary", "actg_7"));
		s.sendRequest(new RequestMessage("goblins", "actg_8"));
		s.sendRequest(new RequestMessage("R.A.M.O.N.E.S", "actg_9"));
		s.sendRequest(new RequestMessage("N.Y.C", "actg_10"));
		Thread.sleep(1000);
		s.sendRequest(new RequestMessage("Misfit", "actg_11"));
		s.sendRequest(new RequestMessage("radio", "actg_12"));
		s.sendRequest(new RequestMessage("fingolfin", "actg_13"));
		s.sendRequest(new RequestMessage("sono", "actg_14"));
		Thread.sleep(1000);
		s.sendRequest(new RequestMessage("ultimo", "actg_15"));
		s.sendRequest(new RequestMessage("banho", "actg_16"));
		
		s.stop();
	}

}
