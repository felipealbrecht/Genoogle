package bio.pih.util.mutation;

import java.util.List;

import com.google.common.collect.Lists;

public class GenerateFamilies {

	//AAACAATCTCGATTCTAAATTGAAACGAACGCAGCATTTCAGGGACTGGATGAGGAGCTTACGGTTTTTTACAGAATCATCAATATCTTGGAAGAAAAAGAATGTTAAGAAATAACAAAACAATAATTATTAAGTACTTTCTTAATCTCATTAATGGAGC 
	//total 10 
	//generation 250 
	//fraction 4
	
	public static void main(String[] args) {
		generateSequenceFamilyTree("AAACAATCTCGATTCTAAATTGAAACGAACGCAGCATTTCAGGGACTGGATGAGGAGCTTACGGTTTTTTACAGAATCATCAATATCTTGGAAGAAAAAGAATGTTAAGAAATAACAAAACAATAATTATTAAGTACTTTCTTAATCTCATTAATGGAGC",10, 3, 0, 4);		
	}
			
	
	static List<List<SequenceInfo>> generateSequenceFamilyTree(String parent, int totalGenerations, int sequencesBygeneration, int generationCount, int sequenceFraction) {
		List<List<SequenceInfo>> sequences = Lists.newLinkedList();
				
		for (int i = 0; i < totalGenerations; i++) {
			List<SequenceInfo> children = generateCorrelateds(parent, sequencesBygeneration, generationCount, sequenceFraction);
			for (SequenceInfo sequence: children) {
				generateSequenceFamilyTree(sequence.getSequence(), totalGenerations, sequencesBygeneration, generationCount+1, sequenceFraction);
			}
			sequences.add(children);			
		}
		
		return sequences;		
	}
	
	static List<SequenceInfo> generateCorrelateds(String parent, int qtd, int generationCount, int sequenceFraction) {
		assert qtd >= 0;
		List<SequenceInfo> sequences = Lists.newLinkedList();
		for (int i = qtd; i > 0; i--) {
			String sequence = SequenceMutator.mutateSequence(parent, generationCount, sequenceFraction);
			SequenceInfo child = new SequenceInfo(sequence, generationCount, i);
			sequences.add(child);
		}
		return sequences;		
	}
	
	private static class SequenceInfo {
		String sequence;
		int deep;
		int num;
		
		public SequenceInfo(String sequence, int deep, int num) {
			this.sequence = sequence;
			this.deep = deep;
			this.num = num;
		}
		
		public String getSequence() {
			return sequence;
		}		
		
		@Override
		public String toString() {
			return "["+sequence + " " + deep + " " + num+"]";
		}
	}
}
