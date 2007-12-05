package bio.pih.scheduler;

import java.io.IOException;
import java.util.List;

import bio.pih.scheduler.communicator.Communicator;
import bio.pih.scheduler.communicator.WorkerCommunicator;
import bio.pih.scheduler.communicator.message.Message;
import bio.pih.search.SearchInformation;
import bio.pih.search.SearchParams;
import bio.pih.search.SearchResult;

/**
 * A interface that define a worker, or who will do the hard job
 * @author albrecht
 *
 */
public class Worker {
	
	List<SearchInformation> runningSearches;
	Communicator communicator;
	int identifier;
	
	/**
	 * @param port
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public Worker(int port) throws IOException, ClassNotFoundException {
		this.communicator = new WorkerCommunicator(this,port);
	}
	
	/**
	 * Start the worker.
	 * @throws IOException 
	 */
	public void start() throws IOException {
		this.communicator.start();
	}
	
	/**
	 * @return the integer that identifier the worker.
	 */
	public int getIdentifier() {
		return this.identifier;
	}
	
	/**
	 * @param identifier
	 */
	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * @return the communicator
	 */
	public Communicator getCommunicator() {
		return communicator;
	}
		
	/**
	 * Get the actual running searches
	 * @return a <code>list</code> contening the running searches
	 */	
	public List<SearchInformation> getRunningSearches() {
		return runningSearches;
	}
	
	/**
	 * Do a search
	 * @param params
	 * @return
	 */
	public SearchResult doSearch(SearchParams params) {
		return null;
	}

	/**
	 * @param m
	 */
	public void processMessage(Message m) {
		System.out.println("Processing " + m);		
	}
	
	/**
	 * @return <code> if the worker is ready (Database, index and others stuffs loaded)
	 */
	public boolean isReady() {
		return communicator.isReady();
	}
	
}
