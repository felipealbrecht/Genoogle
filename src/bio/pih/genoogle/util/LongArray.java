/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.util;

import java.util.Arrays;

/**
 * Array of primitive long which has growing capability. Useful for large arrays of arrays.
 * 
 * @author Felipe Albrecht
 */
public class LongArray {

	private static long[] EMPTY_ARRAY = new long[0];
	private long[] finalArray;
	private long[] actualBlock;
	private int actualBockPos;

	private static final int DEFAULT_INITIAL_SIZE = 5;

	/**
	 * Minimum quantity of memory used by each instance of this class.
	 */
	public static final int MINIMUM_MEMORY_USED = 8 /* class instance */
			+ 8 /* final arrayArray */
			+ 8 /* actualBlock */
			+ 4 /* actualBlockPos */
			+ 8 * DEFAULT_INITIAL_SIZE; /* array */

	/**
	 * Constructor that gives the block size.
	 */
	public LongArray() {
		this.finalArray = null;
		this.actualBlock = null;
		this.actualBockPos = 0;
	}

	/**
	 * Add a new value into the array
	 * 
	 * @param value
	 */
	public void add(long value) {
		ensureCapacity();
		actualBlock[actualBockPos++] = value;
	}

	private void ensureCapacity() {
		if (actualBockPos == DEFAULT_INITIAL_SIZE) {
			getArray();
		}
		if (actualBlock == null) {
			actualBlock = new long[DEFAULT_INITIAL_SIZE];
			actualBockPos = 0;
		}
	}

	/**
	 * Get the data as a long array. It returns the internal array reference, do not a copy, for
	 * performance reasons. It is also used to compress the buckets into a unique array.
	 * 
	 * @return the array, without empty fields.
	 */
	public long[] getArray() {
		// Special case: no data was added from begin or after the last getArray()
		if (actualBockPos == 0) {
			if (finalArray == null) {
				return EMPTY_ARRAY;
			}
			return finalArray;
		}

		int size = 0;
		if (finalArray != null) {
			size += finalArray.length;
		}

		size += actualBockPos;

		long[] o = new long[size];

		int pos = 0;

		if (finalArray != null) {
			System.arraycopy(finalArray, 0, o, 0, finalArray.length);
			pos = finalArray.length;
		}

		System.arraycopy(actualBlock, 0, o, pos, actualBockPos);

		actualBockPos = 0;
		actualBlock = null;
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

	@Override
	public String toString() {
		getArray();
		return Arrays.toString(finalArray);
	}

	/**
	 * @return the length of the data
	 */
	public int length() {
		return getArray().length;
	}

	/**
	 * Saving space. Do not create a new LongArray neither a new actualBlock.
	 */
	public void reset() {
		actualBockPos = 0;
		finalArray = null;
	}

	/**
	 * test
	 * TODO: move to a junit.
	 */
	public static void mainX(String[] args) {
		LongArray longArray = new LongArray();
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
