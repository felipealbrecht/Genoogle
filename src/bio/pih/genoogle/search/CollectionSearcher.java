package bio.pih.genoogle.search;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.biojava.bio.symbol.IllegalSymbolException;

import bio.pih.genoogle.io.AbstractSequenceDataBank;
import bio.pih.genoogle.io.DatabankCollection;
import bio.pih.genoogle.io.IndexedDNASequenceDataBank;
import bio.pih.genoogle.search.IndexRetrievedData.BothStrandSequenceAreas;
import bio.pih.genoogle.search.results.HSP;
import bio.pih.genoogle.search.results.Hit;
import bio.pih.genoogle.search.results.SearchResults;

import com.google.common.collect.Lists;

/**
 * A searcher that does search operation at each data bank of its collection.
 * 
 * @author albrecht
 * 
 */
public class CollectionSearcher extends AbstractSearcher {

	private static Logger logger = Logger.getLogger(CollectionSearcher.class.getName());

	private final DatabankCollection<AbstractSequenceDataBank> databankCollection;

	static Comparator<BothStrandSequenceAreas> AREAS_LENGTH_COMPARATOR = new Comparator<BothStrandSequenceAreas>() {
		@Override
		public int compare(final BothStrandSequenceAreas o1, final BothStrandSequenceAreas o2) {
			return o2.getSumLengths() - o1.getSumLengths();
		}
	};

	public CollectionSearcher(long code, SearchParams sp, DatabankCollection<AbstractSequenceDataBank> databank) {
		super(code, sp, databank);
		this.databankCollection = databank;
	}

	@Override
	public SearchResults call() {
		long begin = System.currentTimeMillis();
		List<Throwable> fails = Lists.newLinkedList();

		int indexSearchers = databankCollection.size();

		ExecutorService subDatabanksExecutor = Executors.newFixedThreadPool(indexSearchers);
		CompletionService<List<BothStrandSequenceAreas>> subDataBanksCS = new ExecutorCompletionService<List<BothStrandSequenceAreas>>(subDatabanksExecutor);

		ExecutorService queryExecutor = Executors.newFixedThreadPool(sp.getMaxThreadsIndexSearch());

		fails = Collections.synchronizedList(fails);
		Iterator<AbstractSequenceDataBank> it = databankCollection.databanksIterator();
		while (it.hasNext()) {
			AbstractSequenceDataBank innerBank = it.next();
			final DNAIndexBothStrandSearcher indexSearcher = new DNAIndexBothStrandSearcher(id, sp, (IndexedDNASequenceDataBank) innerBank, queryExecutor, fails);
			subDataBanksCS.submit(indexSearcher);
		}

		if (fails.size() > 0) {
			sr.addAllFails(fails);
			return sr;
		}

		List<BothStrandSequenceAreas> sequencesRetrievedAreas = null;
		try {
			sequencesRetrievedAreas = Lists.newLinkedList();
			for (int i = 0; i < indexSearchers; i++) {
				List<BothStrandSequenceAreas> list;
				list = subDataBanksCS.take().get();
				if (list == null) {
					logger.error("Results from searcher " + i + " was empty.");
				} else {
					sequencesRetrievedAreas.addAll(list);
				}
			}
		} catch (InterruptedException e) {
			sr.addFail(e);
			return sr;
		} catch (ExecutionException e) {
			sr.addFail(e);
			return sr;
		}

		queryExecutor.shutdown();
		subDatabanksExecutor.shutdown();

		logger.info("DNAIndexBothStrandSearcher total Time of " + this.toString() + " "
				+ (System.currentTimeMillis() - begin));

		long alignmentBegin = System.currentTimeMillis();

		Collections.sort(sequencesRetrievedAreas, AREAS_LENGTH_COMPARATOR);

		ExecutorService alignerExecutor = Executors.newFixedThreadPool(sp.getMaxThreadsExtendAlign());

		int maxHits = sp.getMaxHitsResults() > 0 ? sp.getMaxHitsResults() : sequencesRetrievedAreas.size();
		maxHits = Math.min(maxHits, sequencesRetrievedAreas.size());

		CountDownLatch alignnmentsCountDown = new CountDownLatch(maxHits);

		try {
			for (int i = 0; i < maxHits; i++) {
				BothStrandSequenceAreas retrievedArea = sequencesRetrievedAreas.get(i);
				SequenceAligner sequenceAligner = new SequenceAligner(alignnmentsCountDown, retrievedArea, sr);
				alignerExecutor.submit(sequenceAligner);
			}
		} catch (IOException e) {
			sr.addFail(e);
			return sr;
		} catch (IllegalSymbolException e) {
			sr.addFail(e);
			return sr;
		}

		try {
			alignnmentsCountDown.await();
		} catch (InterruptedException e) {
			sr.addFail(e);
			return sr;
		}

		alignerExecutor.shutdown();

		for (Hit hit : sr.getHits()) {
			Collections.sort(hit.getHSPs(), HSP.COMPARATOR);
		}

		Collections.sort(sr.getHits(), Hit.COMPARATOR);
		logger.info("Alignments total Time of " + this.toString() + " " + (System.currentTimeMillis() - alignmentBegin));
		logger.info("Total Time of " + this.toString() + " " + (System.currentTimeMillis() - begin));

		return sr;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Long.toString(id));
		sb.append(" CollectionSearcher ");
		return sb.toString();
	}
}