package bio.pih.scheduler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import bio.pih.scheduler.communicator.Communicator;
import bio.pih.scheduler.communicator.SchedulerCommunicator;
import bio.pih.scheduler.communicator.WorkerInfo;
import bio.pih.scheduler.communicator.message.Message;
import bio.pih.scheduler.communicator.message.RequestMessage;
import bio.pih.scheduler.communicator.message.ResultMessage;
import bio.pih.search.results.HSP;

/**
 * The Scheduler!
 * 
 * @author albrecht
 */
public class Scheduler {
	private volatile long totalSearchs;
	private List<WorkerInfo> workers;
	private final String[] workerAddress;
	private Communicator communicator;
	private List<Searching> waitingList;
	private Map<Long, Searching> searches;

	/**
	 * @param workerAddress
	 *            a array of String in the "address:port" format.
	 */
	public Scheduler(String[] workerAddress) {
		this.workerAddress = workerAddress.clone(); 
		communicator = new SchedulerCommunicator(this);
		this.workers = Collections.synchronizedList(new LinkedList<WorkerInfo>());
		waitingList = Collections.synchronizedList(new LinkedList<Searching>());
		searches = Collections.synchronizedMap(new HashMap<Long, Searching>());

		
	}

	/**
	 * @throws IOException
	 */
	public void start() throws IOException {
		communicator.start();
	}

	/**
	 * Return a <code>List</code> of workers connected
	 * 
	 * @return <code>List</code> of workers connected
	 */
	public List<WorkerInfo> getWorkers() {
		return workers;
	}

	/**
	 * Remove a worker from this scheduler.
	 * 
	 * @param worker
	 * @return <code>true</code> if the worker was removed
	 */
	public boolean removeWorker(WorkerInfo worker) {
		synchronized (this.getWorkers()) {
			return this.getWorkers().remove(worker);			
		}
	}

	/**
	 * @return a <code>Array</code> containing the workers addresses.
	 */
	public String[] getWorkerAddress() {
		return workerAddress.clone();
	}
	
	/**
	 * @return all the {@link Searching} that are being processed or still save
	 */
	public Map<Long, Searching> getSearches() {
		return searches;
	}

	/**
	 * Inform if the scheduler communicator is ready.
	 * 
	 * @return <code>true</code> if the communicator is ready.
	 */
	public boolean isReady() {
		return communicator.isReady();
	}

	/**
	 * @return if the this scheduler has some worker.
	 */
	public boolean hasWorker() {
		return getWorkers().size() > 0;
	}

	/**
	 * @param workerIdentifier
	 * @param message
	 */
	public void processMessage(int workerIdentifier, Message message) {
		System.out.println("mensagem de " + workerIdentifier + " processando: " + message);
		switch (message.getKind()) {
		case RESULT:
			processResultMessage(workerIdentifier, (ResultMessage) message);
			break;
		default:
			System.out.println("fudeu, nao deveria estar aqui! :-(");
		}
	}

	/**
	 * Execute a search.
	 * <p>
	 * It is an asynchronous call. The result must be checked with
	 * 
	 * <p>
	 * <b>TODO</b>: Expand the argument list for the future options, like the blosum matrix. <br>
	 * <b>TODO</b>: Substitute the <code>int</code> for a object that represents the call, may be this object can handle its "getSearchResult".
	 * 
	 * @param database
	 * @param query
	 * 
	 * @return the identifier of the result;
	 * @throws IOException
	 */
	public Searching doSearch(String database, String query) throws IOException {
		totalSearchs++;
		Searching searching = new Searching(totalSearchs, database, query, getWorkers().size());
		getSearches().put(totalSearchs, searching);
		waitingList.add(searching);

		communicator.sendMessage(new RequestMessage(database, query, totalSearchs));

		return searching;
	}

	/**
	 * @param workerIdentifier
	 * @param message
	 */
	public synchronized void processResultMessage(int workerIdentifier, ResultMessage message) {
		Searching searching = getSearches().get(message.getCode());
		searching.addPartialResult(workerIdentifier, message);

		if (searching.isDone()) {
			int actualPos = 0;
			HSP[] alignments;
			alignments = new HSP[searching.getTotalAlignments()];
			for (ResultMessage partialResults : searching.getPartialResults()) {
				System.arraycopy(partialResults.getAlignments(), 0, alignments, actualPos, partialResults.getAlignments().length);
				actualPos += partialResults.getAlignments().length;
			}

			Arrays.sort(alignments, new Comparator<HSP>() {
				public int compare(HSP o1, HSP o2) {
					return o2.getPontuation() - o1.getPontuation();
				};
			});

			searching.setAlignments(alignments);
			getWaitingList().remove(searching);
		}
	}

	/**
	 * @return a list of searches that are not completed yet.
	 */
	public List<Searching> getWaitingList() {
		return waitingList;
	}

	/**
	 * Is scheduler waiting for some search?
	 * 
	 * @return <code>true</code> if it is, <code>false</code> otherwise.
	 */
	public boolean isWaitingSearch() {
		return this.getWaitingList().size() > 0 ? true : false;
	}

	/**
	 * Stop the scheduler.
	 * 
	 * @throws IOException
	 */
	public void stop() throws IOException {
		communicator.stop();
	}

	/**
	 * @author albrecht 
	 */
	public static class Searching {
		volatile int remainsResult;
		HSP[] alignments;
		ResultMessage[] partialResults;
		String database;
		String query;
		long startTime;
		long code;
		int totalAlignments;

		/**
		 * @param code
		 * @param database
		 * @param query
		 * @param qtdWorker
		 */
		public Searching(long code, String database, String query, int qtdWorker) {
			this.database = database;
			this.query = query;
			this.startTime = System.currentTimeMillis();
			this.partialResults = new ResultMessage[qtdWorker];
			this.remainsResult = qtdWorker;
			this.code = code;
			this.totalAlignments = 0;
		}

		/**
		 * @param worker
		 * @param resultMessage
		 */
		public synchronized void addPartialResult(int worker, ResultMessage resultMessage) {
			assert (partialResults[worker - 1] == null);
			partialResults[worker - 1] = resultMessage;
			totalAlignments += resultMessage.getAlignments().length;
			remainsResult--;
		}

		/**
		 * Verify if this search finished.
		 * 
		 * @return <code>true</code> if it finished.
		 */
		public boolean isDone() {
			return remainsResult == 0;
		}

		/**
		 * @return the code of this search
		 */
		public long getCode() {
			return code;
		}

		/**
		 * @return the {@link ResultMessage} received from the {@link AbstractWorker} for this search 
		 */
		public ResultMessage[] getPartialResults() {
			return partialResults.clone();
		}

		/**
		 * @return the total of alignments found for this search
		 */
		public int getTotalAlignments() {
			return totalAlignments;
		}
		
		/**
		 * Set the alignments merged and sorted 
		 * @param alignments
		 */
		public void setAlignments(HSP[] alignments) {			
			this.alignments = alignments.clone(); 
		}
		
		/**
		 *  Get the alignments from this search
		 * @return The alignments merged and sorted
		 */
		public HSP[] getAlignments() {
			return alignments.clone();
		}
		
		@Override
		public String toString() {		
			return database + " " + query + " " + startTime;
		}
	}

}
