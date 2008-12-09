package bio.pih.search;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import bio.pih.io.IndexedDNASequenceDataBank;

public class DNAIndexBothStrandSearcher implements Callable<IndexRetrievedData[]> {

	private DNAIndexSearcher searcher;
	private DNAIndexSearcher complementInvertedSearcher;

	private static final Logger logger = Logger.getLogger(DNAIndexBothStrandSearcher.class
			.getName());
	private final long id;
	private final SearchParams sp;
	private final IndexedDNASequenceDataBank databank;
	private final CountDownLatch countDown;
	private final ExecutorService executor;
	private final IndexRetrievedData[] retrievedDatas;
	private final int pos;

	public DNAIndexBothStrandSearcher(long id, SearchParams sp,	IndexedDNASequenceDataBank databank, 
			ExecutorService executor, CountDownLatch countDown,
			IndexRetrievedData[] retrievedDatas, int pos) {
		this.id = id;
		this.sp = sp;
		this.databank = databank;
		this.executor = executor;
		this.countDown = countDown;
		this.retrievedDatas = retrievedDatas;
		this.pos = pos;		
	}

	@Override
	public IndexRetrievedData[] call() throws Exception {
		long begin = System.currentTimeMillis();

		searcher = new DNAIndexSearcher(id, sp, databank, countDown, retrievedDatas, pos * 2);
		complementInvertedSearcher = new DNAIndexReverseComplementSearcher(id, sp, databank, countDown, retrievedDatas, (pos * 2) + 1);
		
		executor.submit(searcher);
		executor.submit(complementInvertedSearcher);		
				
		logger.info("Total Time of " + this.toString() + " "
						+ (System.currentTimeMillis() - begin));

		return retrievedDatas;
	}
}
