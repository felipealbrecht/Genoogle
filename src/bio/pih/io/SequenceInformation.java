package bio.pih.io;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Information about sequences that will be stored in the main memory.
 * This class also provides methods to store and load sequences from disk.
 *
 * <p>FILE FORMAT: (line break for reading propose)
 * 
 * <TOTAL_LENGTH:INT>
 * <SEQUENCE_ID:INT> 
 * <GI_LENGTH:SHORT><NAME_LENGTH:SHORT><ACCESSION_LENGTH:SHORT><DESCRIPTION_LENGTH:SHORT><SEQUENCE_BYTES_LENGTH:SHORT>
 * <GI:CHAR[]><NAME:CHAR[]><ACCESSION:CHAR[]><DESCRIPTION:CHAR[]> <VERSION:SHORT> 
 * <ENCODED_SEQUENCE:SHORT[]>
 *
 * @author albrecht
 */
public class SequenceInformation {

	static int unvariableCapacity = 20; // 4 (total length) + 4 (sequence id) + (5 * 2) (lengths) + 2 (version) 

	static Charset defaultCharset = Charset.forName("ISO-8859-1");

	private int id;
	private String gi;
	private String name;
	private String accession;
	private short version;
	private String description;
	private short[] encodedSequence;

	/**
	 * @param id 
	 * @param gi
	 * @param name
	 * @param accession
	 * @param version
	 * @param description
	 * @param encodedSequence  
	 */
	public SequenceInformation(int id, String gi, String name, String accession, short version, String description, short[] encodedSequence) {
		this.id = id;
		this.gi = gi;
		this.name = name;
		this.accession = accession;
		this.description = description;
		this.version = version;
		this.encodedSequence = encodedSequence;
	}

	/**
	 * @return an {@link ByteBuffer} containing this instance serialized
	 * @throws UnsupportedEncodingException 
	 */
	public ByteBuffer toByteBuffer() throws UnsupportedEncodingException {

		byte[] giBytes = null;
		byte[] nameBytes = null;
		byte[] accessionBytes = null;
		byte[] descriptionBytes = null;
			
		giBytes = gi.getBytes(defaultCharset.name());
		nameBytes = name.getBytes(defaultCharset.name());
		accessionBytes = accession.getBytes(defaultCharset.name());
		descriptionBytes = description.getBytes(defaultCharset.name());
		
		int encodedSequenceLengthInByte = encodedSequence.length * 2;
		ByteBuffer sequenceByteBuffer = ByteBuffer.allocate(encodedSequenceLengthInByte);
		sequenceByteBuffer.asShortBuffer().put(encodedSequence);		

		int totalVariableLength = giBytes.length + nameBytes.length + accessionBytes.length + descriptionBytes.length + encodedSequenceLengthInByte;
		assert totalVariableLength > 0;

		ByteBuffer buffer = ByteBuffer.allocate(unvariableCapacity + totalVariableLength);

		buffer.putInt(totalVariableLength);
		
		buffer.putInt(id);

		buffer.putShort((short) giBytes.length);
		buffer.putShort((short) nameBytes.length);
		buffer.putShort((short) accessionBytes.length);
		buffer.putShort((short) descriptionBytes.length);
		buffer.putShort((short) encodedSequenceLengthInByte);

		buffer.put(giBytes);
		buffer.put(nameBytes);
		buffer.put(accessionBytes);
		buffer.put(descriptionBytes);
		
		buffer.putShort(version);
		 
		buffer.put(sequenceByteBuffer);
		
		assert buffer.position() == buffer.capacity();
		
		return buffer;
	}

	/**
	 * Read a SequenceInformation from a {@link ByteBuffer} without knowing its variable length
	 * @param buffer
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static SequenceInformation informationFromByteBuffer(ByteBuffer buffer) throws UnsupportedEncodingException {
		return informationFromByteBuffer(buffer, -1);
	}
	
	/**
	 * Read a SequenceInformation from a {@link ByteBuffer} knowing its variable length
	 * @param buffer
	 * @param variableCapacity 
	 * @return the {@link SequenceInformation} stored
	 * @throws UnsupportedEncodingException 
	 */
	public static SequenceInformation informationFromByteBuffer(ByteBuffer buffer, int variableCapacity) throws UnsupportedEncodingException {
		if (variableCapacity == -1) {
			variableCapacity = buffer.getInt();
		}
		
		int id = buffer.getInt();

		short giLength = buffer.getShort();
		short nameLength = buffer.getShort();
		short accessionLength = buffer.getShort();
		short descriptionLength = buffer.getShort();
		short encodedSequenceLengthInBytes = buffer.getShort();

		assert variableCapacity == giLength + nameLength + accessionLength + descriptionLength + encodedSequenceLengthInBytes;

		byte[] giBytes = new byte[giLength];
		buffer.get(giBytes);
		String gi = new String(giBytes, defaultCharset.name());

		byte[] nameBytes = new byte[nameLength];
		buffer.get(nameBytes);
		String name = new String(nameBytes, defaultCharset.name());

		byte[] accessionBytes = new byte[accessionLength];
		buffer.get(accessionBytes);
		String accession = new String(accessionBytes, defaultCharset.name());

		byte[] descriptionBytes = new byte[descriptionLength];
		buffer.get(descriptionBytes);
		String description = new String(descriptionBytes, defaultCharset.name());

		short version = buffer.getShort();				
		
		int encodedSequenceLengthInShort = encodedSequenceLengthInBytes/2;
		short[] encodedSequence = new short[encodedSequenceLengthInShort];
		for (int i = 0; i < encodedSequenceLengthInShort; i++) {
			encodedSequence[i] = buffer.getShort();
		}
				
		return new SequenceInformation(id, gi, name, accession, version, description, encodedSequence); 
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SequenceInformation)) {
			return false;
		}
		
		SequenceInformation other = (SequenceInformation) obj;
		
		if (other.getId() != id) {
			return false;
		}
			
		if (!other.getAccession().equals(accession)) {
			return false;
		}

		if (!other.getName().equals(name)) {
			return false;
		}

		if (!other.getDescription().equals(description)) {
			return false;
		}

		if (!other.getGi().equals(gi)) {
			return false;
		}

		if (other.getVersion() != version) {
			return false;
		}

		short[] otherEncoded = other.encodedSequence;
		if (other.getEncodedSequence().length != encodedSequence.length) {
			return false;			
		}
				
		for (int i = 0; i < other.encodedSequence.length;i++) {
			if (otherEncoded[i] != encodedSequence[i]) {
				return false;
			}
		}		

		return true;
	}
	
	/**
	 * @return the amount of unvariableCapacity. Useful information to read the data from a stream. 
	 */
	public static int getUnvariableCapacity() {
		return unvariableCapacity;
	}
	
	
	/**
	 * @return the id of this sequence.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return gi
	 */
	public String getGi() {
		return gi;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return accession
	 */
	public String getAccession() {
		return accession;
	}

	/**
	 * @return version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @return encodedSequence
	 */
	public short[] getEncodedSequence() {
		return encodedSequence;
	}

}
