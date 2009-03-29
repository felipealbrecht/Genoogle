package bio.pih.util;

import bio.pih.search.IndexRetrievedData.RetrievedArea;

public class CircularArrayList {

	private RetrievedArea[] elementData;
	private int head = 0, tail = 0;
	private int size = 0;
	private Iterator it = new Iterator();

	public CircularArrayList() {
		this(4);
	}

	public CircularArrayList(int size) {
		elementData = new RetrievedArea[size];
	}

	public boolean isEmpty() {
		return head == tail; // or size == 0
	}

	public void ensureCapacity(int minCapacity) {
		int oldCapacity = elementData.length;
		if (minCapacity > oldCapacity) {
			int newCapacity = (oldCapacity * 3) / 2 + 1;
			if (newCapacity < minCapacity)
				newCapacity = minCapacity;
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

	public boolean add(int queryPos, int sequencePos, int subSequenceLength) {
		ensureCapacity(size + 1 + 1);
		RetrievedArea retrievedArea = elementData[tail];
		if (retrievedArea != null) {
			retrievedArea.reset(queryPos, sequencePos, subSequenceLength);
		} else {
			assert(tail != elementData.length);
			elementData[tail] = new RetrievedArea(queryPos, sequencePos, subSequenceLength);
		}
		tail = (tail + 1) % elementData.length;
		size++;
		assert(tail > head);
		return true;

	}

	public boolean addFast(int queryPos, int sequencePos, int subSequenceLength) {
		elementData[tail] = new RetrievedArea(queryPos, sequencePos, subSequenceLength);
		tail = (tail + 1) % elementData.length;
		size++;
		assert(tail > head);
		return true;
	}

	public void removeElements(int from, int to) {
		int total = Math.max(from, to) - Math.min(from, to);
		size = size - total;
		if (size == 0) {
			tail = 0;
			head = 0;
		} else {
			head = (head + total) % elementData.length;  
		}
		assert(size >= 0);
		assert(tail >= 0);
		assert(head >= 0);
		assert(tail >= head);
	}

	public void clear() {
		head = tail = size = 0;
	}

	public Iterator getIterator() {
		it.reset();
		return it;
	}

	public class Iterator {
		int pos = 0;

		public boolean hasNext() {
			return pos < size;
		}

		public RetrievedArea next() {
			assert(pos >= 0);
			RetrievedArea retrievedArea = elementData[pos];
			pos = (pos+1) % elementData.length;
			return retrievedArea;
		}

		public void reset() {
			pos = 0;
		}

		public int getPos() {
			return pos-1;
		}

	}

}
