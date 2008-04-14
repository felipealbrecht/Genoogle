package bio.pih;

import bio.pih.util.mutation.SequenceMutator;

public class MutateSequence {

	
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
