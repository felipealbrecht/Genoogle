package bio.pih.alignment;

import org.biojava.bio.alignment.SequenceAlignment;
import org.biojava.bio.symbol.SymbolList;

/** 
 * Some changes by Felipe Albrecht for faster alignment methods 
 */
public abstract class GenoogleSequenceAlignment extends SequenceAlignment {

  /**
   * Performs a pairwise sequence alignment of the two given SymbolList.
   * 
   * @param query
   * @param subject
   * @return score of the alignment or the distance.
   * @throws Exception
   */
  public abstract double fastPairwiseAlignment(SymbolList query, SymbolList subject)
      throws Exception;    
}
