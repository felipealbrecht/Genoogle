package bio.pih.genoogle.util;

import bio.pih.genoogle.util.CircularArrayList;

public final class Poll {
    int pos = -1;
    static final int MAX_SIZE = 1024 * 1024;
    CircularArrayList[] poll = new CircularArrayList[MAX_SIZE];

    public final void push(final CircularArrayList c) {
        if (pos < MAX_SIZE) {
            this.poll[++pos] = c;
        }
    }

    public final CircularArrayList pop() {
        if (this.pos < 0) {
            return new CircularArrayList();
        }
        return this.poll[pos--];
    }
}
