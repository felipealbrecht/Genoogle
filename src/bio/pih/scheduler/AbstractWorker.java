package bio.pih.scheduler;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.naming.LimitExceededException;

import bio.pih.scheduler.communicator.Communicator;
import bio.pih.scheduler.communicator.WorkerCommunicator;
import bio.pih.scheduler.communicator.message.Message;
import bio.pih.scheduler.communicator.message.RequestMessage;
import bio.pih.scheduler.communicator.message.ResultMessage;
import bio.pih.search.SearchStatus;
import bio.pih.search.SearchParams;
import bio.pih.search.results.HSP;

/**
 * A interface that define a worker, or who will do the hard job
 * 
 * @author albrecht
 * 
 */
public abstract class AbstractWorker {

	int availableProcessors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
	private List<SearchStatus> waitingList; // search queue
	private List<SearchStatus> runningSearch;
	private WorkerCommunicator communicator;
	private int identifier;
	volatile private int maxSimultaneousSearch;

	/**
	 * @param port
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public AbstractWorker(int port) {
		this.communicator = new WorkerCommunicator(this, port);
		this.runningSearch = Collections.synchronizedList(new LinkedList<SearchStatus>());
		this.waitingList = Collections.synchronizedList(new LinkedList<SearchStatus>());
		this.maxSimultaneousSearch = availableProcessors;
	}

	/**
	 * Start the worker.
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		this.communicator.start();
	}

	/**
	 * Stop the worker.
	 * 
	 * <p>
	 * All searching will continue and no information will be lost but they will not send or receive data.
	 * <p>
	 * TODO: a allStop(), that will stop all search too.
	 * 
	 * @throws IOException
	 */
	public void stop() throws IOException {
		System.out.println("Stoping worker " + getIdentifier());
		this.communicator.stop();
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
	 * Return how many processors are available. Note: HyperThreading(HT) processor counts as 2
	 * 
	 * @return available processors
	 */
	public int getAvailableprocessors() {
		return availableProcessors;
	}

	/**
	 * Get the actual running searches
	 * 
	 * @return a <code>list</code> containing the running searches
	 */
	public List<SearchStatus> getRunningSearch() {
		return runningSearch;
	}

	/**
	 * Request a search.
	 * <p>
	 * This is a asynchronous call. When the search finish, the result will be sent to the scheduler.
	 * 
	 * @param params
	 * 
	 * TODO: move code into a search manager
	 */
	public void requestSearch(SearchParams sp) {
		SearchStatus si = null; // new SearchStatus(sp. );
		if (canSearchOrQueue(si) != null) {
			doSearch(si);
		} else {
			getWaitingList().add(si);
		}
	}

	/**
	 * @param searchInformation
	 */
	protected abstract void doSearch(SearchStatus searchInformation);

	/**
	 * @param m
	 *            TODO: Não usar {@link RequestMessage} e outros {@link Message}
	 */
	public void processMessage(Message m) {
		switch (m.getKind()) {
		case REQUEST:
			SearchParams sp = createSearchParams((RequestMessage) m);
			requestSearch(sp);
			break;

		default:
			return;
		}
	}

	private SearchParams createSearchParams(RequestMessage m) {
		return m.getSearchParams();
	}

	/**
	 * @return <code>true</code> if the worker is ready (Database, index and others stuffs loaded)
	 */
	public boolean isReady() {
		return communicator.isReady();
	}

	/**
	 * @return <code>true</code> if this worker is connected with the scheduler 
	 */
	public boolean isConnected() {
		return communicator.isConnected();
	}
	
	/**
	 * @return a {@link List} containing {@link SearchStatus} of all search that are waiting to be processed.
	 */
	public List<SearchStatus> getWaitingList() {
		return waitingList;
	}

	/**
	 * @return the amount of search that can be done concurrently
	 */
	public int getMaxSimultaneousSearch() {
		return maxSimultaneousSearch;
	}

	/**
	 * @param maxSimultaneousSearch
	 * @throws LimitExceededException when the value is lower than one
	 */
	public void setMaxSimultaneousSearchs(int maxSimultaneousSearch) throws LimitExceededException {
		if (maxSimultaneousSearch < 1) {
			throw new LimitExceededException("The maxSimultaneousSearch must be one or more");
		}
		this.maxSimultaneousSearch = maxSimultaneousSearch;
	}

	/**
	 * Check if a search can be performed or the information must be put in the queue
	 * 
	 * @param searchInformation
	 * @return {@link SearchStatus} if can search him or <code>null</code>.
	 */
	protected synchronized SearchStatus canSearchOrQueue(SearchStatus searchInformation) {
		if (getRunningSearch().size() < getMaxSimultaneousSearch()) {
			getRunningSearch().add(searchInformation);
			return searchInformation;
		}
		return null;
	}

	/**
	 * Get the next search to be performed and remove the last search did in atomic form.
	 * 
	 * @param lastSearch -
	 *            the least search did by this thread.
	 * @return the next {@link SearchStatus} in the queue
	 */
	protected synchronized SearchStatus getNextSearchInformation(SearchStatus lastSearch) {
		assert (getRunningSearch().remove(lastSearch) == true);

		if (getWaitingList().size() > 0) {
			SearchStatus searchInformation = getWaitingList().remove(0);
			getRunningSearch().add(searchInformation);
			return searchInformation;
		}
		return null;
	}

	/**
	 * Send the search result to the scheduler.
	 */
	protected class ResultSender implements Runnable {
		private String db;
		private String query;
		private long code;
		private HSP[] alignmentResult;

		/**
		 * @param db
		 * @param query
		 * @param code
		 * @param alignemtnResult
		 *            a
		 */
		public ResultSender(String db, String query, long code, HSP[] alignemtnResult) {
			this.db = db;
			this.query = query;
			this.code = code;
			this.alignmentResult = alignemtnResult;
		}

		public void run() {
			try {
				communicator.sendMessage(new ResultMessage(db, query, code, alignmentResult));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
