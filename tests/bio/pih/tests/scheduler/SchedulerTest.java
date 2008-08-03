package bio.pih.tests.scheduler;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.naming.LimitExceededException;

import junit.framework.TestCase;

import org.junit.Test;

import bio.pih.scheduler.AbstractWorker;
import bio.pih.scheduler.Dispatcher;
import bio.pih.scheduler.communicator.Communicator;
import bio.pih.scheduler.interfaces.CommandLine;

/**
 * Tests for the scheduler and communicator
 * 
 * @author albrecht
 */
public class SchedulerTest extends TestCase {

	/**
	 * Test if the worker initialize and finish correctly.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException 
	 */
	@Test
	public void testWorkerStartUpAndFinalization() throws IOException, InterruptedException {
		AbstractWorker w = new MockWorker(50000);
		w.start();
		
		Thread.sleep(100);
		
		while (!w.isReady()) {
			Thread.sleep(100);
		}
		
		w.stop();
	}

	/**
	 * Test if the scheduler initialize and finish correctly.
	 * 
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	@Test
	public void testSchedulerStartUpAndFinalization() throws IOException, InterruptedException {				
		String[] hosts = new String[] {};
		
		Dispatcher s = new Dispatcher(hosts);
		s.start();
		while (!s.isReady() && !s.hasWorker()) {
			Thread.sleep(100);
		}
		s.stop();
	}
	
	/**
	 * Test the connection from the scheduler to the workers and disconnect  
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException 
	 */
	public void testSchedulerAndWorkers() throws IOException, InterruptedException {
		AbstractWorker w1 = new MockWorker(50000);
		AbstractWorker w2 = new MockWorker(50001);

		w1.start();		
		w2.start();
		
		while (!(w1.isReady() && w2.isReady())) {
			Thread.sleep(100);
		}
		
		String[] hosts = new String[] {"localhost:50000", "localhost:50001"};
		
		Dispatcher s = new Dispatcher(hosts);
		s.start();
		while (!s.isReady()) {
			Thread.sleep(100);
		}		
				
		while(!(w1.isConnected() && w2.isConnected())) {
			Thread.sleep(100);
		}
		
		s.stop();
				
		while (s.hasWorker()) {
			Thread.sleep(100);
		}
		
		// Time for workers socket timeout
		Thread.sleep(Communicator.SOCKET_TIMEOUT + 10);
		
	}

	/**
	 * Test if the searching scheduler is working  
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException 
	 */
	public void testDoSearchs() throws IOException, InterruptedException {
		AbstractWorker w1 = new MockWorker(50000);
		AbstractWorker w2 = new MockWorker(50001);
		AbstractWorker w3 = new MockWorker(50002);

		w1.start();		
		w2.start();
		w3.start();
		
		while (!(w1.isReady() && w2.isReady() && w3.isReady())) {
			Thread.sleep(100);
		}
		
		String[] hosts = new String[] {"localhost:50000", "localhost:50001", "localhost:50002"};
		
		Dispatcher s = new Dispatcher(hosts);
		s.start();
		while (!s.isReady()) {
			Thread.sleep(100);
		}		
		
		s.doSearch("database", "query1");
		s.doSearch("database", "query2");
		s.doSearch("database", "query3");
		
		while (s.isWaitingSearch()) {
			Thread.sleep(10);
		}
		
		s.stop();
		
		while (s.hasWorker()) {
			Thread.sleep(100);
		}
		
		// Time for workers socket timeout
		Thread.sleep(Communicator.SOCKET_TIMEOUT + 10);
	}

	/**
	 * Test if the searching scheduler is working  
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException 
	 */
	public void testInterfaceTest() throws IOException, InterruptedException {
		AbstractWorker w1 = new MockWorker(50000);
		AbstractWorker w2 = new MockWorker(50001);
		AbstractWorker w3 = new MockWorker(50002);

		w1.start();		
		w2.start();
		w3.start();
		
		while (!(w1.isReady() && w2.isReady() && w3.isReady())) {
			Thread.sleep(100);
		}
		
		String[] hosts = new String[] {"localhost:50000", "localhost:50001", "localhost:50002"};
		
		Dispatcher s = new Dispatcher(hosts);
		s.start();
		while (!s.isReady()) {
			Thread.sleep(100);
		}		
		
		String commands = "search db actgactg\n"+
				"check 1\n"+
				"read 1\n"+
				"clean 1\n"+
				"exit";
							
		
		CommandLine commandLine = new CommandLine(s, new ByteArrayInputStream(commands.getBytes()), true);
		new Thread(commandLine, "command line").start();
		 		
		while (s.isWaitingSearch()) {
			Thread.sleep(10);
		}
		
		s.stop();
		
		while (s.hasWorker()) {
			Thread.sleep(100);
		}
		
		// Time for workers socket timeout
		Thread.sleep(Communicator.SOCKET_TIMEOUT + 10);		
	}
	
	/**
	 * Test if the searching scheduler is working  
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException 
	 * @throws LimitExceededException 
	 */
	public void testOneThreadByWorker() throws IOException, InterruptedException, LimitExceededException {
		AbstractWorker w1 = new MockWorker(50000);
		AbstractWorker w2 = new MockWorker(50001);

		w1.setMaxSimultaneousSearchs(1);
		w2.setMaxSimultaneousSearchs(1);
		w1.start();		
		w2.start();
		
		while (!(w1.isReady() && w2.isReady())) {
			Thread.sleep(100);
		}
		
		String[] hosts = new String[] {"localhost:50000", "localhost:50001"};
		
		Dispatcher s = new Dispatcher(hosts);
		s.start();
		while (!s.isReady()) {
			Thread.sleep(100);
		}		
		
		String commands = "search db actgactg\n"+
				"check 1\n"+
				"read 1\n"+
				"search db gtcagctgcatgc\n"+
				"read 1\n"+
				"clean 1\n"+
				"read 2\n"+
				"search db gcgcgctatagcaa\n"+
				"search ax cctatatctctact\n"+
				"search bx actgcatgcatgca\n"+
				"search db aactgactgacatc\n"+
				"clean 2\n"+
				"exit";							
		
		CommandLine commandLine = new CommandLine(s, new ByteArrayInputStream(commands.getBytes()), true);
		new Thread(commandLine, "command line").start();
		 
		
		while (s.isWaitingSearch()) {
			Thread.sleep(10);
		}
		
		s.stop();
	}

}
