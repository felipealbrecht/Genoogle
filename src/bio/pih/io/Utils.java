package bio.pih.io;

import java.nio.ByteBuffer;
import java.util.Arrays;

import bio.pih.io.proto.Io.StoredSequence;

import com.google.protobuf.ByteString;

/**
 * Some methods for various proposes.
 * 
 * @author albrecht
 */
public class Utils {

	public static int[] getEncodedSequenceAsArray(StoredSequence storedSequence) {
		ByteString encodedSequence = storedSequence.getEncodedSequence();
		byte[] byteArray = encodedSequence.toByteArray();
		int[] ret = new int[byteArray.length / 4];
		ByteBuffer.wrap(byteArray).asIntBuffer().get(ret);
		return ret;
	}

	public static String invert(String s) {
		char[] cs = s.toCharArray();
		char[] result = new char[s.length()];
		int j = 0;
		int i = s.length() - 1;
		while (i >= 0) {
			result[j++] = cs[i--];
		}
		return new String(result);
	}

	public static String sequenceComplement(String seqString) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < seqString.length(); i++) {
			char base = seqString.charAt(i);
			char complement = Utils.getComplement(base);
			sb.append(complement);
		}
		return sb.toString();
	}

	public static char getComplement(char base) {
		switch (base) {
		case 'A':
			return 'T';
		case 'T':
			return 'A';
		case 'C':
			return 'G';
		case 'G':
			return 'C';
		case 'a':
			return 't';
		case 't':
			return 'a';
		case 'c':
			return 'g';
		case 'g':
			return 'c';
		default:
			throw new IllegalStateException(base + " is not a valid DNA base.");
		}
	}

	public static boolean isIn(int begin, int end, int pos) {
		if ((pos >= begin) && (pos <= end)) {
			return true;
		}
		return false;
	}

	public static boolean contains(int seq1Begin, int seq1End, int seq2Begin, int seq2End) {
		if ((seq2Begin >= seq1Begin) && (seq2End <= seq1End)) {
			return true;
		}
		return false;
	}

}
