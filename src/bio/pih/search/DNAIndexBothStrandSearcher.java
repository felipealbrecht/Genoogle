package bio.pih.search;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.search.IndexRetrievedData.BothStrandSequenceAreas;
import bio.pih.search.IndexRetrievedData.RetrievedArea;

public class DNAIndexBothStrandSearcher implements Callable<List<BothStrandSequenceAreas>> {

	private DNAIndexSearcher searcher;
	private DNAIndexReverseComplementSearcher complementInvertedSearcher;

	private static final Logger logger = Logger.getLogger(DNAIndexBothStrandSearcher.class
			.getName());
	private final long id;
	private final SearchParams sp;
	private final IndexedDNASequenceDataBank databank;
	private final CountDownLatch countDown;
	private final ExecutorService executor;
	private final List<BothStrandSequenceAreas>[] retrievedDatas;
	private final int pos;

	public DNAIndexBothStrandSearcher(long id, SearchParams sp,	IndexedDNASequenceDataBank databank, 
			ExecutorService executor, CountDownLatch countDown,
			List<BothStrandSequenceAreas>[] retrievedDatas, int pos) {
		this.id = id;
		this.sp = sp;
		this.databank = databank;
		this.executor = executor;
		this.countDown = countDown;
		this.retrievedDatas = retrievedDatas;
		this.pos = pos;		
	}

	@Override
	public List<BothStrandSequenceAreas> call() throws Exception {
		long begin = System.currentTimeMillis();

		searcher = new DNAIndexSearcher(id, sp, databank);
		complementInvertedSearcher = new DNAIndexReverseComplementSearcher(id, sp, databank);
		
		Future<List<RetrievedArea>[]> submit = executor.submit(searcher);
		Future<List<RetrievedArea>[]> submit2 = executor.submit(complementInvertedSearcher);
		
		List<RetrievedArea>[] indexRetrievedData = submit.get();
		List<RetrievedArea>[] indexRetrievedData2 = submit2.get();		
		
		List<BothStrandSequenceAreas> results = Lists.newLinkedList();
		
		int numberOfSequences = databank.getNumberOfSequences();
		for (int i = 0; i < numberOfSequences; i++) {
			List<RetrievedArea> areas1 = indexRetrievedData[i];
			List<RetrievedArea> areas2 = indexRetrievedData2[i];
			
			if (areas1 != null||areas2 != null) {
				BothStrandSequenceAreas retrievedAreas = new BothStrandSequenceAreas(i,
						searcher, complementInvertedSearcher, 
						areas1, areas2);
				results.add(retrievedAreas);				
			}						
		}
		
		retrievedDatas[pos] = results;	
				
		logger.info("Total Time of " + this.toString() + " "
						+ (System.currentTimeMillis() - begin));
		
		countDown.countDown();
		return results;
	}
}
