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
import bio.pih.search.AlignmentResult;

/**
 * The Scheduler!
 * 
 * @author albrecht
 */
public class Scheduler {
	private volatile int totalSearchs;
	private List<WorkerInfo> workers;
	private String[] workerAddress;
	private Communicator communicator;
	private List<Searching> waitingList;
	private Map<Integer, Searching> searches;

	/**
	 * @param workerAddress
	 *            a array of String in the "address:port" format.
	 */
	public Scheduler(String[] workerAddress) {
		this.workerAddress = workerAddress;
		this.workers = new LinkedList<WorkerInfo>();
		communicator = new SchedulerCommunicator(this);
		waitingList = Collections.synchronizedList(new LinkedList<Searching>());
		searches = Collections.synchronizedMap(new HashMap<Integer, Searching>());

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
	 * @return a <code>Array</code> containing the workers addresses.
	 */
	public String[] getWorkerAddress() {
		return workerAddress;
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
		searches.put(totalSearchs, searching);
		waitingList.add(searching);

		communicator.sendMessage(new RequestMessage(database, query, totalSearchs));

		return searching;
	}

	/**
	 * @param workerIdentifier
	 * @param message
	 */
	public synchronized void processResultMessage(int workerIdentifier, ResultMessage message) {
		Searching searching = searches.get(message.getCode());
		searching.addPartialResult(workerIdentifier, message);

		if (searching.isDone()) {
			int actualPos = 0;
			AlignmentResult[] alignments;
			alignments = new AlignmentResult[searching.getTotalAlignments()];
			for (ResultMessage partialResults : searching.getPartialResults()) {
				System.arraycopy(partialResults.getAlignments(), 0, alignments, actualPos, partialResults.getAlignments().length);
				actualPos += partialResults.getAlignments().length;
			}

			Arrays.sort(alignments, new Comparator<AlignmentResult>() {
				public int compare(AlignmentResult o1, AlignmentResult o2) {
					return o2.getPontuation() - o1.getPontuation();
				};
			});
			
			// TODO: Report the alignments!

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
	 * @author albrecht TODO: verify if it have concurrently access, for synchronize it
	 */
	public class Searching {
		volatile int remainsResult;
		ResultMessage[] partialResults;
		String database;
		String query;
		long startTime;
		int code;
		int totalAlignments;

		/**
		 * @param code
		 * @param database
		 * @param query
		 * @param qtdWorker
		 */
		public Searching(int code, String database, String query, int qtdWorker) {
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
		public int getCode() {
			return code;
		}

		/**
		 * @return
		 */
		public ResultMessage[] getPartialResults() {
			return partialResults;
		}

		/**
		 * @return
		 */
		public int getTotalAlignments() {
			return totalAlignments;
		}
	}

}
