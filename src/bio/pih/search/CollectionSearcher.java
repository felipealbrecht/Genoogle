package bio.pih.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import bio.pih.io.DatabankCollection;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.SequenceDataBank;
import bio.pih.search.IndexRetrievedData.SequenceRetrievedAreas;
import bio.pih.search.results.HSP;
import bio.pih.search.results.Hit;
import bio.pih.search.results.SearchResults;

/**
 * A searcher that does search operation at each data bank of its collection.
 * 
 * @author albrecht
 * 
 */
public class CollectionSearcher extends AbstractSearcher {

	static Logger logger = Logger.getLogger(CollectionSearcher.class.getName());
	private final DatabankCollection<SequenceDataBank> databankCollection;

	public CollectionSearcher(long code, SearchParams sp,
			DatabankCollection<SequenceDataBank> databank, ExecutorService executor) {
		super(code, sp, databank, executor);
		this.databankCollection = databank;
	}

	@Override
	public SearchResults call() {
		long begin = System.currentTimeMillis();

		int indexSearchers = databankCollection.size() * 2;
		// scatter the results of the index searcher into this array. 
		IndexRetrievedData[] irds = new IndexRetrievedData[indexSearchers];
		CountDownLatch searchersCountDown = new CountDownLatch(indexSearchers);

		int pos = 0;
		Iterator<SequenceDataBank> it = databankCollection.databanksIterator();
		while (it.hasNext()) {
			SequenceDataBank innerBank = it.next();
			final DNAIndexBothStrandSearcher indexSearcher = new DNAIndexBothStrandSearcher(id, sp,
					(IndexedDNASequenceDataBank) innerBank, executor, searchersCountDown, irds, pos++);
			executor.submit(indexSearcher);
		}

		try {
			searchersCountDown.await();
		} catch (InterruptedException e1) {
			sr.addFail(e1);
			return sr;
		}

		int totalAreas = 0;
		long totalSumAreas = 0;
		int totalSequences = 0;
		for (IndexRetrievedData indexRetrievedData : irds) {
			totalAreas += indexRetrievedData.getTotalAreas();
			totalSumAreas += indexRetrievedData.getSumLength();
			totalSequences += indexRetrievedData.getTotalSequences();
		}

		ArrayList<SequenceRetrievedAreas> sequencesRetrievedAreas = new ArrayList<SequenceRetrievedAreas>(
				totalSequences);

		for (IndexRetrievedData indexRetrievedData : irds) {
			sequencesRetrievedAreas.addAll(indexRetrievedData.getSequencesRetrievedAreas());
		}

		Collections.sort(sequencesRetrievedAreas, new Comparator<SequenceRetrievedAreas>() {
			@Override
			public int compare(SequenceRetrievedAreas o1, SequenceRetrievedAreas o2) {
				return o2.getSumLengths() - o1.getSumLengths();
			}
		});

		System.out.println(totalSequences);
		System.out.println(totalAreas);
		System.out.println(totalSumAreas);

		int MAX = sp.getMaxHitsResults()>0?sp.getMaxHitsResults():sequencesRetrievedAreas.size();

		CountDownLatch alignnmentsCountDown = new CountDownLatch(MAX);
		try {
			for (int i = 0; i < MAX; i++) {
				SequenceRetrievedAreas retrievedArea = sequencesRetrievedAreas.get(i);
				SequenceAligner sequenceAligner;
				sequenceAligner = new SequenceAligner(alignnmentsCountDown, retrievedArea, sr);
				executor.submit(sequenceAligner);
			}
			alignnmentsCountDown.await();
		} catch (Exception e) {
			sr.addFail(e);
			return sr;
		}

		for (Hit hit : sr.getHits()) {
			Collections.sort(hit.getHSPs(), HSP.COMPARATOR);
		}

		Collections.sort(sr.getHits(), Hit.COMPARATOR);

		logger
				.info("Total Time of " + this.toString() + " "
						+ (System.currentTimeMillis() - begin));

		return sr;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Long.toString(id));
		sb.append(" CollectionSearcher ");
		return sb.toString();
	}
}