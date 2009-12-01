/*
 * Genoogle: Similar DNA Sequences Searching Engine and Tools. (http://genoogle.pih.bio.br)
 * Copyright (C) 2008,2009  Felipe Fernandes Albrecht (felipe.albrecht@gmail.com)
 *
 * For further information check the LICENSE file.
 */

package bio.pih.genoogle.index;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public  class IndexFileOffset {
	final int subSequence;
	final int offset;
	final int length;
	
	public IndexFileOffset(int subSequence, int offset, int length) {
		this.subSequence = subSequence;
		this.offset = offset;
		this.length = length;
	}
	
	public void write(DataOutputStream stream) throws IOException {
		stream.writeInt(subSequence);
		stream.writeInt(offset);
		stream.writeInt(length);
	}

	public static void writeTo(int subSequence, int sequenceId, int position, DataOutputStream stream)
			throws IOException {
		stream.writeInt(subSequence);
		stream.writeInt(sequenceId);
		stream.writeInt(position);
	}

	public static IndexFileOffset newFrom(DataInputStream stream) throws IOException {
		int subSequence = stream.readInt();
		int offset = stream.readInt();
		int length = stream.readInt();
		return new IndexFileOffset(subSequence, offset, length);
	}
}