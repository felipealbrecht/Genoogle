package bio.pih.search;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.alignment.GenoogleSmithWaterman;
import bio.pih.encoder.DNAMaskEncoder;
import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.index.InvalidHeaderData;
import bio.pih.index.ValueOutOfBoundsException;
import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.search.results.HSP;
import bio.pih.statistics.Statistics;
import bio.pih.util.SymbolListWindowIterator;
import bio.pih.util.SymbolListWindowIteratorFactory;

/**
 * Interface witch defines methods for search for similar DNA sequences and checks the status of the
 * searchers.
 * 
 * @author albrecht
 */
public class DNAIndexSearcher implements Callable<IndexRetrievedData> {

	private static final Logger logger = Logger.getLogger(DNAIndexSearcher.class.getName());

	protected final long id;
	protected final SearchParams sp;
	protected final int subSequenceLegth;
	protected final DNASequenceEncoderToInteger encoder;
	protected final IndexedDNASequenceDataBank databank;
	protected final SymbolList querySequence;
	private final Statistics statistics;
	private int[] encodedQuery;

	private final CountDownLatch countDown;
	private final IndexRetrievedData[] retrievedDatas;
	private final int resultPos;

	/**
	 * @param id
	 * @param sp
	 * @param databank
	 * @param i 
	 * @param retrievedDatas 
	 * @param countDown 
	 * @throws BioException
	 */
	public DNAIndexSearcher(long id, SearchParams sp, IndexedDNASequenceDataBank databank, CountDownLatch countDown, IndexRetrievedData[] retrievedDatas, int resultPos )
			throws BioException {
		this.id = id;
		this.sp = sp;
		this.databank = databank;
		this.countDown = countDown;
		this.retrievedDatas = retrievedDatas;
		this.resultPos = resultPos;		
		this.subSequenceLegth = databank.getSubSequenceLength();
		this.encoder = databank.getEncoder();
		this.querySequence = getQuery();
		this.statistics = new Statistics(1, -3, querySequence, databank.getTotalDataBaseSize(),
				databank.getTotalNumberOfSequences(), sp.getMinEvalue());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(Long.toString(id));
		sb.append(" (direct) ");
		return sb.toString();
	}

	@Override
	public IndexRetrievedData call() throws Exception {
		int queryLength = querySequence.length();
		if (queryLength < databank.getSubSequenceLength()) {
			throw new RuntimeException("Sequence: \"" + querySequence.seqString()
					+ "\" is too short");
		}
		
		this.encodedQuery = encoder.encodeSymbolListToIntegerArray(querySequence);

		// sr.setMinSubSequenceLength(this.statistics.getMinLengthDropOut());

		logger.info("[" + this.toString() + "] Begining the search at " + databank.getName()
				+ " with the sequence with " + querySequence.length() + "bases "
				+ querySequence.seqString() + " and min subSequenceLength >= "
				+ this.statistics.getMinLengthDropOut());

		int[] iess = getEncodedSubSequences(querySequence, databank.getMaskEncoder());		

		long init = System.currentTimeMillis();
		IndexRetrievedData retrievedData = getIndexPositions(iess);

		retrievedData.finish();
		retrievedDatas[resultPos] = retrievedData;
		countDown.countDown();
		
		logger.info("[" + this.toString() + "] Index search time:"
				+ (System.currentTimeMillis() - init) + " with " + retrievedData.getTotalAreas()
				+ " areas in " + retrievedData.getTotalSequences() + " sequences.");

		return retrievedData;
	}

	private IndexRetrievedData getIndexPositions(int[] iess) throws ValueOutOfBoundsException,
			IOException, InvalidHeaderData {

		int subSequenceLength = databank.getMaskEncoder() == null ? databank.getSubSequenceLength()
				: databank.getMaskEncoder().getPatternLength();
		IndexRetrievedData retrievedData = new IndexRetrievedData(databank.getNumberOfSequences(),
				sp, statistics.getMinLengthDropOut(), subSequenceLength, this);

		for (int ss = 0; ss < iess.length; ss++) {
			retrieveIndexPosition(iess[ss], retrievedData, ss);
		}
		return retrievedData;
	}

	boolean useSimilarSubSequences = false;

	private void retrieveIndexPosition(int encodedSubSequence, IndexRetrievedData retrievedData,
			int queryPos) throws ValueOutOfBoundsException, IOException, InvalidHeaderData {

		if (useSimilarSubSequences) {
			List<Integer> similarSubSequences = databank.getSimilarSubSequence(encodedSubSequence);
			for (Integer similarSubSequence : similarSubSequences) {
				long[] indexPositions = databank.getMatchingSubSequence(similarSubSequence);
				for (long subSequenceIndexInfo : indexPositions) {
					retrievedData.addSubSequenceInfoIntRepresention(queryPos, subSequenceIndexInfo);
				}
			}
		} else {
			long[] indexPositions = databank.getMatchingSubSequence(encodedSubSequence);
			for (long subSequenceIndexInfo : indexPositions) {
				retrievedData.addSubSequenceInfoIntRepresention(queryPos, subSequenceIndexInfo);
			}
		}
	}

	private int[] getEncodedSubSequences(SymbolList querySequence) {
		int[] iess = new int[querySequence.length() - (subSequenceLegth - 1)];

		SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory
				.getOverlappedFactory()
				.newSymbolListWindowIterator(querySequence, subSequenceLegth);
		int pos = -1;
		while (symbolListWindowIterator.hasNext()) {
			pos++;
			SymbolList subSequence = symbolListWindowIterator.next();
			iess[pos] = encoder.encodeSubSymbolListToInteger(subSequence);
		}
		return iess;
	}

	private int[] getEncodedSubSequences(SymbolList querySequence, DNAMaskEncoder maskEncoder) {
		if (maskEncoder == null) {
			return getEncodedSubSequences(querySequence);
		}

		int[] iess = new int[querySequence.length() - (maskEncoder.getPatternLength() - 1)];

		SymbolListWindowIterator symbolListWindowIterator = SymbolListWindowIteratorFactory
				.getOverlappedFactory().newSymbolListWindowIterator(querySequence,
						maskEncoder.getPatternLength());
		int pos = -1;
		while (symbolListWindowIterator.hasNext()) {
			pos++;
			iess[pos] = maskEncoder.applyMask(symbolListWindowIterator.next());
		}
		return iess;
	}

	protected SymbolList getQuery() throws IllegalSymbolException {
		return sp.getQuery();
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public IndexedDNASequenceDataBank getDatabank() {
		return databank;
	}
	
	public SearchParams getSearchparams() {
		return sp;
	}
	
	public int[] getEncodedQuery() {
		return encodedQuery;
	}
		
	protected HSP createHSP(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman,
			double normalizedScore, double evalue, int queryLength, int targetLength) {

		return new HSP(smithWaterman, getQueryStart(extensionResult, smithWaterman), getQueryEnd(
				extensionResult, smithWaterman), getTargetStart(extensionResult, smithWaterman),
				getTargetEnd(extensionResult, smithWaterman), normalizedScore, evalue);
	}

	private int getQueryStart(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman) {
		return extensionResult.getBeginQuerySegment() + smithWaterman.getQueryStart();
	}

	private int getQueryEnd(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman) {
		return extensionResult.getBeginQuerySegment() + smithWaterman.getQueryEnd();
	}

	private int getTargetStart(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman) {
		return extensionResult.getBeginTargetSegment() + smithWaterman.getTargetStart();
	}

	private int getTargetEnd(ExtendSequences extensionResult, GenoogleSmithWaterman smithWaterman) {
		return extensionResult.getBeginTargetSegment() + smithWaterman.getTargetEnd();
	}
}