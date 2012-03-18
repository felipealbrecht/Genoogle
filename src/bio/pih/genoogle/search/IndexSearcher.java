/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009, 2010, 2011, 2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.search;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import bio.pih.genoogle.alignment.GenoogleSequenceAlignment;
import bio.pih.genoogle.encoder.MaskEncoder;
import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.index.ValueOutOfBoundsException;
import bio.pih.genoogle.io.IndexedSequenceDataBank;
import bio.pih.genoogle.search.IndexRetrievedData.RetrievedArea;
import bio.pih.genoogle.search.results.HSP;
import bio.pih.genoogle.seq.SymbolList;
import bio.pih.genoogle.statistics.Statistics;

import com.google.common.collect.Lists;

/**
 * Interface witch defines methods for search for similar sequences and checks.
 * 
 * @author albrecht
 */
public class IndexSearcher implements Runnable {

	private static final Logger logger = Logger.getLogger(IndexSearcher.class.getName());

	protected final long id;
	protected final SearchParams sp;
	protected final SequenceEncoder encoder;
	protected final IndexedSequenceDataBank databank;
	private final Statistics statistics;
	private final List<RetrievedArea>[] retrievedAreas;
	private final CountDownLatch countDown;

	private final int subSequenceLength;
	private final SymbolList fullQuery;
	private final int offset;
	private final int[] encodedQuery;
	private final String sliceQuery;
	
	private final int frame;

	private final List<Throwable> fails;

	public IndexSearcher(long id, SearchParams sp, IndexedSequenceDataBank databank, SequenceEncoder encoder, int subSequenceLength, String sliceQuery,
			int offset, SymbolList fullQuery, int[] encodedQuery, List<RetrievedArea>[] retrievedAreas, int frame,
			Statistics statistics, CountDownLatch countDown, List<Throwable> fails) {
		this.id = id;
		this.sp = sp;
		this.databank = databank;
		this.sliceQuery = sliceQuery;
		this.offset = offset;
		this.fullQuery = fullQuery;
		this.encodedQuery = encodedQuery;
		this.retrievedAreas = retrievedAreas;
		this.frame = frame;
		this.statistics = statistics;
		this.countDown = countDown;
		this.fails = fails;
		this.encoder = encoder;
		this.subSequenceLength = subSequenceLength;
	}

	public IndexSearcher(long id, SearchParams sp, IndexedSequenceDataBank databank, String sliceQuery,
			int offset, SymbolList fullQuery, int[] encodedQuery, List<RetrievedArea>[] retrievedAreas, int frame,
			Statistics statistics, CountDownLatch countDown, List<Throwable> fails) {
		
		this(id, sp, databank, databank.getEncoder(),
				databank.getMaskEncoder() == null ? databank.getSubSequenceLength() : databank.getMaskEncoder().getPatternLength(),
				sliceQuery, offset, fullQuery, encodedQuery, retrievedAreas, frame,				
				statistics, countDown, fails);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Long.toString(id));
		sb.append(" (direct) ");
		return sb.toString();
	}

	@Override
	public void run() {
		try {
			int queryLength = sliceQuery.length();
			if (queryLength < subSequenceLength) {
				throw new RuntimeException("Sequence: \"" + sliceQuery + "\" is too short. Its length is "
						+ queryLength + " but should to be at least " + subSequenceLength + ".");
			}

			logger.info("[" + this.toString() + "] Begining the search at " + databank.getName()
					+ " with the sequence with " + sliceQuery.length() + "bases and min subSequenceLength >= "
					+ this.sp.getMinHspLength());

			int[] iess = getEncodedSubSequences(sliceQuery, databank.getMaskEncoder());

			long init = System.currentTimeMillis();
			IndexRetrievedData retrievedData = getIndexPositions(iess, offset);

			retrievedData.finish();

			List<RetrievedArea>[] retrievedAreasArray = retrievedData.getRetrievedAreasArray();

			int totalHits = 0;
			final int length = retrievedAreasArray.length;

			for (int i = 0; i < length; i++) {
				List<RetrievedArea> localRetrievedAreas = retrievedAreasArray[i];
				if (localRetrievedAreas != null) {
					totalHits += localRetrievedAreas.size();
					List<RetrievedArea> retrievedAreasList = retrievedAreas[i];
					synchronized (retrievedAreasList) {

						if (retrievedAreasList.size() == 0) {
							retrievedAreasList.addAll(localRetrievedAreas);
						}

						else {
							List<RetrievedArea> toAdd = Lists.newArrayList();
							for (RetrievedArea existingArea : retrievedAreasList) {
								for (RetrievedArea newArea : localRetrievedAreas) {
									if (!existingArea.testAndSet(newArea.getQueryAreaBegin(),
											newArea.getSequenceAreaBegin(), sp.getMaxSubSequencesDistance(),
											subSequenceLength)) {
										toAdd.add(newArea);
									}
								}
							}
							retrievedAreasList.addAll(toAdd);
						}
					}
				}
			}

			logger.info("[" + this.toString() + "] Index search time:" + (System.currentTimeMillis() - init) + " and "
					+ totalHits + " hits.");
		} catch (Throwable t) {
			fails.add(t);
		} finally {
			countDown.countDown();
		}
	}

	private IndexRetrievedData getIndexPositions(final int[] iess, final int offset) throws ValueOutOfBoundsException,
			IOException {

		IndexRetrievedData retrievedData = new IndexRetrievedData(databank.getNumberOfSequences(), sp, subSequenceLength, this);

		for (int ss = 0; ss < iess.length; ss++) {
			retrieveIndexPosition(iess[ss], retrievedData, ss + offset);
		}
		return retrievedData;
	}

	private void retrieveIndexPosition(int encodedSubSequence, IndexRetrievedData retrievedData, int queryPos)
			throws ValueOutOfBoundsException, IOException {

		long[] indexPositions = databank.getMatchingSubSequence(encodedSubSequence);
		for (long subSequenceIndexInfo : indexPositions) {
			retrievedData.addSubSequenceInfoIntRepresention(queryPos, subSequenceIndexInfo);
		}
	}

	private int[] getEncodedSubSequences(String querySequence) {
		int size = querySequence.length() - subSequenceLength;
		int[] iess = new int[size];

		for (int i = 0; i < size; i++) {
			String subSequence = querySequence.substring(i, i + subSequenceLength);
			iess[i] = encoder.encodeSubSequenceToInteger(subSequence);
		}
		return iess;
	}

	private int[] getEncodedSubSequences(String querySequence, MaskEncoder maskEncoder) {
		if (maskEncoder == null) {
			return getEncodedSubSequences(querySequence);
		}

		int size = querySequence.length() - maskEncoder.getPatternLength();
		int[] iess = new int[size];

		for (int i = 0; i < size; i++) {
			iess[i] = maskEncoder.applyMask(i, i + maskEncoder.getPatternLength(), querySequence);
		}
		return iess;
	}

	public SymbolList getQuery() {
		return fullQuery;
	}

	public int[] getEncodedQuery() {
		return encodedQuery;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public IndexedSequenceDataBank getDatabank() {
		return databank;
	}

	public SearchParams getSearchParams() {
		return sp;
	}

	protected HSP createHSP(ExtendSequences extensionResult, GenoogleSequenceAlignment smithWaterman,
			double normalizedScore, double evalue, int queryLength, int targetLength) {

		return new HSP(smithWaterman, getQueryStart(extensionResult, smithWaterman), getQueryEnd(extensionResult,
				smithWaterman), getTargetStart(extensionResult, smithWaterman), getTargetEnd(extensionResult,
				smithWaterman), normalizedScore, evalue);
	}

	private int getQueryStart(ExtendSequences extensionResult, GenoogleSequenceAlignment smithWaterman) {
		return extensionResult.getBeginQuerySegment() + smithWaterman.getQueryStart();
	}

	private int getQueryEnd(ExtendSequences extensionResult, GenoogleSequenceAlignment smithWaterman) {
		return extensionResult.getBeginQuerySegment() + smithWaterman.getQueryEnd();
	}

	private int getTargetStart(ExtendSequences extensionResult, GenoogleSequenceAlignment smithWaterman) {
		return extensionResult.getBeginTargetSegment() + smithWaterman.getTargetStart();
	}

	private int getTargetEnd(ExtendSequences extensionResult, GenoogleSequenceAlignment smithWaterman) {
		return extensionResult.getBeginTargetSegment() + smithWaterman.getTargetEnd();
	}
}