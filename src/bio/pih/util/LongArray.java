package bio.pih.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Felipe Albrecht
 * 
 * KISS !
 * 
 * TODO: junits!
 */
public class LongArray {

	private List<long[]> blockArrays;
	private long[] finalArray;
	private long[] actualBlock;
	private int actualBockPos;
	private int blockSize;
	
	private static int defaultInitialSize = 50; 

	public LongArray() {
		this(defaultInitialSize);
	}
	
	public LongArray(int blockSize) {
		this.finalArray = null;
		this.actualBlock = null;
		this.blockArrays = null;
		this.actualBockPos = 0;
		this.blockSize = blockSize;
	}

	public void add(long value) {
		ensureCapacity();
		actualBlock[actualBockPos++] = value;
	}

	private void ensureCapacity() {
		if (actualBlock == null) {
			actualBlock = new long[blockSize];
			actualBockPos = 0;
		} else if (actualBockPos == blockSize) {
			getBlocksArray().add(actualBlock);
			actualBlock = new long[blockSize];
			actualBockPos = 0;
		}
	}

	private List<long[]> getBlocksArray() {
		if (blockArrays == null) {
			blockArrays = new LinkedList<long[]>();
		}
		return blockArrays;
	}

	public long[] getArray() {
		// Special case: no data was added from begin or after the last getArray()
		if ((actualBockPos == 0) && (getBlocksArray().size() == 0)) {
			return finalArray;
		}

		int size = 0;
		if (finalArray != null) {
			size += finalArray.length;
		}
		size += (getBlocksArray().size() * blockSize);
		size += actualBockPos;

		long[] o = new long[size];

		int pos = 0;

		if (finalArray != null) {
			System.arraycopy(finalArray, 0, o, 0, finalArray.length);
			pos = finalArray.length;
		}

		for (long[] block : getBlocksArray()) {
			System.arraycopy(block, 0, o, pos, blockSize);
			pos += blockSize;
		}

		System.arraycopy(actualBlock, 0, o, pos, actualBockPos);
		
		actualBockPos = 0;
		actualBlock = null;
		blockArrays = null;
		finalArray = o;
		

		return finalArray;
	}

	public static void mainX(String[] args) {
		LongArray longArray = new LongArray(3);
		longArray.add(1);
		longArray.add(2);
		longArray.add(3);
		for (long i : longArray.getArray())
			System.out.print(i + " ");
		System.out.println();
		longArray.add(2);
		longArray.add(5);
		longArray.add(3);
		for (long i : longArray.getArray())
			System.out.print(i + " ");
		System.out.println();
		longArray.add(4);
		for (long i : longArray.getArray())
			System.out.print(i + " ");
		System.out.println();
		longArray.add(30);
		longArray.add(40);
		longArray.add(50);
		longArray.add(60);
		longArray.add(70);
		longArray.add(80);
		for (long i : longArray.getArray())
			System.out.print(i + " ");
		System.out.println();
		longArray.add(15);
		longArray.add(21);
		for (long i : longArray.getArray())
			System.out.print(i + " ");
		System.out.println();
		longArray.add(6);
		longArray.add(6);
		longArray.add(6);
		longArray.add(6);
		longArray.add(7);
		longArray.add(9);
		longArray.add(1);
		longArray.add(2);
		longArray.add(3);
		longArray.add(2);
		longArray.add(5);
		longArray.add(3);
		longArray.add(4);
		for (long i : longArray.getArray())
			System.out.print(i + " ");
		System.out.println();
		longArray.add(15);
		longArray.add(6);
		longArray.add(6);
		longArray.add(6);
		longArray.add(6);
		longArray.add(7);
		longArray.add(9);
		for (long i : longArray.getArray())
			System.out.print(i + " ");
		System.out.println();
		longArray.add(6);
		longArray.add(6);
		longArray.add(6);
		longArray.add(6);
		longArray.add(7);
		longArray.add(9);
		longArray.add(7);
		longArray.add(6);
		longArray.add(11);
		for (long i : longArray.getArray())
			System.out.print(i + " ");
		System.out.println();
	}

}
