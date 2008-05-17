package bio.pih.index;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

import org.biojava.bio.symbol.FiniteAlphabet;

import bio.pih.io.IndexedDNASequenceDataBank;
import bio.pih.io.IndexedSequenceDataBank;
import bio.pih.util.IntArray;

/**
 * An inverted index witch the data is stored into disk. 
 *
 * @author albrecht
 */
public class PersistentSubSequencesInvertedIndex extends MemorySubSequencesInvertedIndex {

	/**
	 * @param subSequenceLength
	 * @param alphabet
	 * @throws ValueOutOfBoundsException
	 */
	public PersistentSubSequencesInvertedIndex(int subSequenceLength, FiniteAlphabet alphabet) throws ValueOutOfBoundsException {
		super(subSequenceLength, alphabet);
	}
	
	/**
	 * Test >-)
	 * @param args
	 * @throws ValueOutOfBoundsException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ValueOutOfBoundsException, IOException {
		IndexedDNASequenceDataBank indexedDNASequenceDataBank = new IndexedDNASequenceDataBank("persistido", new File("files/fasta/cow.rna.fna"), IndexedSequenceDataBank.StorageKind.DISK);
		indexedDNASequenceDataBank.load();
		System.out.println(indexedDNASequenceDataBank);
		indexedDNASequenceDataBank.write();
	}
	
	@Override
	public void write() throws IOException {
		long[] subSequencesPos = new long[(int) Math.pow(2, 16)];
		
		File indexFile = new File("index.idx");
		File dataFile  = new File("index.dat");
		
		FileChannel indexChannel = new FileOutputStream(indexFile).getChannel();
		FileChannel dataChannel = new FileOutputStream(dataFile).getChannel();
		
		int[] bucket;
		final int[] EMPTY = new int[0];
		
		for (int i = 0; i < subSequencesPos.length; i++) {
			IntArray intArray = index[i];
			if (intArray != null) {
				bucket = intArray.getArray();
			} else {
				bucket = EMPTY;
			}
			
			System.out.println(i + " " + bucket.length);
			
			ByteBuffer buffer = ByteBuffer.allocate(16);
			buffer.putInt(i);
			buffer.putInt(bucket.length);
			buffer.putLong(dataChannel.position());
			buffer.rewind();
			indexChannel.write(buffer);
			
			buffer = ByteBuffer.allocate(8 + (bucket.length * 4));
			buffer.putInt(i);
			buffer.putInt(bucket.length);
			
			IntBuffer iBuffer = buffer.asIntBuffer();
			iBuffer.put(bucket);
			buffer.rewind();
			dataChannel.write(buffer);
		}		
	}

}
