package bio.pih.io;

import java.nio.ByteBuffer;

import bio.pih.io.proto.Io.StoredSequence;

import com.google.protobuf.ByteString;

public class Utils {
	static int[] getEncodedSequence(StoredSequence storedSequence) {
		ByteString encodedSequence = storedSequence.getEncodedSequence();
		byte[] byteArray = encodedSequence.toByteArray();
		final int[] ret = new int[byteArray.length/4];
		ByteBuffer.wrap(byteArray).asIntBuffer().get(ret);
		return ret;
	}
}
