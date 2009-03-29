package bio.pih.search;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import bio.pih.io.DatabankCollection;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.SequenceDataBank;
import bio.pih.search.IndexRetrievedData.BothStrandSequenceAreas;
import bio.pih.search.results.HSP;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;

import com.google.common.collect.Lists;

/**
 * A searcher that does search operation at each data bank of its collection.
 * 
 * @author albrecht
 * 
 */
public class CollectionSearcher extends AbstractSearcher {

	private static Logger logger = Logger.getLogger(CollectionSearcher.class.getName());

	private final DatabankCollection<SequenceDataBank> databankCollection;

	public CollectionSearcher(long code, SearchParams sp,
			DatabankCollection<SequenceDataBank> databank) {
		super(code, sp, databank);
		this.databankCollection = databank;
	}

	@Override
	public SearchResults call() {
		long begin = System.currentTimeMillis();
		List<Exception> fails = Lists.newLinkedList();
		fails = Collections.synchronizedList(fails);
		try {
			int indexSearchers = databankCollection.size();

			Iterator<SequenceDataBank> it = databankCollection.databanksIterator();
			ExecutorService subDatabanksExecutor = Executors.newFixedThreadPool(indexSearchers);
			CompletionService<List<BothStrandSequenceAreas>> subDataBanksCS = new ExecutorCompletionService<List<BothStrandSequenceAreas>>(
					subDatabanksExecutor);

			ExecutorService queryExecutor = Executors.newFixedThreadPool(sp
					.getMaxThreadsIndexSearch());

			while (it.hasNext()) {
				SequenceDataBank innerBank = it.next();
				final DNAIndexBothStrandSearcher indexSearcher = new DNAIndexBothStrandSearcher(id,
						sp, (IndexedDNASequenceDataBank) innerBank, queryExecutor, fails);
				subDataBanksCS.submit(indexSearcher);
			}

			if (fails.size() > 0) {
				sr.addAllFails(fails);
				return sr;
			}

			List<BothStrandSequenceAreas> sequencesRetrievedAreas = Lists.newLinkedList();
			for (int i = 0; i < indexSearchers; i++) {
				List<BothStrandSequenceAreas> list = subDataBanksCS.take().get();
				if (list == null) {
					logger.error("Results from searcher " + i + " was empty.");
				} else {
					sequencesRetrievedAreas.addAll(list);
				}
			}

			subDatabanksExecutor.shutdown();

			logger.info("DNAIndexBothStrandSearcher total Time of " + this.toString() + " "
					+ (System.currentTimeMillis() - begin));

			long alignmentBegin = System.currentTimeMillis();

			Collections.sort(sequencesRetrievedAreas, new Comparator<BothStrandSequenceAreas>() {
				@Override
				public int compare(BothStrandSequenceAreas o1, BothStrandSequenceAreas o2) {
					return o2.getSumLengths() - o1.getSumLengths();
				}
			});

			ExecutorService alignerExecutor = Executors.newFixedThreadPool(sp
					.getMaxThreadsIndexSearch());

			int maxHits = sp.getMaxHitsResults() > 0 ? sp.getMaxHitsResults()
					: sequencesRetrievedAreas.size();
			maxHits = Math.min(maxHits, sequencesRetrievedAreas.size());

			CountDownLatch alignnmentsCountDown = new CountDownLatch(maxHits);

			for (int i = 0; i < maxHits; i++) {
				BothStrandSequenceAreas retrievedArea = sequencesRetrievedAreas.get(i);
				SequenceAligner sequenceAligner;
				sequenceAligner = new SequenceAligner(alignnmentsCountDown, retrievedArea, sr);
				alignerExecutor.submit(sequenceAligner);
			}
			alignnmentsCountDown.await();
			alignerExecutor.shutdown();

			for (Hit hit : sr.getHits()) {
				Collections.sort(hit.getHSPs(), HSP.COMPARATOR);
			}

			Collections.sort(sr.getHits(), Hit.COMPARATOR);
			logger.info("Alignments total Time of " + this.toString() + " "
					+ (System.currentTimeMillis() - alignmentBegin));
			logger.info("Total Time of " + this.toString() + " "
					+ (System.currentTimeMillis() - begin));

		} catch (Exception e) {
			sr.addFail(e);
		}

		return sr;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Long.toString(id));
		sb.append(" CollectionSearcher ");
		return sb.toString();
	}
}