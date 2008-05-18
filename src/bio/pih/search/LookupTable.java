package bio.pih.search;

import bio.pih.util.IntArray;

/**
 * Table that stores sub-sequences and its similar sub-sequences positions at the query.
 * Before the use of the LookupTable, the end() method should be called.
 * 
 * @author albrecht
 */
public class LookupTable {
	IntArray[] positions;
	int[] EMPTY = new int[0];

	/**
	 * Construct an empty {@link LookupTable}
	 */
	public LookupTable() {
		positions = new IntArray[(int) Math.pow(4, 8)];
	}

	/**
	 * Add a new sub-sequence and its position information.
	 * @param subSequence
	 * @param pos
	 */
	public void addPosition(int subSequence, int pos) {
		if (positions[subSequence] == null) {
			positions[subSequence] = new IntArray(20);
		}
		positions[subSequence].add(pos);
	}

	/**
	 * Get the positions where a sub-sequence occurs.
	 * 
	 * @param subSequence
	 * @return array with all positions where the subSequence occurs.
	 */
	public int[] getPos(int subSequence) {
		if (positions[subSequence] != null) {
			return positions[subSequence].getArray();
		}
		return EMPTY;
	}

	/**
	 * Finish the construction of the {@link LookupTable}.
	 * It should be executed before any getPos(int subSequence) call.
	 */
	public void end() {
		for (IntArray intArray : positions) {
			if (intArray != null) {
				intArray.sort();
			}
		}
	}
}
