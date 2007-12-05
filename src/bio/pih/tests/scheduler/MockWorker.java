package bio.pih.tests.scheduler;

import java.io.IOException;

import bio.pih.scheduler.AbstractWorker;
import bio.pih.search.SearchInformation;
import bio.pih.search.SearchResult;
import bio.pih.search.SearchInformation.Step;

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
	public MockWorker(int port) throws IOException, ClassNotFoundException {
		super(port);
	}

	@Override
	protected void doSearch(SearchInformation searchInformation) {
		new Thread(new Searcher(searchInformation)).start();
	}

	private class Searcher implements Runnable {
		SearchInformation searchInformation;

		/**
		 * @param searchInformation
		 */
		public Searcher(SearchInformation searchInformation) {
			this.searchInformation = searchInformation;
		}

		@Override
		public void run() {
			threadsCount++;
			System.out.println("Sao " + threadsCount + " threads");
			doSearch();
			while ((this.searchInformation = getNextSearchInformation()) != null) {				
				doSearch();
			}
			threadsCount--;
		}

		/**
		 * @param searchInformation
		 */
		public void doSearch() {
			try {		
				System.out.println("doing search: " + this.searchInformation);
				this.searchInformation.setActualStep(Step.SEEDS);
				long sleepTime = Math.round(Math.random() * 1000);
				Thread.sleep(sleepTime);

				this.searchInformation.setActualStep(Step.ALIGNMENT);
				sleepTime = Math.round(Math.random() * 1000);
				Thread.sleep(sleepTime);

				this.searchInformation.setActualStep(Step.SELECTING);
				sleepTime = Math.round(Math.random() * 1000);
				Thread.sleep(sleepTime);

				this.searchInformation.setActualStep(Step.FINISHED);
				SearchResult searchResult = new SearchResult();

				getRunningSearch().remove(searchInformation);
				getSearchResult().add(searchResult);
				System.out.println("Finished " + searchInformation + " in " + getIdentifier() );
				// TODO spam a new thread to alert for send the results

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
