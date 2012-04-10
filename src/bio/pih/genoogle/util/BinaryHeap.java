package bio.pih.genoogle.util;

import java.util.Comparator;

public class BinaryHeap<T> {

	private final Comparator<T> comparator;
	private final T[] data;
	private int freePos;

	@SuppressWarnings("unchecked")
	public BinaryHeap(int size, Comparator<T> comparator) {
		this.comparator = comparator;
		this.data = (T[]) new Object[size];
		this.freePos = 0;
	}

	public void add(T t) {
		if (freePos == data.length) {
			// TODO: re arrange/increase?
			throw new RuntimeException("Too much data in the Binary Heap");
		}		
		data[freePos] = t;
		freePos++;
		for (int i = freePos-1; i > 0;) {
			int parent = (int)((i-1)/2);			
			if (comparator.compare(data[i], data[parent]) < 0) {
				T swap = data[i];
				data[i] = data[parent];
				data[parent] = swap;
			}
			i = parent;
		}
	}

	private void heapify(final int i) {
		if (i > freePos-1) {
			return;			
		}

		final int left = (i + 1) * 2 -1;
		final int right = (i + 1)* 2;
		
		int minidx = i;
		 
		if (left < freePos && comparator.compare(data[left], data[i]) < 0) {
			minidx = left;
		}
		if (right < freePos && comparator.compare(data[right], data[i]) < 0 && comparator.compare(data[right], data[left]) < 0) {
			minidx= right;
		}
		if (minidx != i) {
			T swap = data[i];
			data[i] = data[minidx];
			data[minidx] = swap;
			heapify(minidx);
		}
	}

	public T top() {
		if (freePos == 0) {
			return null;
		}
		return data[0];
	}

	public T switchTop(T t) {
		T v;
		if (freePos == 0) {
			v = null;
		} else {
			v = data[0];
		}
		data[0] = t;
		heapify(0);
		return v;
	}	
	
	public T removeTop() {
		if (freePos == 0) {
			return null;
		}
		
		T top = data[0];
		data[0] = data[freePos-1] ;
		data[freePos-1] = null;
		freePos--;		
		heapify(0);
		
		return top;
	}
	
	public boolean isEmpty() {
		return freePos == 0;
	}
	
	public static void xx(String[] args) {
		Comparator<Integer> c = new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}			
		};
		
		BinaryHeap<Integer> bh = new BinaryHeap<Integer>(10, c);

		bh.add(7);
		bh.add(6);		
		bh.add(8);
		bh.add(10);
		bh.add(4);
		bh.add(1);
		bh.add(9);		
		bh.add(2);
		bh.add(3);		
		bh.add(5);
		
		
		System.out.println(bh.switchTop(11));
		System.out.println(bh.switchTop(1));
		System.out.println(bh.switchTop(5));
		System.out.println(bh.switchTop(8));
		System.out.println(bh.switchTop(9));
		System.out.println(bh.switchTop(51));
		System.out.println(bh.switchTop(5));
		System.out.println(bh.removeTop());
		System.out.println(bh.removeTop());
		System.out.println(bh.removeTop());
		System.out.println(bh.removeTop());
		System.out.println(bh.removeTop());
		System.out.println(bh.removeTop());
		System.out.println(bh.removeTop());
		System.out.println(bh.removeTop());
		System.out.println(bh.removeTop());
		System.out.println(bh.removeTop());
		
	}
}
