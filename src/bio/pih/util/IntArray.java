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
public class IntArray {

	private static int[] EMPTY_ARRAY = new int[0];
	private List<int[]> blockArrays;
	private int[] finalArray;
	private int[] actualBlock;
	private int actualBockPos;
	private int blockSize;

	private static int defaultInitialSize = 50;

	/**
	 * Default constructor that uses default size for the blocks.
	 */
	public IntArray() {
		this(defaultInitialSize);
	}

	/**
	 * Constructor that gives the block size.
	 * 
	 * @param blockSize
	 */
	public IntArray(int blockSize) {
		this.finalArray = null;
		this.actualBlock = null;
		this.blockArrays = null;
		this.actualBockPos = 0;
		this.blockSize = blockSize;
	}

	/**
	 * Add a new value into the array
	 * 
	 * @param value
	 */
	public void add(int value) {
		ensureCapacity();
		actualBlock[actualBockPos++] = value;
	}

	private void ensureCapacity() {
		if (actualBlock == null) {
			actualBlock = new int[blockSize];
			actualBockPos = 0;
		} else if (actualBockPos == blockSize) {
			getBlocksArray().add(actualBlock);
			actualBlock = new int[blockSize];
			actualBockPos = 0;
		}
	}

	private List<int[]> getBlocksArray() {
		if (blockArrays == null) {
			blockArrays = new LinkedList<int[]>();
		}
		return blockArrays;
	}

	/**
	 * Get the data as a int array. It returns the internal array reference, do not a copy, for performance reasons.
	 * 
	 * @return the array, without empty fields.
	 */
	public int[] getArray() {
		// Special case: no data was added from begin or after the last getArray()
		if ((actualBockPos == 0) && (getBlocksArray().size() == 0)) {
			if (finalArray == null) {
				return EMPTY_ARRAY;
			}
			return finalArray;
		}

		int size = 0;
		if (finalArray != null) {
			size += finalArray.length;
		}
		size += (getBlocksArray().size() * blockSize);
		size += actualBockPos;

		int[] o = new int[size];

		int pos = 0;

		if (finalArray != null) {
			System.arraycopy(finalArray, 0, o, 0, finalArray.length);
			pos = finalArray.length;
		}

		for (int[] block : getBlocksArray()) {
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
	
	/**
	 * Sort the array
	 */
	public void sort() {
		getArray();
		if (finalArray != null) {
			Arrays.sort(finalArray);
		}
	}
	
	/**
	 * @return the length of the data
	 */
	public int length() {
		return getArray().length;
	}
	
	@Override
	public String toString() {
		getArray();
		return Arrays.toString(finalArray);
	}

	/**
	 * Test
	 * 
	 * @param args
	 */
	public static void mainX(String[] args) {
		IntArray intArray = new IntArray(3);
		intArray.add(1);
		intArray.add(2);
		intArray.add(3);
		for (int i : intArray.getArray())
			System.out.print(i + " ");
		System.out.println();
		intArray.add(2);
		intArray.add(5);
		intArray.add(3);
		for (int i : intArray.getArray())
			System.out.print(i + " ");
		System.out.println();
		intArray.add(4);
		for (int i : intArray.getArray())
			System.out.print(i + " ");
		System.out.println();
		intArray.add(30);
		intArray.add(40);
		intArray.add(50);
		intArray.add(60);
		intArray.add(70);
		intArray.add(80);
		for (int i : intArray.getArray())
			System.out.print(i + " ");
		System.out.println();
		intArray.add(15);
		intArray.add(21);
		for (int i : intArray.getArray())
			System.out.print(i + " ");
		System.out.println();
		intArray.add(6);
		intArray.add(6);
		intArray.add(6);
		intArray.add(6);
		intArray.add(7);
		intArray.add(9);
		intArray.add(1);
		intArray.add(2);
		intArray.add(3);
		intArray.add(2);
		intArray.add(5);
		intArray.add(3);
		intArray.add(4);
		for (int i : intArray.getArray())
			System.out.print(i + " ");
		System.out.println();
		intArray.add(15);
		intArray.add(6);
		intArray.add(6);
		intArray.add(6);
		intArray.add(6);
		intArray.add(7);
		intArray.add(9);
		for (int i : intArray.getArray())
			System.out.print(i + " ");
		System.out.println();
		intArray.add(6);
		intArray.add(6);
		intArray.add(6);
		intArray.add(6);
		intArray.add(7);
		intArray.add(9);
		intArray.add(7);
		intArray.add(6);
		intArray.add(11);
		for (int i : intArray.getArray())
			System.out.print(i + " ");
		System.out.println();
	}

}
