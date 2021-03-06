/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009,2010,2011,2012  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.util;

import bio.pih.genoogle.search.IndexRetrievedData;
import bio.pih.genoogle.search.RetrievedArea;

/**
 * Circular Array used to store the {@link IndexRetrievedData} retrieved from the inverted index.
 * 
 * @author albrecht
 * 
 */
public class CircularArrayList {

	private RetrievedArea[] elementData;
	private int head = 0, tail = 0;
	private int size = 0;

	private static final int DEFAULT_SIZE = 5;

	/**
	 * Defaul constructor
	 */
	public CircularArrayList() {
		this(DEFAULT_SIZE);
	}

	/**
	 * Constructor which specify the size of circular list. When the slot is occupied fully, it
	 * grows to 3/2 of the previous size.
	 * 
	 * @param size
	 */
	public CircularArrayList(int size) {
		elementData = new RetrievedArea[size];
	}

	public boolean isEmpty() {
		return head == tail; // or size == 0
	}

	private void ensureCapacity(int minCapacity) {
		int oldCapacity = elementData.length;
		if (minCapacity > oldCapacity) {
			int newCapacity = (oldCapacity * 3) / 2 + 1;
			RetrievedArea newData[] = new RetrievedArea[newCapacity];
			toArray(newData);
			tail = size;
			head = 0;
			elementData = newData;
		}
	}

	public int size() {
		return size;
	}

	public RetrievedArea[] toArray(RetrievedArea a[]) {
		if (head < tail) {
			System.arraycopy(elementData, head, a, 0, tail - head);
		} else {
			System.arraycopy(elementData, head, a, 0, elementData.length - head);
			System.arraycopy(elementData, 0, a, elementData.length - head, tail);
		}
		return a;
	}

	public void add(int queryPos, int sequencePos, int subSequenceLength) {
		ensureCapacity(size + 1 + 1);
		RetrievedArea retrievedArea = elementData[tail];
		if (retrievedArea != null) {
			retrievedArea.reset(queryPos, sequencePos, subSequenceLength);
		} else {
			assert (tail != elementData.length);
			elementData[tail] = new RetrievedArea(queryPos, sequencePos, subSequenceLength);
		}
		tail = (tail + 1) % elementData.length;
		size++;
		assert ((head + size) % elementData.length == tail);
	}

/**
 * Set the informed position to this correct position. 
 * @param openedArea {@link RetrievedArea} which will be moved
 * @param pos which should be moved
 */
        

	public void rePos(RetrievedArea openedArea, int _pos) {
		int pos =  (_pos + head) % elementData.length;
		assert (elementData[pos] == openedArea);
		int prev = pos;
		pos = (pos + 1) % elementData.length;
		while (pos != tail && elementData[pos] != null
				&& openedArea.getQueryAreaEnd() > elementData[pos].getQueryAreaEnd()) {
			elementData[prev] = elementData[pos];
			elementData[pos] = openedArea;
			prev = pos;
			pos = (pos + 1) % elementData.length;
		}
	}

	/**
	 * Remove elements from the circular list.
	 * 
	 * @param total
	 *            quantity of elements which will be removed.
	 */
	public void removeElements(int total) {

		size = size - total;
		if (size == 0) {
			tail = 0;
			head = 0;
		} else {
			head = (head + total) % elementData.length;
		}
		assert ((head + size) % elementData.length == tail);
		assert (size >= 0);
		assert (tail >= 0);
		assert (head >= 0);
	}

	public RetrievedArea get(int pos) {
                if ((pos + 1) > size) {
                    return null;
                }
		RetrievedArea retrievedArea = elementData[(pos + head) % elementData.length];
		return retrievedArea;
	}

	public void clear() {
		head = tail = size = 0;
	}
}
