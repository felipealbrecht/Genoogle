package bio.pih.io;

import java.nio.ByteBuffer;

import bio.pih.io.proto.Io.StoredSequence;

import com.google.protobuf.ByteString;

/**
 * Some methods for various proposes.
 * 
 * @author albrecht
 */
public class Utils {
	public static int[] getEncodedSequence(StoredSequence storedSequence) {
		ByteString encodedSequence = storedSequence.getEncodedSequence();
		byte[] byteArray = encodedSequence.toByteArray();
		final int[] ret = new int[byteArray.length/4];
		ByteBuffer.wrap(byteArray).asIntBuffer().get(ret);
		return ret;
	}
	

	public static String invert (String s) {
	     StringBuilder temp = new StringBuilder();
	     for (int i=s.length()-1; i>=0; i--) {
	    	 temp.append(s.charAt(i));
	     }
	     return temp.toString();
	}
	
}
