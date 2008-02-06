package bio.pih.index;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import org.biojava.bio.BioException;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;

import bio.pih.alignment.GenoogleNeedlemanWunsch;
import bio.pih.compressor.DNASequenceCompressorToShort;
import bio.pih.search.AlignmentResult;
import bio.pih.seq.LightweightSymbolList;

/**
 * Given two sub-sequences, return the *global* alignment between they.
 * 
 * @author Albrecht
 * 
 */
public class SubSequencesComparer {

	private final String indexFileName = "sequences.idx";
	private final String dataFileName  = "sequences.dat";
		
	private final SubstitutionMatrix substitutionMatrix;
	private final GenoogleNeedlemanWunsch aligner;
	private final DNASequenceCompressorToShort encoder;

	private static final int threadshould = 1;
	private static final int match = -1;
	private static final int dismatch = 1;
	private static final int gapOpen = 2;
	private static final int gapExtend = 0;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		new SubSequencesComparer(DNATools.getDNA(), 8, match, dismatch, gapOpen, gapOpen, gapExtend).createData(threadshould);
		//new SubSequencesComparer(DNATools.getDNA(), 8, match, dismatch, gapOpen, gapOpen, gapExtend).readData();
	}

	/**
	 * @param alphabet
	 * @param subSequenceLength
	 * @param match
	 * @param replace
	 * @param insert
	 * @param delete
	 * @param gapExtend
	 * @throws ValueOutOfBoundsException
	 */
	public SubSequencesComparer(FiniteAlphabet alphabet, int subSequenceLength, int match, int replace, int insert, int delete, int gapExtend) throws ValueOutOfBoundsException {
		this.substitutionMatrix = new SubstitutionMatrix(alphabet, match * -1, dismatch * -1); // values
		// from
		// ftp://ftp.ncbi.nlm.nih.gov/blast/matrices/NUC.4.4
		this.aligner = new GenoogleNeedlemanWunsch(match, replace, insert, delete, gapExtend, substitutionMatrix);
		this.encoder = new DNASequenceCompressorToShort(8);
	}

	private void readData() throws IOException, InvalidHeaderData {
		File indexFile = new File(indexFileName);		
		MappedByteBuffer mappedIndexFile = new FileInputStream(indexFile).getChannel().map(MapMode.READ_ONLY, 0, indexFile.length());				
		
		File dataFile = new File(dataFileName);
		FileChannel dataChannel = new FileInputStream(dataFile).getChannel();
		
		for (int i = 0; i <= 10; i++) {
			short sequence = mappedIndexFile.getShort();
			short quantity = mappedIndexFile.getShort();
			long  offset   = mappedIndexFile.getLong();
		
			System.out.println(encoder.decodeShortToString(sequence));
			System.out.println(quantity);
			System.out.println(offset);
				
			int[] similarSequences = getSimilarSequences(dataChannel, sequence, quantity, offset);
			int exacly = similarSequences[1];
			int veryDifferent = similarSequences[similarSequences.length-1];
			
			System.out.println("mais similar e nao exata: " + (encoder.decodeShortToString(ComparationResult.getSequenceFromRepresentation(exacly))) + " pontuation: " + ComparationResult.getScoreFromRepresentation(exacly));
			System.out.println("mais diferente: " + (encoder.decodeShortToString(ComparationResult.getSequenceFromRepresentation(veryDifferent))) + " pontuação: " + ComparationResult.getScoreFromRepresentation(veryDifferent));
			System.out.println("-------");			
		}

	}
	
	int[] getSimilarSequences(FileChannel dataChannel, short sequence, short quantity, long offset) throws IOException, InvalidHeaderData {
		
		int resultsQtd = quantity * 4;
		MappedByteBuffer map = dataChannel.map(MapMode.READ_ONLY, offset, 2+2+resultsQtd);
		IntBuffer buffer = map.asIntBuffer();
		int header = buffer.get();
		if ((header >> 16) != sequence) {
			throw new InvalidHeaderData("the value " + (header >> 16) + " is different from " + sequence);
		}
		
		if ((header & 0xFFFF) != quantity) {
			throw new InvalidHeaderData("the value " + (header & 0xFFFF) + " is different from " + sequence);
		}
						
		int similarSequences[] = new int[quantity];
		buffer.get(similarSequences, 0, quantity);
		return similarSequences;
	}

	/*
	 * offset index
	 * 
	 * <SEQUENCIA:2><QUANTITY:2><OFFSET:64>
	 * 
	 */

	/*
	 * data:
	 * 
	 * <SEQUENCIA:2><QUANTIDADE:4><<SEQUENCIA:><SCORE:8>>(CONTINUA)
	 */

	private void createData(int threadshould) throws Exception {
		int max = 0xFFFF;
		int score;
		long initialTime = System.currentTimeMillis();
		LinkedList<ComparationResult> results;

		File indexFile = new File(indexFileName);
		indexFile.delete();
		indexFile.createNewFile();
		FileChannel indexFileChannel = new FileOutputStream(indexFile).getChannel();

		File dataFile = new File(dataFileName);
		dataFile.delete();
		dataFile.createNewFile();
		FileChannel dataFileChannel = new FileOutputStream(dataFile).getChannel();

		for (int encodedSequence1 = 0; encodedSequence1 <= max; encodedSequence1++) {

			results = new LinkedList<ComparationResult>();
			for (int encodedSequence2 = 0; encodedSequence2 <= max; encodedSequence2++) {
				score = (int) compareCompactedSequences((short) encodedSequence1, (short) encodedSequence2);

				ComparationResult ar = new ComparationResult((short) score, (short) encodedSequence2);
				int alignmentIntRepresentation = ar.getIntRepresentation();

				if (ComparationResult.getScoreFromRepresentation(alignmentIntRepresentation) != (short) score) {
					System.out.println("score DIFERENTE!");
				}

				if (ComparationResult.getSequenceFromRepresentation(alignmentIntRepresentation) != (short) encodedSequence2) {
					System.out.println("sequence DIfererente");
				}

				if (score > 0) {
					results.add(ar);
				}
			}
			Collections.sort(results, new Comparator<ComparationResult>() {
				@Override
				public int compare(ComparationResult o1, ComparationResult o2) {
					return o2.getScore() - o1.getScore();
				}
			});
			System.out.println(getSequenceFromShort((short) (encodedSequence1 & 0xFFF)).getString() + " - " + results.size());
			
			System.out.println(encoder.decodeShortToString(results.get(results.size()-1).getSequence()));
			
			 // 2 for sequence + 2 for the quantity + 64 for offset
			ByteBuffer buffer = ByteBuffer.allocate(68);
			
			buffer.putShort((short)(encodedSequence1 & 0xFFFF));
			buffer.putShort((short)results.size()); 			
			buffer.putLong(dataFileChannel.position());
			buffer.flip();
			
			indexFileChannel.write(buffer);
							
			// 2 for sequence itself and 2 for the quantity (both for sanity check) and 4 bytes for each result
			int size = results.size() * 4 + 2 + 2;
			buffer = ByteBuffer.allocate(size);
			buffer.putShort((short) (encodedSequence1 & 0xFFFF));
			buffer.putShort((short) results.size());
			for(ComparationResult cr: results) {
				buffer.putInt(cr.getIntRepresentation());
			}
			buffer.flip();
			dataFileChannel.write(buffer);
		}
		
		indexFileChannel.close();
		dataFileChannel.close();

		System.out.println("-----------------------------");
		System.out.println(" tempo total: " + (System.currentTimeMillis() - initialTime));
		System.out.println("");
	}

	private void saveData() {

	}

	static HashMap<Short, LightweightSymbolList> encodedToSymbolList = new HashMap<Short, LightweightSymbolList>();

	private LightweightSymbolList getSequenceFromShort(short encodedSymbolList) throws IllegalSymbolException, BioException {
		LightweightSymbolList symbolList = encodedToSymbolList.get(encodedSymbolList);
		if (symbolList == null) {
			symbolList = (LightweightSymbolList) encoder.decodeShortToSymbolList(encodedSymbolList);
			encodedToSymbolList.put(encodedSymbolList, symbolList);
		}
		return symbolList;
	}

	private double compareCompactedSequences(short encodedSequence1, short encodedSequence2) throws Exception {
		LightweightSymbolList symbolList1 = getSequenceFromShort(encodedSequence1);
		LightweightSymbolList symbolList2 = getSequenceFromShort(encodedSequence2);

		// System.out.println(symbolList1.getString() + " - " +
		// symbolList2.getString());
		return aligner.pairwiseAlignment(symbolList1, symbolList2) * -1;
	}

	private void blah() throws BioException {
		Sequence seq1 = new SimpleSequence(LightweightSymbolList.createDNA("ACTGCGTC"), null, "ACTGCGTC", null);
		Sequence seq2 = new SimpleSequence(LightweightSymbolList.createDNA("TGCGTCCA"), null, "TGCGTCCA", null);
		double value = aligner.pairwiseAlignment(seq1, seq2);
		System.out.println(aligner.getAlignmentString());

		Sequence seq3 = new SimpleSequence(LightweightSymbolList.createDNA("TGGACCCC"), null, "TGGACCCC", null);
		Sequence seq4 = new SimpleSequence(LightweightSymbolList.createDNA("TGGACCCC"), null, "TGGACCCC", null);
		value = aligner.pairwiseAlignment(seq3, seq4);
		System.out.println(aligner.getAlignmentString());

		Sequence seq3a = new SimpleSequence(LightweightSymbolList.createDNA("AAAAAAAA"), null, "AAAAAAAA", null);
		Sequence seq4a = new SimpleSequence(LightweightSymbolList.createDNA("CCCCCCCC"), null, "CCCCCCCC", null);
		value = aligner.pairwiseAlignment(seq3a, seq4a);
		System.out.println(aligner.getAlignmentString());

		Sequence seq5 = new SimpleSequence(LightweightSymbolList.createDNA("AAAACCCC"), null, "AAAACCCC", null);
		Sequence seq6 = new SimpleSequence(LightweightSymbolList.createDNA("CCCCAAAA"), null, "CCCCAAAA", null);
		value = aligner.pairwiseAlignment(seq6, seq5);
		System.out.println(aligner.getAlignmentString());

		Sequence seq5a = new SimpleSequence(LightweightSymbolList.createDNA("AATCCCCT"), null, "AATCCCCT", null);
		Sequence seq6a = new SimpleSequence(LightweightSymbolList.createDNA("CCCCAAGA"), null, "CCCCAAGA", null);
		value = aligner.pairwiseAlignment(seq6a, seq5a);
		System.out.println(aligner.getAlignmentString());

		// continuacao da seq6a
		Sequence seq5aa = new SimpleSequence(LightweightSymbolList.createDNA("AATCCCCT"), null, "AATCCCCT", null);
		Sequence seq6aa = new SimpleSequence(LightweightSymbolList.createDNA("TCCCCAAG"), null, "TCCCAAAG", null);
		value = aligner.pairwiseAlignment(seq6aa, seq5aa);
		System.out.println(aligner.getAlignmentString());

		// continuacao da seq5a
		Sequence seq5ab = new SimpleSequence(LightweightSymbolList.createDNA("CAATCCCC"), null, "CAATCCCC", null);
		Sequence seq6ab = new SimpleSequence(LightweightSymbolList.createDNA("CCCCAAGA"), null, "CCCCAAGA", null);
		value = aligner.pairwiseAlignment(seq6ab, seq5ab);
		System.out.println(aligner.getAlignmentString());

		Sequence seq5b = new SimpleSequence(LightweightSymbolList.createDNA("AACCCCCT"), null, "AACCCCCT", null);
		Sequence seq6b = new SimpleSequence(LightweightSymbolList.createDNA("CCCCCAAA"), null, "CCCCCAAA", null);
		value = aligner.pairwiseAlignment(seq6b, seq5b);
		System.out.println(aligner.getAlignmentString());

		Sequence seq5c = new SimpleSequence(LightweightSymbolList.createDNA("AACCCCCT"), null, "AACCCCCT", null);
		Sequence seq6c = new SimpleSequence(LightweightSymbolList.createDNA("ACCCCCAA"), null, "ACCCCAAA", null);
		value = aligner.pairwiseAlignment(seq6c, seq5c);
		System.out.println(aligner.getAlignmentString());

		Sequence seq5d = new SimpleSequence(LightweightSymbolList.createDNA("TACCCCCT"), null, "TACCCCCT", null);
		Sequence seq6d = new SimpleSequence(LightweightSymbolList.createDNA("TCCCCCAA"), null, "TCCCCCAA", null);
		value = aligner.pairwiseAlignment(seq6d, seq5d);
		System.out.println(aligner.getAlignmentString());

		Sequence seq7 = new SimpleSequence(LightweightSymbolList.createDNA("AAAAAAAA"), null, "AAAAAAAA", null);
		Sequence seq8 = new SimpleSequence(LightweightSymbolList.createDNA("AAAAAAAA"), null, "AAAAAAAA", null);
		value = aligner.pairwiseAlignment(seq7, seq8);
		System.out.println(aligner.getAlignmentString());

		Sequence seq7a = new SimpleSequence(LightweightSymbolList.createDNA("AAAAAAAA"), null, "AAAAAAAA", null);
		Sequence seq8a = new SimpleSequence(LightweightSymbolList.createDNA("AAAAAAAC"), null, "AAAAAAAC", null);
		value = aligner.pairwiseAlignment(seq7a, seq8a);
		System.out.println(aligner.getAlignmentString());

		Sequence seq9 = new SimpleSequence(LightweightSymbolList.createDNA("AACCAACC"), null, "AACCAACC", null);
		Sequence seq10 = new SimpleSequence(LightweightSymbolList.createDNA("CCAACCAA"), null, "CCAACCAA", null);
		value = aligner.pairwiseAlignment(seq9, seq10);
		System.out.println(aligner.getAlignmentString());

		Sequence seq11 = new SimpleSequence(LightweightSymbolList.createDNA("AAAAACCC"), null, "AAAAACCC", null);
		Sequence seq12 = new SimpleSequence(LightweightSymbolList.createDNA("CCCAAAAA"), null, "CCCAAAAA", null);
		value = aligner.pairwiseAlignment(seq11, seq12);
		System.out.println(aligner.getAlignmentString());

		Sequence seq11a = new SimpleSequence(LightweightSymbolList.createDNA("AAATGCCC"), null, "AAATGCCC", null);
		Sequence seq12a = new SimpleSequence(LightweightSymbolList.createDNA("CCCGTAAA"), null, "CCCGTAAA", null);
		value = aligner.pairwiseAlignment(seq11a, seq12a);
		System.out.println(aligner.getAlignmentString());

		Sequence seq13 = new SimpleSequence(LightweightSymbolList.createDNA("AAAGGCCC"), null, "AAAAACCC", null);
		Sequence seq14 = new SimpleSequence(LightweightSymbolList.createDNA("CCCGGAAA"), null, "CCCAAAAA", null);
		aligner.pairwiseAlignment(seq13, seq14);
		System.out.println(aligner.getAlignmentString());

		Sequence seq15 = new SimpleSequence(LightweightSymbolList.createDNA("ACTGCCCC"), null, "AAAAACCC", null);
		Sequence seq16 = new SimpleSequence(LightweightSymbolList.createDNA("ACTGTTTT"), null, "CCCAAAAA", null);
		aligner.pairwiseAlignment(seq15, seq16);
		System.out.println(aligner.getAlignmentString());
	}

	private static class ComparationResult {
		short score;
		short sequence;

		/**
		 * @param score
		 * @param sequence
		 */
		public ComparationResult(short score, short sequence) {
			this.score = score;
			this.sequence = sequence;
		}

		/**
		 * @return the score
		 */
		public short getScore() {
			return score;
		}

		/**
		 * @return the sequence
		 */
		public short getSequence() {
			return sequence;
		}

		/**
		 * @return a int containing the sequence and the score.
		 */
		public int getIntRepresentation() {
			return ((sequence << 16) | (score & 0xFFFF));
		}

		/**
		 * @param alignmentIntRepresentation
		 * @return the score
		 */
		public static short getScoreFromRepresentation(int alignmentIntRepresentation) {
			return (short) (alignmentIntRepresentation & 0xFFFF);
		}

		/**
		 * @param alignmentIntRepresentation
		 * @return the sequence
		 */
		public static short getSequenceFromRepresentation(int alignmentIntRepresentation) {
			return (short) (alignmentIntRepresentation >> 16);
		}

		@Override
		public String toString() {
			return "[" + score + "/" + encodedToSymbolList.get((short) (sequence & 0xFFFF)).getString() + "]";
		}

	}
}
