package bio.pih.util.mutation;


/**
 * Generate a list of mutate sequences based on a given seed.
 * @author albrecht
 *
 */
public class MutateSequence {

	
	/**
	 * @param args
	 *  args[1] : total sequences (int)
	 *  args[2] : generation by sequence (int)
	 *  args[3] : sequence fraction (int)
	 *  args[4] : useSameSequence as seed? (true/false) 
	 */
	public static void main(String[] args) {
		
		String sequence = args[0];
		int totalSequences = Integer.parseInt(args[1]);
		int generationBySequence = Integer.parseInt(args[2]);
		int sequenceFraction  = Integer.parseInt(args[3]);
		boolean useSameSequence = Boolean.parseBoolean(args[4]);
			
		System.out.println(sequence);
		for(int i = 0;  i < totalSequences; i++) {
			String seq = SequenceMutator.mutateSequence(sequence, generationBySequence, sequenceFraction);
			System.out.println(seq);
			if (useSameSequence) {
				sequence = seq;
			}			
		}
	}
}
