package bio.pih.genoogle.tests.encoder;

import junit.framework.TestCase;

import org.junit.Test;

import bio.pih.genoogle.encoder.MaskEncoder;
import bio.pih.genoogle.encoder.SequenceEncoder;
import bio.pih.genoogle.encoder.SequenceEncoderFactory;
import bio.pih.genoogle.seq.Alphabet;
import bio.pih.genoogle.seq.DNAAlphabet;
import bio.pih.genoogle.seq.IllegalSymbolException;
import bio.pih.genoogle.seq.LightweightSymbolList;
import bio.pih.genoogle.seq.RNAAlphabet;
import bio.pih.genoogle.seq.SymbolList;

public class MaskEncoderTest extends TestCase {

	@Test
	public void testDNAMaskAAAAAAAA() {
		String mask = "110011011011";
		int subSequenceLength = 8;
		Alphabet alphabet = DNAAlphabet.SINGLETON;		
		SequenceEncoder encoder = SequenceEncoderFactory.getEncoder(alphabet, subSequenceLength);
		
		MaskEncoder maskEncoder = new MaskEncoder(mask, encoder);
		
		int maskedEncodedSubSequence = maskEncoder.applyMask("AAAAAAAAAAAA");
		
		assertEquals(maskedEncodedSubSequence, encoder.encodeSubSequenceToInteger("AAAAAAAA"));
	}
	
	@Test
	public void testDNAMaskCACACACA() {
		String mask = "110011011011";
		               
		int subSequenceLength = 8;
		Alphabet alphabet = DNAAlphabet.SINGLETON;		
		SequenceEncoder encoder = SequenceEncoderFactory.getEncoder(alphabet, subSequenceLength);
		
		MaskEncoder maskEncoder = new MaskEncoder(mask, encoder);
		
		int maskedEncodedSubSequence = maskEncoder.applyMask("CACACACACACA");
		
		assertEquals(maskedEncodedSubSequence, encoder.encodeSubSequenceToInteger("CACAACCA"));
	}
	
	@Test
	public void testRNAMaskCACAAUCA() {
		String mask = "110011011011";		               
		               
		int subSequenceLength = 8;
		Alphabet alphabet = RNAAlphabet.SINGLETON;		
		SequenceEncoder encoder = SequenceEncoderFactory.getEncoder(alphabet, subSequenceLength);
		
		MaskEncoder maskEncoder = new MaskEncoder(mask, encoder);
		
		int maskedEncodedSubSequence = maskEncoder.applyMask("CAUUCACAUUCA");
		
		assertEquals(maskedEncodedSubSequence, encoder.encodeSubSequenceToInteger("CACAAUCA"));
	}
	
	@Test
	public void testRNAMaskCAUUCACAUUCUGACGCAUGACUGACUGACUGACUGACUGCAUGCA() throws IllegalSymbolException {
		String mask = "110011011011";

		int subSequenceLength = 8;
		Alphabet alphabet = RNAAlphabet.SINGLETON;		
		SequenceEncoder encoder = SequenceEncoderFactory.getEncoder(alphabet, subSequenceLength);
		
		MaskEncoder maskEncoder = new MaskEncoder(mask, encoder);
		SymbolList rna = LightweightSymbolList.createRNA("CAUUCACAUUCUGACGCAUGACUGACUGACUGACUGACUGCAUGCACA");
		int[] maskedEncodedSequence = maskEncoder.applySequenceMask(rna);
		
		SymbolList rnaMasked = LightweightSymbolList.createRNA("CACAAUCUGACAGAUGACACGAUGACCAGCCA");

		String decodedRnaMasked = encoder.decodeIntegerArrayToString(maskedEncodedSequence);
		assertEquals(rnaMasked.seqString(), decodedRnaMasked);
	}
}
