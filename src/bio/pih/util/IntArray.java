/*
 * Copyright 2004-2007 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package bio.pih.util;

import java.util.Arrays;

/**
 * @author Thomas
 * @author Felipe Albrecht
 * 
 * TODO: junits!
 */
public class IntArray {

	private ArraysPool tmpPool;
	private int[] data;
	private int pos;
	private int hash;
	public int compressCount;
	private double previousGaim;

	public IntArray(int size) {
		this.data = new int[size];
		this.pos = 0;
		this.compressCount = 0;
		this.previousGaim = 1.0;
	}

	public void add(int value) {
		ensureCapacity();
		data[pos++] = value;
	}

	public boolean addUnique(int value) {
		for (int i = 0; i < pos; i++) {
			if (data[i] == value) {
				return false;
			}
		}
		add(value);
		return true;
	}

	public void add(int i, int value) {
		if (i > pos) {
			throw new ArrayIndexOutOfBoundsException("i=" + i + " size=" + pos);
		}
		ensureCapacity();
		if (i == pos) {
			add(value);
		} else {
			System.arraycopy(data, i, data, i + 1, pos - i);
			data[i] = value;
			pos++;
		}
	}

	public int get(int i) {
		if (i >= pos) {
			throw new ArrayIndexOutOfBoundsException("i=" + i + " size=" + pos);
		}
		return data[i];
	}

	public int remove(int i) {
		if (i >= pos) {
			throw new ArrayIndexOutOfBoundsException("i=" + i + " size=" + pos);
		}
		int value = data[i];
		System.arraycopy(data, i + 1, data, i, pos - i - 1);
		pos--;
		return value;
	}

	private void ensureCapacity() {
		if (pos == data.length) {
//			if (previousGaim > 0.25) {
//				compress();
//			}
			if (pos == data.length) {
				int[] d = new int[data.length * 2];
				System.arraycopy(data, 0, d, 0, data.length);
				data = d;
				previousGaim = 0.5;
			}
		}
	}

	public static void mainX(String[] args) {
		// int[] a = new int[] {1, 2, 3, 2, 5, 3, 4}; // 1, 2, 3, 4, 5
		IntArray intArray = new IntArray(10);
		intArray.add(1);
		intArray.add(2);
		intArray.add(3);
		intArray.add(2);
		intArray.add(5);
		intArray.add(3);
		intArray.add(4);
		intArray.compress();
		System.out.println(intArray);
		intArray.add(6);
		intArray.add(6);
		intArray.add(6);
		intArray.add(6);
		intArray.add(7);
		intArray.add(9);
		System.out.println(intArray);
		intArray.compress();
		intArray.add(15);
		intArray.add(21);
		System.out.println(intArray);
		intArray.compress();
		intArray.add(6);
		intArray.add(6);
		intArray.add(6);
		intArray.add(6);
		intArray.add(7);
		intArray.add(9);
		intArray.compress();
		intArray.add(1);
		intArray.add(2);
		intArray.add(3);
		intArray.add(2);
		intArray.add(5);
		intArray.add(3);
		intArray.add(4);
		intArray.compress();
		System.out.println(intArray);
		intArray.add(15);
		intArray.add(6);
		intArray.add(6);
		intArray.add(6);
		intArray.add(6);
		intArray.add(7);
		intArray.add(9);
		intArray.compress();
		System.out.println(intArray);
		intArray.compress();
		intArray.add(6);
		intArray.add(6);
		intArray.add(6);
		intArray.add(6);
		intArray.add(7);
		intArray.add(9);
		intArray.add(7);
		intArray.add(6);
		intArray.add(11);
		intArray.compress();
		System.out.println(intArray);
	}

	
	public void compress() {		
		if (pos == 0) return;		
		this.sort();
		int k = 1;
		for (int i = 1; i < pos; i++) {
			if (this.data[i] != this.data[i - 1]) {
				this.data[k++] = this.data[i];
			}
		}
		compressCount++;
		assert k <= pos;
		previousGaim = (pos - k) / (double) pos;
		if (previousGaim >= 0.5){
			System.out.println("ohu!");
		}
		this.pos = k;
	}

	public void set(int i, int value) {
		if (i >= pos) {
			throw new ArrayIndexOutOfBoundsException("i=" + i + " size=" + pos);
		}
		data[i] = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IntArray)) {
			return false;
		}
		IntArray other = (IntArray) obj;
		if (hashCode() != other.hashCode() || pos != other.pos) {
			return false;
		}
		for (int i = 0; i < pos; i++) {
			if (data[i] != other.data[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		if (pos >= 1) {
			sb.append(data[0]);
			for (int i = 1; i < pos; i++) {
				sb.append(',');
				sb.append(data[i]);
			}
		}
		sb.append(']');
		return sb.toString();
	}

	public int size() {
		return data.length;
	}

	public int pos() {
		return pos;
	}

	public void sort() {
		Arrays.sort(this.data, 0, pos);
	}


	public void toArray(int[] array) {
		System.arraycopy(data, 0, array, 0, pos);
	}
	

	private class ArraysPool {
		final int[][] arraysPool; 
		final int initialSize;
		final int deep;

		public ArraysPool(int initialSize, int deep) {
			this.initialSize = initialSize;
			this.deep = deep;
			this.arraysPool = new int[deep][];
		}

		public int[] getArray(int size) {
			int deep = Integer.highestOneBit(size / initialSize) - 1;
			int[] array = arraysPool[deep]; 
			if (array == null) {
				array = new int[size];
				arraysPool[deep] = array;
			}
			return array;
		}
		
		public void setArray(int size, int[] array) {
			int deep = Integer.highestOneBit(size / initialSize) - 1;
			arraysPool[deep] = array;
		}
	}
}
