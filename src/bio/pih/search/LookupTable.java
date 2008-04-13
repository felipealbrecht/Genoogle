package bio.pih.search;

import bio.pih.util.IntArray;

public class LookupTable {
	IntArray[] positions;
	int[] EMPTY = new int[0];

	public LookupTable() {
		positions = new IntArray[(int) Math.pow(4, 8)];
	}

	public void addPosition(int subSequence, int pos) {
		if (positions[subSequence] == null) {
			positions[subSequence] = new IntArray(20);
		}
		positions[subSequence].add(pos);
	}

	public int[] getPos(int subSequence) {
		if (positions[subSequence] != null) {
			return positions[subSequence].getArray();
		}
		return EMPTY;
	}

	public void end() {
		for (IntArray intArray : positions) {
			if (intArray != null) {
				intArray.sort();
			}
		}
	}
}
