package bio.pih.util.mutation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Generate a list of mutate sequences based on a given seed.
 * 
 * @author albrecht
 * 
 */
public class MutateSequence {

	/**
	 * @param args
	 *            args[1] : total sequences (int) args[2] : generation by sequence (int) args[3] :
	 *            sequence fraction (int) args[4] : useSameSequence as seed? (true/false)
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {

		File sequenceFile = new File(args[0]);
		int totalSequences = Integer.parseInt(args[1]);
		int generationBySequence = Integer.parseInt(args[2]);
		int sequenceFraction = Integer.parseInt(args[3]);
		boolean useSameSequence = Boolean.parseBoolean(args[4]);
		File outputFile = new File(args[5]);

		System.out.println(outputFile.getCanonicalPath());
		if (outputFile.exists()) {
			throw new IOException("File " + outputFile + " already exists.");
		}

		boolean b = outputFile.createNewFile();
		if (!b) {
			throw new IOException(outputFile + " can not be create.");
		}
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(outputFile));
			String sequence = SequenceMutator.readSequence(sequenceFile);
			bw.write(sequence);

			for (int i = 0; i < totalSequences; i++) {
				String seq = SequenceMutator.mutateSequence(sequence, generationBySequence, sequenceFraction);
				bw.write('\n');
				bw.write(seq);
				if (useSameSequence) {
					sequence = seq;
				}
			}

		} finally {
			bw.close();
		}

		System.out.println("Done!");
	}
}
