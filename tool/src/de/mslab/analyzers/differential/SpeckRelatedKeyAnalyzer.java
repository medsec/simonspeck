package de.mslab.analyzers.differential;

import de.mslab.ciphers.Cipher;
import de.mslab.util.MathUtil;
import de.mslab.util.StringUtil;

public class SpeckRelatedKeyAnalyzer extends DifferentialAnalyzer {
	
	protected SpeckRelatedKeyAnalyzer(Cipher cipher) {
		super(cipher);
	}
	
	public void propagateDifferential(int fromRound, long[] fourRoundKeysDifference) {
		long[] keyDifferences = new long[numRounds + 4];
		
		for (int round = fromRound; round <= fromRound + 3; round++) {
			keyDifferences[round] = fourRoundKeysDifference[round - fromRound];
		}
		
		for (int round = fromRound - 1; round >= 0; round--) {
			// K^{i+4} = K^{i} ^ K^{i+1} ^ (K^{i+1} >>> 1) ^ (K^{i+3} >>> 3) ^ (K^{i+3} >>> 4)
			keyDifferences[round] = keyDifferences[round + 4] 
				^ keyDifferences[round + 1]
				^ MathUtil.rotateRight(keyDifferences[round + 1], 1, wordSize)
				^ MathUtil.rotateRight(keyDifferences[round + 3], 3, wordSize)
				^ MathUtil.rotateRight(keyDifferences[round + 3], 4, wordSize);
		}
		
		for (int round = fromRound; round <= numRounds - 4; round++) {
			// K^{i+4} = K^{i} ^ K^{i+1} ^ (K^{i+1} >>> 1) ^ (K^{i+3} >>> 3) ^ (K^{i+3} >>> 4)
			keyDifferences[round + 4] = keyDifferences[round] 
				^ keyDifferences[round + 1]
				^ MathUtil.rotateRight(keyDifferences[round + 1], 1, wordSize)
				^ MathUtil.rotateRight(keyDifferences[round + 3], 3, wordSize)
				^ MathUtil.rotateRight(keyDifferences[round + 3], 4, wordSize);
		}
		
		for (int round = 0; round <= numRounds; round++) {
			log(String.format(
				"Delta K^{%2d}: %s", round, StringUtil.toBinary(keyDifferences[round])
			));
		}
	}
	
	public void propagateFurtherBackwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits,
		int[] leftUnknownBits, int[] rightUnknownBits) {
		
		throw new Error("Not implemented");
	}

	public void propagateFurtherForwards(int fromRound, int[] leftActiveBits, int[] rightActiveBits,
		int[] leftUnknownBits, int[] rightUnknownBits) {

		throw new Error("Not implemented");
	}
	
}
