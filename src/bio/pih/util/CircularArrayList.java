package bio.pih.util;

import bio.pih.search.IndexRetrievedData.RetrievedArea;

public class CircularArrayList {

	private RetrievedArea[] elementData;
	private int head = 0, tail = 0;
	private int size = 0;
	private Iterator it = new Iterator();

	public CircularArrayList() {
		this(5);
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
		assert((head + size) % elementData.length == tail);
		return true;

	}

	public boolean addFast(int queryPos, int sequencePos, int subSequenceLength) {
		elementData[tail] = new RetrievedArea(queryPos, sequencePos, subSequenceLength);
		tail = (tail + 1) % elementData.length;
		size++;
		assert(tail > head);
		return true;
	}
	
	public void rePos(RetrievedArea openedArea, int pos) {
		assert(elementData[pos] == openedArea);
		int prev = pos; 
		pos = (pos +1) % elementData.length;		
		while (pos != tail && elementData[pos] != null && openedArea.getQueryAreaEnd() > elementData[pos].getQueryAreaEnd()) {
			elementData[prev] = elementData[pos];
			elementData[pos] = openedArea;
			prev = pos;
			pos = (pos +1) % elementData.length;
		}
	}


	public void removeElements(int total) {

		size = size - total;
		if (size == 0) {
			tail = 0;
			head = 0;
		} else {
			head = (head + total) % elementData.length;  
		}
		assert((head + size) % elementData.length == tail);
		assert(size >= 0);
		assert(tail >= 0);
		assert(head >= 0);
	}

	public void clear() {
		head = tail = size = 0;
	}

	public Iterator getIterator() {
		it.reset();
		return it;
	}

	public class Iterator {
		int pos = -1;

		public boolean hasNext() {
			return pos+1 < size;
		}

		public RetrievedArea next() {
			pos++;
			RetrievedArea retrievedArea = elementData[(pos+head) % elementData.length];
			return retrievedArea;
		}

		public void reset() {
			pos = -1;
		}

		public int getPos() {
			return (pos + head) % elementData.length;
		}

	}
}
