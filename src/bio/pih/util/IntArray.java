/*
 * Copyright 2004-2007 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package bio.pih.util;

/**
 * @author Thomas
 * some changes by Felipe Albrecht
 */
public class IntArray {

    private int[] data;
    private int pos;
    private int hash;

    public IntArray() {
        this.data = new int[10];
    }

    public IntArray(int[] data) {
        this.data = data;
        this.pos = 0;
    }
    
    public IntArray(int size) {
        this.data = new int[size];
        this.pos = 0;
    }

    public void add(int value) {
        ensureCapacity();
        data[pos++] = value;
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
            int[] d = new int[data.length * 2];
            System.arraycopy(data, 0, d, 0, data.length);
            data = d;
        }
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

    public void set(int i, int value) {
        if (i >= pos) {
            throw new ArrayIndexOutOfBoundsException("i=" + i + " size=" + pos);
        }
        data[i] = value;
    }

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

    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        int h = pos + 1;
        for (int i = 0; i < pos; i++) {
            h = h * 31 + data[i];
        }
        hash = h;
        return h;
    }

    public int size() {
        return data.length;
    }
    
    public int pos() {
    	return pos;
    }

    public void addValueSorted(int value) {
        int l = 0, r = pos;
        while (l < r) {
            int i = (l + r) >>> 1;
            int d = data[i];
            if (d == value) {
                return;
            } else if (d > value) {
                r = i;
            } else {
                l = i + 1;
            }
        }
        add(l, value);
    }

    public void removeValue(int value) {
        for (int i = 0; i < pos; i++) {
            if (data[i] == value) {
                remove(i);
                return;
            }
        }        
    }

    public int findNextValueIndex(int value) {
        int l = 0, r = pos;
        while (l < r) {
            int i = (l + r) >>> 1;
            int d = data[i];
            if (d >= value) {
                r = i;
            } else {
                l = i + 1;
            }
        }
        return l;

//        for(int i=0; i<size; i++) {
//            if(data[i] >= value) {
//                return i;
//            }
//        }
//        return size;
    }

    public void sort() {
        // insertion sort
        for (int i = 1, j; i < size(); i++) {
            int t = get(i);
            for (j = i - 1; j >= 0 && (get(j) > t); j--) {
                set(j + 1, get(j));
            }
            set(j + 1, t);
        }
    }

    public void toArray(int[] array) {
        System.arraycopy(data, 0, array, 0, pos);
    }

}
