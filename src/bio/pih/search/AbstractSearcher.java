package bio.pih.search;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

import bio.pih.alignment.GenoogleSmithWaterman;
import bio.pih.encoder.DNASequenceEncoderToInteger;
import bio.pih.io.SequenceDataBank;
import bio.pih.search.SearchStatus.SearchStep;
import bio.pih.search.results.HSP;
import bio.pih.search.results.SearchResults;

/**
 * This interface defines the methods that are presents in a similar sequence
 * searcher.
 * 
 * The doSearch method works asynchronous, returning an unique identifier for
 * the solicited search. The verifySearch return the current status of the
 * search. Others methods are for
 * 
 * @author albrecht
 */
public abstract class AbstractSearcher implements Callable<SearchResults> {

	protected final SearchStatus status;
	protected final SearchParams sp;
	protected final SearchResults sr;
	protected final DNASequenceEncoderToInteger encoder;
	protected final int subSequenceLegth;
	protected final long id;
	protected final ExecutorService executor;

	

	/**
	 * @param id
	 * @param sp
	 *            Parameter of the search
	 * @param databank 
	 *            Sequence data bank where the search will be performed.
	 */
	public AbstractSearcher(long id, SearchParams sp, SequenceDataBank databank, 
			ExecutorService executor) {
		this.id = id;
		this.sp = sp;
		this.executor = executor;
		this.sr = new SearchResults(sp);
		this.encoder = databank.getEncoder();
		this.subSequenceLegth = databank.getSubSequenceLength();
		status = new SearchStatus(id, sp, databank);
		status.setActualStep(SearchStep.NOT_INITIALIZED);
	}

	/**
	 * @return {@link SearchStatus} of this Search
	 */
	public SearchStatus getStatus() {
		return status;
	}
		
	/**
	 * @return the results of the search
	 */
	public SearchResults getSearchResults() {
		return sr;
	}
	
	protected SymbolList getQuery() throws IllegalSymbolException {
		return sp.getQuery();
	}

	protected HSP createHSP(ExtendSequences extensionResult,
			GenoogleSmithWaterman smithWaterman, double normalizedScore, double evalue, 
			int queryLength, int targetLength) {

		return new HSP(smithWaterman,
				getQueryStart(extensionResult, smithWaterman),
				getQueryEnd(extensionResult, smithWaterman),
				getTargetStart(extensionResult, smithWaterman),
				getTargetEnd(extensionResult, smithWaterman),
				normalizedScore, evalue);
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
