package bio.pih.search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.Utils;
import bio.pih.search.IndexRetrievedData.BothStrandSequenceAreas;
import bio.pih.search.IndexRetrievedData.RetrievedArea;
import bio.pih.seq.LightweightSymbolList;
import bio.pih.statistics.Statistics;

import com.google.common.collect.Lists;

public class DNAIndexBothStrandSearcher implements Callable<List<BothStrandSequenceAreas>> {

	private DNAIndexSearcher searcher;
	private DNAIndexReverseComplementSearcher crSearcher;

	private static final Logger logger = Logger.getLogger(DNAIndexBothStrandSearcher.class.getName());
	private final long id;
	private final SearchParams sp;
	private final IndexedDNASequenceDataBank databank;
	private final List<RetrievedArea>[] retrievedAreas;
	private final List<RetrievedArea>[] rcRetrievedAreas;
	private final List<Throwable> fails;
	private final ExecutorService executor;

	@SuppressWarnings("unchecked")
	public DNAIndexBothStrandSearcher(long id, SearchParams sp, IndexedDNASequenceDataBank databank,
			ExecutorService executor, List<Throwable> fails) {
		this.id = id;
		this.sp = sp;
		this.databank = databank;
		this.executor = executor;
		this.fails = fails;
		int numberOfSequences = databank.getNumberOfSequences();
		this.retrievedAreas = new ArrayList[numberOfSequences];
		this.rcRetrievedAreas = new ArrayList[numberOfSequences];
		for (int i = 0; i < numberOfSequences; i++) {
			retrievedAreas[i] = new ArrayList<RetrievedArea>(0);
			rcRetrievedAreas[i] = new ArrayList<RetrievedArea>(0);
		}
	}

	@Override
	public List<BothStrandSequenceAreas> call() throws BioException, InterruptedException {
		long searchBegin = System.currentTimeMillis();

		SymbolList query = sp.getQuery();

		Statistics statistics = new Statistics(sp.getMatchScore(), sp.getDismatchScore(), query, databank.getTotalDataBaseSize(), databank.getTotalNumberOfSequences(), sp.getMinEvalue());

		String seqString = query.seqString();

		int subSequenceLength = databank.getSubSequenceLength();
		DNASequenceEncoderToInteger encoder = DNASequenceEncoderToInteger.getEncoder(subSequenceLength);

		int[] encodedQuery = encoder.encodeSymbolListToIntegerArray(query);
		String inverted = Utils.invert(query.seqString());
		String rcString = Utils.sequenceComplement(inverted);

		SymbolList rcQuery = null;
		rcQuery = LightweightSymbolList.createDNA(rcString);
		int[] rcEncodedQuery = encoder.encodeSymbolListToIntegerArray(rcQuery);

		int length = query.length();

		int querySplitQuantity = sp.getQuerySplitQuantity();
		int minLength = sp.getMinQuerySliceLength();
		int sliceSize = length / querySplitQuantity;

		while (sliceSize < minLength && querySplitQuantity != 1) {
			querySplitQuantity--;
			sliceSize = length / querySplitQuantity;
		}

		CountDownLatch indexSearchersCountDown = new CountDownLatch(querySplitQuantity * 2);

		logger.info("(" + id + ") Preprocessing time: " + (System.currentTimeMillis() - searchBegin));
		logger.info("(" + id + ") " + querySplitQuantity + " threads with slice query with " + length + " bases.");
		for (int i = 0; i < querySplitQuantity; i++) {
			int begin = (sliceSize * i);
			int end = (sliceSize * i) + sliceSize + (statistics.getMinLengthDropOut() - subSequenceLength);
			if (end > length) {
				end = length;
			}
			logger.info("(" + id + ") " + i + " [" + begin + " - " + end + "].");
			String sliceQuery = seqString.substring(begin, end);
			String rcSliceQuery = rcString.substring(begin, end);
			submitSearch(sliceQuery, begin, query, encodedQuery, statistics, indexSearchersCountDown);
			submitRCSearch(rcSliceQuery, begin, rcQuery, rcEncodedQuery, statistics, indexSearchersCountDown);
		}

		indexSearchersCountDown.await();

		if (fails.size() > 0) {
			return null;
		}

		List<BothStrandSequenceAreas> results = Lists.newLinkedList();

		int numberOfSequences = databank.getNumberOfSequences();
		for (int i = 0; i < numberOfSequences; i++) {
			List<RetrievedArea> areas1 = retrievedAreas[i];
			List<RetrievedArea> areas2 = rcRetrievedAreas[i];

			if (areas1.size() > 0 || areas2.size() > 0) {
				BothStrandSequenceAreas retrievedAreas = new BothStrandSequenceAreas(i, searcher, crSearcher, areas1, areas2);
				results.add(retrievedAreas);
			}
		}

		logger.info("(" + id + ") " + "Index search time: " + (System.currentTimeMillis() - searchBegin));

		return results;
	}

	private void submitSearch(String sliceQuery, int offset, SymbolList fullQuery, int[] encodedQuery,
			Statistics statistics, CountDownLatch countDown) throws BioException {
		searcher = new DNAIndexSearcher(id, sp, databank, sliceQuery, offset, fullQuery, encodedQuery, retrievedAreas, statistics, countDown, fails);
		executor.submit(searcher);
	}

	private void submitRCSearch(String sliceQuery, int offset, SymbolList fullQuery, int[] encodedQuery,
			Statistics statistics, CountDownLatch countDown) throws BioException {
		crSearcher = new DNAIndexReverseComplementSearcher(id, sp, databank, sliceQuery, offset, fullQuery, encodedQuery, rcRetrievedAreas, statistics, countDown, fails);
		executor.submit(crSearcher);
	}
}
