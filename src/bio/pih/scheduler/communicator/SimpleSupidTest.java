package bio.pih.scheduler.communicator;

import bio.pih.scheduler.AbstractWorker;
import bio.pih.scheduler.Scheduler;
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
//		
//		c = new MockWorker(5003);
//		c.start();
//		
//		c = new MockWorker(5004);
//		c.start();
								
		String[] w = new String[3];		
		w[0] = "localhost:5000";
		w[1] = "localhost:5001";
		w[2] = "localhost:5002";

		Scheduler s = new Scheduler(w);
		
		s.start();
		
		while (!s.isReady()) {
			Thread.sleep(10);			
		}

		s.doSearch("dummy", "actg_1");
//		s.doSearch("hey", "actg_2");
//		s.doSearch("ho", "actg_3");
//		s.doSearch("lets", "actg_4");
//		s.sendRequest(new RequestMessage("go", "actg_5"));
//		s.sendRequest(new RequestMessage("pet", "actg_6"));		
//		Thread.sleep(1000);
//		s.sendRequest(new RequestMessage("sematary", "actg_7"));
//		s.sendRequest(new RequestMessage("goblins", "actg_8"));
//		s.sendRequest(new RequestMessage("R.A.M.O.N.E.S", "actg_9"));
//		s.sendRequest(new RequestMessage("N.Y.C", "actg_10"));
//		Thread.sleep(1000);
//		s.sendRequest(new RequestMessage("Misfit", "actg_11"));
//		s.sendRequest(new RequestMessage("radio", "actg_12"));
//		s.sendRequest(new RequestMessage("fingolfin", "actg_13"));
//		s.sendRequest(new RequestMessage("sono", "actg_14"));
//		Thread.sleep(1000);
//		s.sendRequest(new RequestMessage("ultimo", "actg_15"));
//		s.sendRequest(new RequestMessage("banho", "actg_16"));
		
		while (s.isWaitingSearch()) {
			Thread.sleep(10);			
		}
		
		s.stop();
	}
}
