package bio.pih.tests.scheduler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.biojava.bio.dist.Distribution;
import org.biojava.bio.dist.DistributionTools;
import org.biojava.bio.dist.UniformDistribution;
import org.biojava.bio.seq.DNATools;

import bio.pih.scheduler.AbstractWorker;
import bio.pih.search.AlignmentResult;
import bio.pih.search.SearchStatus;
import bio.pih.search.SearchStatus.SearchStep;

/**
 * Mock class for unit tests
 * 
 * @author albrecht
 */
public class MockWorker extends AbstractWorker {

	volatile int threadsCount = 0;

	/**
	 * @param port
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public MockWorker(int port) {
		super(port);
	}

	@Override
	protected void doSearch(SearchStatus searchInformation) {
		new Thread(new Searcher(searchInformation), "Searcher at " + this.getIdentifier()).start();
	}

	private class Searcher implements Runnable {
		SearchStatus searchInformation;

		/**
		 * @param searchInformation
		 */
		public Searcher(SearchStatus searchInformation) {
			this.searchInformation = searchInformation;
		}

		public void run() {
			threadsCount++;
			
			do {
				doSearch();
			} while ((this.searchInformation = getNextSearchInformation(this.searchInformation)) != null);
			
			threadsCount--;
		}

		/**
		 * @param searchInformation
		 */
		public void doSearch() {
			try {
				System.out.println("Sao " + threadsCount + " threads");
				System.out.println("doing search: " + this.searchInformation);
				this.searchInformation.setActualStep(SearchStep.SEEDS);
				long sleepTime = Math.round(Math.random() * 300);
				Thread.sleep(sleepTime);

				this.searchInformation.setActualStep(SearchStep.ALIGNMENT);
				sleepTime = Math.round(Math.random() * 300);
				Thread.sleep(sleepTime);

				this.searchInformation.setActualStep(SearchStep.SELECTING);
				sleepTime = Math.round(Math.random() * 300);
				Thread.sleep(sleepTime);

				this.searchInformation.setActualStep(SearchStep.FINISHED);

				List<AlignmentResult> results = new LinkedList<AlignmentResult>();

				Distribution dist = new UniformDistribution(DNATools.getDNA());
				long seqs = Math.round(Math.random() * 10) + 1; // 1 to 11
				while (seqs-- > 0) {
					results.add(new AlignmentResult(DistributionTools.generateSequence("random seq " + seqs, dist, 700), (int) seqs * 2));
				}

				System.out.println("Finished " + searchInformation + " in " + getIdentifier());
				ResultSender resultSender = new ResultSender(searchInformation.getDb(), searchInformation.getQuery(), searchInformation.getCode(), results.toArray(new AlignmentResult[results.size()]));

				new Thread(resultSender).start();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
