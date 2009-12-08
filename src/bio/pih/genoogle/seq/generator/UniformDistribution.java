package bio.pih.genoogle.seq.generator;

import java.util.Map;
import java.util.Random;

import bio.pih.genoogle.seq.Alphabet;

import com.google.common.collect.Maps;

public class UniformDistribution {

	private final Alphabet alphabet;
	private Map<Integer, Character> dist;
	private Random random;

	public UniformDistribution(Alphabet alphabet) {
		this.alphabet = alphabet;
		dist = Maps.newHashMap();
		random = new Random();

		int value = 0;
		for (char c : alphabet.getLetters()) {
			dist.put(value++, c);
		}
	}

	public String generateSymbolList(int lengthFrom, int lengthTo) {
		int length; 	
		if (lengthTo == -1) {
			length = lengthFrom;
		} else {
			length = random.nextInt(lengthTo - lengthFrom) + lengthFrom;
		}
		StringBuilder sb = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			int value = random.nextInt(alphabet.getSize());
			sb.append(dist.get(value));
		}

		return sb.toString();
	}
}
